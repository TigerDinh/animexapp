package com.project24.animexapp.ui.profile

import android.content.Intent
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

class LocalAnimeRVAdapter(var animeList : List<LocalAnime>): RecyclerView.Adapter<LocalAnimeRVAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_anime,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocalAnimeRVAdapter.MyViewHolder, position: Int) {
        val itemAnime = animeList[position]
        holder.bindAnime(itemAnime)
    }

    override fun getItemCount(): Int {
        return animeList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var view: View = itemView
        private lateinit var anime: LocalAnime

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val context = itemView.context
            val showAnimeIntent = Intent(context,AnimeDetails::class.java)
            showAnimeIntent.putExtra(ANIME_ID,anime.mal_id)
            context.startActivity(showAnimeIntent)
        }

        fun bindAnime(anime: LocalAnime) {
            this.anime = anime
            Glide.with(view.context).load(anime.imgURl).centerCrop().into(view.findViewById<ImageView>(R.id.anime_image))
            view.findViewById<TextView>(R.id.anime_title).text = anime.title
        }


        companion object {
            private val ANIME_ID = "ANIME_ID"
        }
    }

    fun addAll(givenAnimeList : List<LocalAnime>){
        animeList = givenAnimeList
    }

    fun clear() {
        animeList = emptyList()
    }

}