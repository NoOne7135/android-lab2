package com.example.clockapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.clockapp.databinding.ActivitySettingsScreenBinding
import java.util.*

class SettingsScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsScreenBinding
    private var isFirstSelection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val timeZones = TimeZone.getAvailableIDs()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            timeZones
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerTimeZone.adapter = adapter

        binding.spinnerTimeZone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                } else {
                    val selectedTimeZoneID = timeZones[position]
                    val selectedTimeZone = TimeZone.getTimeZone(selectedTimeZoneID)
                    saveSelectedTimeZone(selectedTimeZone)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.backButton.setOnClickListener {
            onBackTapped()
        }

        restoreSelectedTimeZone()
    }

    private fun saveSelectedTimeZone(selectedTimeZone: TimeZone) {
        val sharedPref = getSharedPreferences("MY_PREFS", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("selectedTimeZoneId", selectedTimeZone.id)
            apply()
        }

        val resultIntent = Intent()
        resultIntent.putExtra("selectedTimeZoneId", selectedTimeZone.id)
        setResult(Activity.RESULT_OK, resultIntent)
    }

    private fun restoreSelectedTimeZone() {
        val sharedPref = getSharedPreferences("MY_PREFS", Context.MODE_PRIVATE)
        val selectedTimeZoneId = sharedPref.getString("selectedTimeZoneId", "")
        if (selectedTimeZoneId != null && selectedTimeZoneId.isNotEmpty()) {
            val indexOfTimeZone = TimeZone.getAvailableIDs().indexOf(selectedTimeZoneId)
            if (indexOfTimeZone != -1) {
                binding.spinnerTimeZone.setSelection(indexOfTimeZone)
            }
        }
    }

    private fun onBackTapped() {
        finish()
    }
}
