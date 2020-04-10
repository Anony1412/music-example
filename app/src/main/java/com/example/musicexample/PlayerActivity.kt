package com.example.musicexample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_player.*

class PlayerActivity: AppCompatActivity(),
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
        // Bind to PlayerService
        Intent(this, PlayerService::class.java).also {
            intent ->
            startService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
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
        handler = object: Handler() {
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
    }
}
