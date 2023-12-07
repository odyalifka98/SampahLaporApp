package com.example.sampahlaporapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var errorMessageTextView: TextView

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.btnLogin)
        errorMessageTextView = findViewById(R.id.textViewErrorMessage)

        loginButton.setOnClickListener {
            if (validateInputs()) {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val uid = user!!.uid
                            print(uid)

                            db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener {doc ->
                                    val fullName = doc.getString("fullName")
                                    val address = doc.getString("address")

                                    val sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)

                                    val editor = sharedPreferences.edit()
                                    editor.putString("fullName", fullName)
                                    editor.putString("address", address)
                                    editor.putString("uid", uid)
                                    editor.apply()

                                    val intent = Intent(this, MapsActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        errorMessageTextView.text = "Email atau password yang Anda masukkan salah!"
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInputs(): Boolean {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return false
        } else if (!isValidEmail(email)) {
            emailEditText.error = "Invalid email format"
            return false
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = ("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}\$")
        val pattern = Pattern.compile(emailRegex)
        return pattern.matcher(email).matches()
    }

    fun openRegisterActivity(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
