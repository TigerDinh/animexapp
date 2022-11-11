package com.project24.animexapp.ui.home

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project24.animexapp.AnimeDetails
import com.project24.animexapp.R
import com.project24.animexapp.api.*

class AnimeRVAdapter(var animeList : List<Anime>): RecyclerView.Adapter<AnimeRVAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_anime,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeRVAdapter.MyViewHolder, position: Int) {
        val itemAnime = animeList[position]
        holder.bindAnime(itemAnime)
    }

    override fun getItemCount(): Int {
        return animeList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var view: View = itemView
        private lateinit var anime: Anime

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val context = itemView.context
            val showAnimeIntent = Intent(context,AnimeDetails::class.java)
            showAnimeIntent.putExtra(ANIME_ID,anime.mal_id)
            context.startActivity(showAnimeIntent)
        }

        fun bindAnime(anime: Anime) {
            this.anime = anime
            Glide.with(view.context).load(anime.imageData!!.jpg!!.URL).into(view.findViewById<ImageView>(R.id.anime_image))
            view.findViewById<TextView>(R.id.anime_title).text = anime.title
        }


        companion object {
            private val ANIME_ID = "ANIME_ID"
        }
    }




}