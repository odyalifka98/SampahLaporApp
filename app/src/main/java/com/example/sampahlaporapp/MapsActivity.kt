package com.example.sampahlaporapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.sampahlaporapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Calendar
import java.util.Date
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val dinasKebersihanMakassar = LatLng(-5.135562551405088, 119.42873702418484)
    val dinasKebersihanGowa = LatLng(-5.20646628740547, 119.46658299797724)
    val dinasKebersihanMaros = LatLng(-5.0026575191694, 119.57217037985826)
    val dinasKebersihanTakalar = LatLng(-5.420168275316799, 119.44651370271583)
    var myLocation = LatLng(-5.135562551405088, 119.42873702418484)

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            getLastLocation()
        }

        binding.hampirPenuhBtn.setOnClickListener {
            sendData("Hampir penuh")
        }

        binding.telahTumpahBtn.setOnClickListener {
            sendData("Telah tumpah")
        }

        binding.lihatRiwayat.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendData(status: String) {
        val sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("fullName", "")
        val address = sharedPreferences.getString("address", "")
        val uid = sharedPreferences.getString("uid", "")

        val (dinas, jarak) = getNearestOffice()

        val data = mapOf<String, Any>(
            "status" to status,
            "fullName" to fullName!!,
            "address" to address!!,
            "user_id" to uid!!,
            "coordinate" to "${myLocation.latitude}, ${myLocation.longitude}",
            "office_receiver" to dinas,
            "datetime" to Calendar.getInstance().time
        )

        db.collection("notifications")
            .add(data)
            .addOnSuccessListener {documentReference ->
                Toast.makeText(this, "Notifikasi telah dikirim!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                Toast.makeText(this, "Gagal mengirim notifikasi!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return false
        }
        return true
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    myLocation = LatLng(latitude, longitude)
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15F))
                    val bounds = LatLngBounds(myLocation, dinasKebersihanMakassar)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 95))

                    val (dinas, jarak) = getNearestOffice()
                    binding.jarakTV.text = "Jarak terdekat ke dinas kebersihan : $jarak km ($dinas)"
                }
            }
    }

    private fun getNearestOffice(): Pair<String, Double> {
        val jarakMakassar = haversineDistance(myLocation, dinasKebersihanMakassar)
        var jarakText = jarakMakassar
        var dinasText = "Dinas Pertamanan dan Kebersihan Kota Makassar"

        val jarakGowa= haversineDistance(myLocation, dinasKebersihanGowa)
        if (jarakGowa < jarakText) {
            jarakText = jarakGowa
            dinasText = "Dinas Lingkungan Hidup Kabupaten Gowa"
        }

        val jarakTakalar = haversineDistance(myLocation, dinasKebersihanTakalar)
        if (jarakTakalar < jarakText) {
            jarakText = jarakTakalar
            dinasText = "Dinas Kebersihan, Pertamanan dan Pemakaman Kabupaten Maros"
        }

        val jarakMaros= haversineDistance(myLocation, dinasKebersihanMaros)
        if (jarakMaros< jarakText) {
            jarakText = jarakMaros
            dinasText = "Dinas Lingkungan Hidup dan Pertanahan Kabupaten Takalar"
        }

        return Pair(dinasText, jarakText)
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera

        mMap.addMarker(MarkerOptions().position(dinasKebersihanMakassar).title("Dinas Pertamanan dan Kebersihan Kota Makassar"))
        mMap.addMarker(MarkerOptions().position(dinasKebersihanGowa).title("Dinas Lingkungan Hidup Kabupaten Gowa"))
        mMap.addMarker(MarkerOptions().position(dinasKebersihanMaros).title("Dinas Kebersihan, Pertamanan dan Pemakaman Kabupaten Maros"))
        mMap.addMarker(MarkerOptions().position(dinasKebersihanTakalar).title("Dinas Lingkungan Hidup dan Pertanahan Kabupaten Takalar"))

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled= true
    }

    fun haversineDistance(coord1: LatLng, coord2: LatLng): Double {
        val lat1 = Math.toRadians(coord1.latitude)
        val lon1 = Math.toRadians(coord1.longitude)
        val lat2 = Math.toRadians(coord2.latitude)
        val lon2 = Math.toRadians(coord2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))

        // Radius bumi dalam kilometer. Anda dapat menggantinya jika ingin hasil dalam satuan lain.
        val radiusOfEarth = 6371.0

        val distance = radiusOfEarth * c
        val roundedDistance = round(distance * 10.0.pow(2)) / 10.0.pow(2)

        return roundedDistance
    }
}