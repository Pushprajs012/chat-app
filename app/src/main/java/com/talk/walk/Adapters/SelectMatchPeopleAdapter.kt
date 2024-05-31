package com.talk.walk.Adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.talk.walk.Models.SelectPeople
import com.talk.walk.R
import de.hdodenhof.circleimageview.CircleImageView

class SelectMatchPeopleAdapter(val mContext: Context, val selectPeopleList: List<SelectPeople>, var mCallback: DeviceClickListener): RecyclerView.Adapter<SelectMatchPeopleAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val civSelectPeopleProfile: CircleImageView = itemView.findViewById(R.id.civSelectPeopleProfile)
        val tvSelectPeopleName: TextView = itemView.findViewById(R.id.tvSelectPeopleName)
        val tvPointsCounter: TextView = itemView.findViewById(R.id.tvPointsCounter)
        val cvSelectPeople: CardView = itemView.findViewById(R.id.cvSelectPeople)
        val cvPoints: CardView = itemView.findViewById(R.id.cvPoints)

        init {
            cvSelectPeople.setOnClickListener {
                val list = selectPeopleList as List<SelectPeople>
                for (item in list.indices) {
                    list[item].isSelected = false
                }
                list[adapterPosition].isSelected = true

                mCallback.onDeviceClick(adapterPosition, list[adapterPosition])
                notifyDataSetChanged()

//                mContext?.let { it1 -> ContextCompat.getColor(it1, R.color.colorTeal) }?.let { it2 ->
//                    cvSelectPeople?.setBackgroundColor(it2)
//                }
            }
        }
    }

    interface DeviceClickListener {
        fun onDeviceClick(int: Int, selectPeople: SelectPeople)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_match_people_layout, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selectPeople: SelectPeople = selectPeopleList[position]

        Glide.with(mContext).load(selectPeople.drawable).into(holder.civSelectPeopleProfile)
        holder.tvSelectPeopleName.text = selectPeople.name
        holder.tvPointsCounter.text = selectPeople.points.toString()
        if (selectPeople.points == 0) {
            holder.cvPoints.visibility = View.INVISIBLE
        } else {
            holder.cvPoints.visibility = View.VISIBLE
        }

        if (selectPeopleList?.get(position) is SelectPeople) {
            val dataItem = selectPeopleList[position] as SelectPeople
            if (dataItem.isSelected) {
                holder.cvSelectPeople.setCardBackgroundColor(mContext.getColor(R.color.colorPearlAqua))
            } else {
                holder.cvSelectPeople.setCardBackgroundColor(mContext.getColor(R.color.white))

            }
        }
    }

    override fun getItemCount(): Int {
        return selectPeopleList.size
    }
}