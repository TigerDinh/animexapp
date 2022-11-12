package com.project24.animexapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project24.animexapp.api.Anime
import com.project24.animexapp.api.CharacterData

class CharacterRVAdapter(var characterList : List<com.project24.animexapp.api.Character>): RecyclerView.Adapter<CharacterRVAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_anime,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharacterRVAdapter.MyViewHolder, position: Int) {
        val itemCharacter = characterList[position]
        holder.bindCharacter(itemCharacter)
    }

    override fun getItemCount(): Int {
        return characterList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var view: View = itemView
        private lateinit var character: com.project24.animexapp.api.Character

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            /*val context = itemView.context
            val showAnimeIntent = Intent(context,AnimeDetails::class.java)
            showAnimeIntent.putExtra(ANIME_ID,anime.mal_id)
            context.startActivity(showAnimeIntent)*/
        }

        fun bindCharacter(character: com.project24.animexapp.api.Character) {
            this.character = character
            Glide.with(view.context).load(character.characterData.imageData!!.jpg!!.URL).centerCrop().into(view.findViewById<ImageView>(R.id.anime_image))
            view.findViewById<TextView>(R.id.anime_title).text = character.characterData.characterName
        }


        companion object {
            private val ANIME_ID = "ANIME_ID"
        }
    }

}