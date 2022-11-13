package com.project24.animexapp.ui.home

import com.project24.animexapp.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.project24.animexapp.api.*
import com.smarteist.autoimageslider.SliderViewAdapter


class SliderAdapter(var animeList : List<KitsuAnimeData>): SliderViewAdapter<SliderAdapter.MyViewHolder>() {

    override fun getCount(): Int {
        return animeList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slideritem_anime, parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val itemAnime = animeList[position]
        viewHolder.bindAnime(itemAnime)
    }

    class MyViewHolder(itemView: View) : SliderViewAdapter.ViewHolder(itemView) {

        private var view: View = itemView
        private lateinit var anime: KitsuAnimeData

        fun bindAnime(anime: KitsuAnimeData) {
            this.anime = anime
            Glide.with(view.context).load(anime.attributes.coverImageData!!.original).centerCrop().into(view.findViewById(R.id.slider_anime_image))
            view.findViewById<TextView>(R.id.slider_anime_title).text = anime.attributes.title
            view.findViewById<TextView>(R.id.slider_anime_synopsis).text = anime.attributes.synopsis
        }
    }


}