package com.project24.animexapp.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project24.animexapp.R

class ClubAdminAdapter(var adminsList: List<Admins>) : RecyclerView.Adapter<ClubAdminAdapter.ClubAdminViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubAdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_img_username_item, parent, false)
        return ClubAdminAdapter.ClubAdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClubAdminViewHolder, position: Int) {
        val adminItem = adminsList[position]
        holder.bindAdmin(adminItem)
    }

    override fun getItemCount(): Int {
        return adminsList.size
    }

    class ClubAdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var view: View = itemView
        private lateinit var adminItem: Admins
        init {

        }
        fun bindAdmin(adminItem: Admins) {
            this.adminItem = adminItem
            view.findViewById<TextView>(R.id.userName).text = adminItem.userName
        }

    }


}