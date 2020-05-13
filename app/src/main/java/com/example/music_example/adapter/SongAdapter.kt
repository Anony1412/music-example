package com.example.music_example.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.music_example.R
import com.example.music_example.model.SongItem
import kotlinx.android.synthetic.main.item_song.view.*

class SongAdapter(private val songList: ArrayList<SongItem>,
                  private val onClickListener: (Int) -> Unit)
    : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        fun bind(songItem: SongItem, onClickListener: (Int) -> Unit) {
            itemView.textViewSongTitle.text = songItem.title
            itemView.setOnClickListener { onClickListener(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun getItemCount(): Int = songList.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songList[position], onClickListener)

    }
}
