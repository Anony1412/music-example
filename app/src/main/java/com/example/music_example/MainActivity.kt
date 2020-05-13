package com.example.music_example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music_example.adapter.SongAdapter
import com.example.music_example.model.SongItem
import com.example.music_example.player.PlayerActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createListSong()
    }

    private fun createListSong() {
        /**
         * Create item list type ArrayList
         */
        songList = ArrayList()
        songList!!.add(
            SongItem(
                "All Falls Down",
                R.raw.all_falls_down
            )
        )
        songList!!.add(
            SongItem(
                "Coming Home Remix",
                R.raw.coming_home_remix
            )
        )
        songList!!.add(
            SongItem(
                "Dance Monkey",
                R.raw.dance_monkey_lyrics
            )
        )
        songList!!.add(
            SongItem(
                "Next To Me",
                R.raw.next_to_me
            )
        )

        /**
         * Setup for RecyclerView
         * @param Adapter: provided adapter between RecyclerView and Data
         * @param LayoutManager: specify display type for RecyclerView
         */
        recyclerViewListSongs.adapter =
            SongAdapter(
                songList!!
            ) { songPosition: Int -> onClickSongItem(songPosition) }
        recyclerViewListSongs.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerViewListSongs.setHasFixedSize(false)
    }

    /**
     * Solve click event when user click an item of RecyclerView
     * @note: A different deployment of callback
     */
    private fun onClickSongItem(songPosition: Int) {
        val intentPlayerActivity = Intent(this, PlayerActivity::class.java)
        intentPlayerActivity.apply {
            putExtra(SONG_POSITION, songPosition)
        }
        startActivity(intentPlayerActivity)
    }

    companion object {
        const val SONG_POSITION= "song_position"
        var songList: ArrayList<SongItem>? = null
    }
}
