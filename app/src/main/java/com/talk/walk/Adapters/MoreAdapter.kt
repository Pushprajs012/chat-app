package com.talk.walk.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.talk.walk.Models.Sections
import com.talk.walk.R

class MoreAdapter(var mContext: Context, var sectionList: MutableList<Sections>):
    RecyclerView.Adapter<MoreAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvMore: CardView = itemView.findViewById(R.id.cvMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_section_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sections = sectionList[position]
    }

    override fun getItemCount(): Int {
        return sectionList.size
    }
}