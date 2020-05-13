package com.example.music_example.player

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.music_example.MainActivity
import com.example.music_example.R
import java.lang.Exception
import kotlin.concurrent.thread

class PlayerService : Service() {

    private val binder = LocalBinder()
    private var currentPosition: Int = -1
    private var isPlaying: Boolean = false

    private lateinit var mediaPlayer: MediaPlayer

    /** methods for clients */
    fun createSong(songPosition: Int) {
        this.currentPosition = songPosition
        if (isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
            isPlaying = false
        }
        mediaPlayer = MediaPlayer.create(
            applicationContext,
            MainActivity.songList!![songPosition].path
        )
        mediaPlayer.start()
        isPlaying = true

        // set song name
        val strSongTitle = MainActivity.songList!![songPosition].title
        setSongTitle(strSongTitle)

        // set SeekBar max
        setSeekBarMax(mediaPlayer.duration)
        // update SeekBar
        updateSeekBarProgress()
        // init notification
        initNotification(strSongTitle)
    }

    private fun setSongTitle(strSongTitle: String) {
        val bundle = Bundle()
        bundle.putString(SONG_TITLE_VALUE, strSongTitle)
        val message = Message()
        message.what =
            MESSAGE_SONG_TITLE
        message.data = bundle
        PlayerActivity.handler.sendMessage(message)
    }

    /** methods for clients */
    fun onClickButtonPause(isPlayValue: Boolean) {
        this.isPlaying = isPlayValue
        if (isPlaying) {
            mediaPlayer.start()
        } else {
            isPlaying = false
            mediaPlayer.pause()
        }
    }

    /** methods for clients */
    fun onClickButtonPrevious() {
        currentPosition =
            if (currentPosition - 1 < 0) MainActivity.songList!!.size - 1
            else (currentPosition - 1)
        createSong(currentPosition)
    }

    /** methods for clients */
    fun onClickButtonNext() {
        currentPosition = (currentPosition + 1) % MainActivity.songList!!.size
        createSong(currentPosition)
    }

    private fun setSeekBarMax(value: Int) {
        val bundle = Bundle()
        bundle.putInt(SEEK_BAR_MAX_VALUE, value)
        val message = Message()
        message.what =
            MESSAGE_SEEK_BAR_MAX
        message.data = bundle
        PlayerActivity.handler.sendMessage(message)
    }

    /** update SeekBarProgress by time */
    private fun updateSeekBarProgress() {
        thread {
            val totalDuration = mediaPlayer.duration
            var currentPosition = 0
            while (currentPosition < totalDuration) {
                try {
                    Thread.sleep(500)
                    currentPosition = mediaPlayer.currentPosition
                    setSeekBarProgress(currentPosition)
                } catch (e: Exception) {
                }
            }
            // when song play completed let create random song from list
            playRandomSong()
        }
    }

    /** update seek bar progress by time */
    private fun setSeekBarProgress(currentProgress: Int) {
        val bundle = Bundle()
        bundle.putInt(PROGRESS_VALUE, currentProgress)
        val message = Message()
        message.what =
            MESSAGE_SEEK_BAR_PROGRESS
        message.data = bundle
        PlayerActivity.handler.sendMessage(message)
    }

    /** methods for clients */
    fun onSeekBarChanged(currentProgress: Int) {
        mediaPlayer.seekTo(currentProgress)
        Log.d("TAG", "onSeekBarChanged $currentProgress ${mediaPlayer.currentPosition}")
        if (isPlaying) {
            mediaPlayer.start()
        }
    }

    private fun playRandomSong() {
        currentPosition = (0 until MainActivity.songList!!.size).random()
        createSong(currentPosition)
    }

    private fun initNotification(songTitle: String) {
        //---------
        /** Action play */
        val intentPlay = Intent(this, PlayerService::class.java).apply {
            action = ACTION_PLAY
        }
        val pendingIntentPlay = PendingIntent.getService(this,
            0, intentPlay, 0)

        /** Action pause */
        val intentPause = Intent(this, PlayerService::class.java).apply {
            action = ACTION_PAUSE
        }
        val pendingIntentPause = PendingIntent.getService(this,
            0, intentPause, 0)

        /** Action previous */
        val intentPrevious = Intent(this, PlayerService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val pendingIntentPrevious = PendingIntent.getService(this,
            0, intentPrevious, 0)

        /** Action next */
        val intentNext = Intent(this, PlayerService::class.java).apply {
            action = ACTION_NEXT
        }
        val pendingIntentNext = PendingIntent.getService(this,
            0, intentNext, 0)
        //---------
        val mBuilder = NotificationCompat.Builder(this, PlayerActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(songTitle)
            .setContentText("Playing")
            .setLargeIcon(BitmapFactory.decodeResource(this.resources,
                R.drawable.ic_music
            ))
            .addAction(R.drawable.ic_skip_previous, "Pre", pendingIntentPrevious)
            .addAction(R.drawable.ic_pause, "Pause", pendingIntentPause)
            .addAction(R.drawable.ic_skip_next, "Next", pendingIntentNext)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .build()
        PlayerActivity.notificationManager.notify(NOTIFICATION_ID, mBuilder)
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action.toString()) {
            ACTION_PREVIOUS -> onClickButtonPrevious()
            ACTION_PAUSE -> onClickButtonPause(isPlaying)
            ACTION_NEXT -> onClickButtonNext()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    companion object {
        const val MESSAGE_SONG_TITLE = 1
        const val SONG_TITLE_VALUE = "song_title_value"
        const val MESSAGE_SEEK_BAR_PROGRESS = 2
        const val PROGRESS_VALUE = "seek_bar_progress"
        const val MESSAGE_SEEK_BAR_MAX = 3
        const val SEEK_BAR_MAX_VALUE = "seek_bar_max_value"
        const val NOTIFICATION_ID = 1

        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_PREVIOUS = "action_previous"
        const val ACTION_NEXT = "action_next"
    }
}
