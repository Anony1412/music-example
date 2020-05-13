package com.example.music_example.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.music_example.MainActivity
import com.example.music_example.PlayerIntentService
import com.example.music_example.R
import kotlinx.android.synthetic.main.activity_player.*

class PlayerActivity : AppCompatActivity(),
    SeekBar.OnSeekBarChangeListener {

    private lateinit var mService: PlayerService
    private var mBound = false
    private var songPosition: Int = -1
    private var isPlaying = false

    /** Defines callback for service binding, passed to bindService() */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // We've bound to PlayerService, cast the IBinder and get PlayerService instance
            val binder = service as PlayerService.LocalBinder
            mService = binder.getService()
            mBound = true

            // Plays song
            mService.createSong(songPosition)
            isPlaying = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        seekBarSongProcess.setOnSeekBarChangeListener(this)
        getData()
        initHandler()
    }

    override fun onStart() {
        super.onStart()
        initNotificationChannel()
        // Bind to PlayerService
        val intent = Intent(this, PlayerService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        startService(intent)
    }

    private fun initNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                R.string.app_name.toString(),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // Register the channel with the system
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onStop() {
        super.onStop()
        // Unbind Service
        unbindService(connection)
        mBound = false
    }

    // get song position from MainActivity
    private fun getData() {
        songPosition = intent.getIntExtra(MainActivity.SONG_POSITION, -1)
    }

    private fun initHandler() {
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    PlayerService.MESSAGE_SONG_TITLE -> setSongTitle(msg)
                    PlayerService.MESSAGE_SEEK_BAR_MAX -> setSeekBarMax(msg)
                    PlayerService.MESSAGE_SEEK_BAR_PROGRESS -> updateSeekBarProgress(msg)
                }
            }
        }
    }

    private fun setSongTitle(msg: Message) {
        val strSongTitle = msg.data.getString(PlayerService.SONG_TITLE_VALUE)
        textViewSongTitle.text = strSongTitle
    }

    private fun setSeekBarMax(msg: Message) {
        val seekbarMax = msg.data.getInt(PlayerService.SEEK_BAR_MAX_VALUE)
        seekBarSongProcess.max = seekbarMax
    }

    private fun updateSeekBarProgress(msg: Message) {
        val progress = msg.data.getInt(PlayerService.PROGRESS_VALUE)
        seekBarSongProcess.progress = progress
    }

    fun onClickButtonPause(view: View) {
        isPlaying = when (isPlaying) {
            true -> {
                buttonPause.setImageResource(R.drawable.ic_play)
                false
            }
            false -> {
                buttonPause.setImageResource(R.drawable.ic_pause)
                true
            }
        }
        mService.onClickButtonPause(isPlaying)
    }

    fun onClickButtonNext(view: View) {
        mService.onClickButtonNext()
    }

    fun onClickButtonPrevious(view: View) {
        mService.onClickButtonPrevious()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (seekBar != null) {
            mService.onSeekBarChanged(seekBar.progress)
        }
    }

    companion object {
        lateinit var handler: Handler
        lateinit var notificationManager: NotificationManager
        const val CHANNEL_ID = "com.example.musicexample"
    }
}
