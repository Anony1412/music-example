package com.example.music_example

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.music_example.player.PlayerActivity

class PlayerIntentService: IntentService("IntentService Example") {

    override fun onCreate() {
        super.onCreate()
        setIntentRedelivery(true)
        initNotify()
    }

    private fun initNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = NotificationCompat.Builder(this, PlayerActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle("test intent service")
                .setContentText("test test test")
                .build()
            PlayerActivity.notificationManager.notify(2, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("onStartCommand", intent?.action.toString())
        return super.onStartCommand(intent,flags,startId);
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d("onHandleIntent", intent?.action.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
