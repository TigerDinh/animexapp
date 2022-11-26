package com.project24.animexapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project24.animexapp.api.LocalAnime


class ReviewAdapter(var reviewList : List<Reviews>): RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent:ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reviewitem_anime, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    override fun onBindViewHolder(holder: ReviewAdapter.ViewHolder, position: Int) {
        val reviewItemAnime = reviewList[position]
        holder.bindAnime(reviewItemAnime)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//        private var reviewUser: TextView
//        private var reviewComment: TextView
//        var spoiler: TextView
//        private var date: TextView

        private var view: View = itemView
        private lateinit var reviewAnime: Reviews

//        init {
//            reviewUser = itemView.findViewById(R.id.reviewUsername)
//            reviewComment = itemView.findViewById(R.id.reviewComment)
//            spoiler = itemView.findViewById(R.id.spoilerTag)
//            date = itemView.findViewById(R.id.reviewDate)
//        }

        fun bindAnime(reviewAnime: Reviews) {
            this.reviewAnime = reviewAnime
            view.findViewById<TextView>(R.id.reviewUsername).text = reviewAnime.username
            view.findViewById<TextView>(R.id.reviewComment).text = reviewAnime.reviewComment
            view.findViewById<TextView>(R.id.reviewTitle).text = reviewAnime.reviewTitle
            view.findViewById<TextView>(R.id.reviewDate).text = reviewAnime.reviewDate

            if(reviewAnime.reviewSpoilers.equals("yes")) {
                view.findViewById<TextView>(R.id.spoilerTag).visibility = View.VISIBLE
            } else {
                view.findViewById<TextView>(R.id.spoilerTag).visibility = View.GONE
            }
        }

    }
}