package com.example.musicexample

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.session.MediaController
import android.media.session.MediaSession
import android.os.*
import android.util.Log
import java.lang.Exception
import kotlin.concurrent.thread

class PlayerService : Service() {

    private val binder = LocalBinder()
    private var currentPosition: Int = -1
    private var isPlaying: Boolean = false
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var notificationBuilder: Notification.Builder
    private lateinit var mediaSession: MediaSession
    private lateinit var mediaController: MediaController

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
        initNotify(strSongTitle)
    }

    private fun setSongTitle(strSongTitle: String) {
        val bundle = Bundle()
        bundle.putString(SONG_TITLE_VALUE, strSongTitle)
        val message = Message()
        message.what = MESSAGE_SONG_TITLE
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
        message.what = MESSAGE_SEEK_BAR_MAX
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
        message.what = MESSAGE_SEEK_BAR_PROGRESS
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

    private fun initNotify(songTitle: String) {
        createNotifyChannel()
        createNotify(songTitle)
        setNotifyTapAction()
    }

    private fun createNotify(songTitle: String) {
        mediaSession = MediaSession(this, "test media session compat")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(songTitle)
                .setContentText("Playing")
                .setSmallIcon(R.drawable.ic_music_note)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_music))
                .addAction(R.drawable.ic_skip_previous, "Pre", null)
                .addAction(R.drawable.ic_pause, "Pause", null)
                .addAction(R.drawable.ic_skip_next, "Next", null)
                .setStyle(
                    Notification.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.sessionToken)
                )
        }
        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun createNotifyChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            notificationChannel =
                NotificationChannel(CHANNEL_ID, R.string.app_name.toString(), importance)
            // Register the channel with the system
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun setNotifyTapAction() {
        // Create an explicit intent for an PlayerActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        // Set the intent that will fire when the user taps the notification
        notificationBuilder.setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        const val CHANNEL_ID = "com.example.musicexample"
    }
}
