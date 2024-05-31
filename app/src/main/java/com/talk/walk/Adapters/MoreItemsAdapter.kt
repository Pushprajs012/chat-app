package com.talk.walk.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.talk.walk.Models.MainMenuItems
import com.talk.walk.R

class MoreItemsAdapter(var mContext: Context, var mainMenuItemsList: MutableList<MainMenuItems>):
    RecyclerView.Adapter<MoreItemsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mainMenuItems = mainMenuItemsList[position]
    }

    override fun getItemCount(): Int {
        return mainMenuItemsList.size
    }
}