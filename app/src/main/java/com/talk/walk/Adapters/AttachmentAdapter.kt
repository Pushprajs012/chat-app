package com.talk.walk.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.talk.walk.Models.Attachment
import com.talk.walk.R
import de.hdodenhof.circleimageview.CircleImageView

class AttachmentAdapter(var mContext: Context, var attachmentList: MutableList<Attachment>, var onItemClickListener: OnItemClickListener):
    RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvAttachmentName: TextView = itemView.findViewById(R.id.tvAttachmentName)
        var civAttachmentIcon: CircleImageView = itemView.findViewById(R.id.civAttachmentIcon)
        var cvAttachment: CardView = itemView.findViewById(R.id.cvAttachment)
    }

    fun setAttachmentLists(attachmentList: MutableList<Attachment>) {
        this.attachmentList = attachmentList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_attachment_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var attachment = attachmentList[position]
        holder.civAttachmentIcon.setImageDrawable(attachment.drawable)
        holder.tvAttachmentName.text = attachment.attachment_name

        holder.cvAttachment.setOnClickListener {
            onItemClickListener.onItemClick(position, attachment)
        }
    }

    override fun getItemCount(): Int {
        return attachmentList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, attachment: Attachment)
    }
}