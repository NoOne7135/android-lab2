package com.example.clockapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.clockapp.databinding.ActivityTimerScreenBinding

class TimerScreen : AppCompatActivity(), TimerService.TimerObserver {

    private lateinit var binding: ActivityTimerScreenBinding
    private var timerService: TimerService? = null
    private lateinit var serviceConnection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as TimerService.LocalBinder
                timerService = binder.getServiceInstance()
                timerService?.registerObserver(this@TimerScreen)

                updateStartStopButtonText()

                timerService?.let {
                    it.notifyObservers()
                    binding.timerDisplay.text = it.getCurrentTimeString() ?: "00:00:00"
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                timerService?.unregisterObserver(this@TimerScreen)
                timerService = null
            }
        }
        val serviceIntent = Intent(this, TimerService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        binding.lapButton.setOnClickListener { onLapTapped() }
        binding.resetButton.setOnClickListener { onResetTapped() }
        binding.startStopButton.setOnClickListener { onStartStopTapped() }
        binding.backButton.setOnClickListener { onBackTapped() }

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val lapsList = timerService?.getLapsList()
                Log.d("TimerScreen", timerService.toString())
                binding.timerDisplay.text = timerService?.getCurrentTimeString()

                lapsList?.let {
                    val adapter = ArrayAdapter<String>(this@TimerScreen, android.R.layout.simple_list_item_1, it)
                    binding.lapsListView.adapter = adapter
                }

                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }


    private fun updateStartStopButtonText() {
        val buttonText = if (timerService?.isTimerRunning == true) "Stop" else "Start"
        binding.startStopButton.text = buttonText
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    private fun onLapTapped() {
        timerService?.lapTapped()
    }

    private fun onResetTapped() {
        timerService?.resetTapped()
        updateStartStopButtonText()
    }

    private fun onStartStopTapped() {
        timerService?.startStopTapped()
        updateStartStopButtonText()
    }

    private fun onBackTapped() {
        finish()
    }

    override fun onTimerUpdate(time: String) {
        runOnUiThread {
            binding.timerDisplay.text = time
        }
    }
}

