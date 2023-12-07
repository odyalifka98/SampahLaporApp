package com.example.sampahlaporapp

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val backIcon: ImageView = findViewById(R.id.backIcon)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        // Isi konten card
        populateCardContent()
    }

    private fun populateCardContent() {
        val status: TextView = findViewById(R.id.status)
        val statusTextView: TextView = findViewById(R.id.statusText)
        val dinasText: TextView = findViewById(R.id.dinasText)
        val datetimeText: TextView = findViewById(R.id.datetimeText)

        val statusText = statusTextView.text.toString()

        if (statusText == "Hampir Penuh") {
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.yellow))
        } else if (statusText == "Penuh") {
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
        }

        dinasText.text = "Dinas: Dinas Kebersihan Makassar"

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentDateAndTime: String = dateFormat.format(Date())
        datetimeText.text = "Datetime: $currentDateAndTime"
    }
}