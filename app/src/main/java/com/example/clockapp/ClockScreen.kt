package com.example.clockapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.clockapp.databinding.ActivityClockScreenBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ClockScreen : AppCompatActivity() {

    private lateinit var binding: ActivityClockScreenBinding
    private val handler = Handler()
    private lateinit var selectedTimeZoneId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClockScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val sharedPref = getSharedPreferences("MY_PREFS", Context.MODE_PRIVATE)
        selectedTimeZoneId = sharedPref.getString("selectedTimeZoneId", TimeZone.getDefault().id)
            ?: TimeZone.getDefault().id
    }

    override fun onResume() {
        super.onResume()
        updateDateTime()
    }

    private fun updateDateTime() {
        val selectedTimeZone = TimeZone.getTimeZone(selectedTimeZoneId)
        val dateFormat = SimpleDateFormat("HH:mm:ss \n yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = selectedTimeZone
        val calendar = Calendar.getInstance(selectedTimeZone)
        val formattedDate = dateFormat.format(calendar.time)

        binding.TTimezone.text = "Selected Time Zone: ${selectedTimeZone.displayName}"
        binding.TDate.text = formattedDate

        handler.postDelayed({ updateDateTime() }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    fun onClickGoSettings(view: View) {
        val intent = Intent(this, SettingsScreen::class.java)
        resultLauncher.launch(intent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val newSelectedTimeZoneId = data?.getStringExtra("selectedTimeZoneId")
            if (!newSelectedTimeZoneId.isNullOrEmpty()) {
                selectedTimeZoneId = newSelectedTimeZoneId
                handler.removeCallbacksAndMessages(null)
                updateDateTime()
            }
        }
    }

    fun onClickGoTimer(view: View) {
        val intent = Intent(this, TimerScreen::class.java)
        startActivity(intent)
    }
}

