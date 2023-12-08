package com.example.sampahlaporapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Calendar
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        fullNameEditText = findViewById(R.id.editTextFullName)
        addressEditText = findViewById(R.id.editTextAddress)
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)
        registerButton = findViewById(R.id.btnRegister)

        registerButton.setOnClickListener {
            if (validateInputs()) {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                val fullName = fullNameEditText.text.toString().trim()
                val address = addressEditText.text.toString().trim()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val user = auth.currentUser
                        val data = mapOf<String, Any>(
                            "fullName" to fullName,
                            "address" to address,
                            "email" to email,
                        )

                        db.collection("users").document(user!!.uid)
                            .set(data)
                            .addOnSuccessListener {
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Fail to register", Toast.LENGTH_SHORT).show()
                            }
                        Toast.makeText(this, "Register berhasil", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Register gagal", Toast.LENGTH_SHORT).show()
                    }

//
//                auth.createUserWithEmailAndPassword(email, password)
//                    .addOnCompleteListener(this) { task ->
//                        if (task.isSuccessful) {
//                            val user = auth.currentUser
//                            val data = mapOf<String, Any>(
//                                "fullName" to fullName,
//                                "address" to address,
//                                "email" to email,
//                            )
//
//                            db.collection("users").document(user!!.uid)
//                                .set(data)
//                                .addOnSuccessListener {
//                                    val intent = Intent(this, MapsActivity::class.java)
//                                    startActivity(intent)
//                                    finish()
//                                }
//                                .addOnFailureListener {
//                                    Toast.makeText(this, "Fail to register", Toast.LENGTH_SHORT).show()
//                                }
//                        } else {
//                            Toast.makeText(this, "Register gagal", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    .addOnFailureListener {
//                        Toast.makeText(this, "Register gagal", Toast.LENGTH_SHORT).show()
//                    }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val fullName = fullNameEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (fullName.isEmpty()) {
            fullNameEditText.error = "Full Name is required"
            return false
        }

        if (address.isEmpty()) {
            addressEditText.error = "Full Name is required"
            return false
        }

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
        } else if (password.length < 6) {
            passwordEditText.error = "Required at least 6 characters"
            return false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Confirm Password is required"
            return false
        } else if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = ("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}\$")
        val pattern = Pattern.compile(emailRegex)
        return pattern.matcher(email).matches()
    }

    fun openLoginActivity(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
