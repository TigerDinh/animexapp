package com.project24.animexapp.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project24.animexapp.R

class MembersAdapter(var membersList: List<Admins>) : RecyclerView.Adapter<MembersAdapter.MembersViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_img_username_item, parent, false)
        return MembersAdapter.MembersViewHolder(view)
    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        val adminItem = membersList[position]
        holder.bindAdmin(adminItem)
    }

    override fun getItemCount(): Int {
        return membersList.size
    }

    class MembersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var view: View = itemView
        private lateinit var memberItem: Admins
        init {

        }
        fun bindAdmin(memberItem: Admins) {
            this.memberItem = memberItem
            view.findViewById<TextView>(R.id.userName).text = memberItem.userName
        }

    }


}