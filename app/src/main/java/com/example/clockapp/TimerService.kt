package com.example.clockapp

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Handler
import java.lang.ref.WeakReference
import java.util.ArrayList

class TimerService : Service() {

    interface TimerObserver {
        fun onTimerUpdate(time: String)
    }

    private var handler: Handler? = null
    var isTimerRunning = false
    private var time = 0
    private var lastTime = 0
    private val lapsList = ArrayList<String>()
    private val observers = mutableListOf<TimerObserver>()

    inner class LocalBinder : Binder() {
        fun getServiceInstance(): TimerService {
            return this@TimerService
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun registerObserver(observer: TimerObserver) {
        observers.add(observer)
    }

    fun unregisterObserver(observer: TimerObserver) {
        observers.remove(observer)
    }

    fun notifyObservers() {
        val currentTimeString = getCurrentTimeString()
        observers.forEach { it.onTimerUpdate(currentTimeString) }
    }

    fun lapTapped() {
        if (isTimerRunning) {
            val seconds = time - lastTime
            val minutes = seconds / 60
            val hours = minutes / 60

            lapsList.add(String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60))
            lastTime = time
        }
    }

    fun resetTapped() {
        time = 0
        lapsList.clear()
        stopTimer()
        isTimerRunning = false
        lastTime = 0
        notifyObservers()
    }

    fun startStopTapped() {
        if (!isTimerRunning) {
            isTimerRunning = true
            startTimer()
        } else {
            stopTimer()
            isTimerRunning = false
        }
        notifyObservers()
    }

    private fun startTimer() {
        handler = WeakReference(this).get()?.mainLooper?.let { Handler(it) }
        handler?.post(object : Runnable {
            override fun run() {
                if (isTimerRunning) {
                    if (time == 0) {
                        lastTime = 0
                    }
                    time++
                    notifyObservers()
                    handler?.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun stopTimer() {
        handler?.removeCallbacksAndMessages(null)
    }

    fun getCurrentTimeString(): String {
        val seconds = time
        val minutes = seconds / 60
        val hours = minutes / 60

        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
    }

    fun getLapsList(): ArrayList<String> {
        return lapsList
    }
}
