package com.example.sampahlaporapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    val dinasKebersihan = LatLng(-5.135562551405088, 119.42873702418484)
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
    }

    private fun sendData(status: String) {
        val sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("fullName", "")
        val address = sharedPreferences.getString("address", "")
        val uid = sharedPreferences.getString("uid", "")

        val data = mapOf<String, Any>(
            "status" to status,
            "fullName" to fullName!!,
            "address" to address!!,
            "user_id" to uid!!,
            "coordinat" to "${myLocation.latitude}, ${myLocation.longitude}",
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
                    val bounds = LatLngBounds(myLocation, dinasKebersihan)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 95))
                    val jarak = haversineDistance(myLocation, dinasKebersihan)
                    binding.jarakTV.text = "Jarak : $jarak KM"
                }
            }
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

        mMap.addMarker(MarkerOptions().position(dinasKebersihan).title("Marker in dinas kebersihan"))

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