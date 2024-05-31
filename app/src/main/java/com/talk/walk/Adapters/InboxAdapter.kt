package com.talk.walk.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Activities.ChatActivity
import com.talk.walk.Models.Chat
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import de.hdodenhof.circleimageview.CircleImageView

class InboxAdapter(
    var mContext: Context,
    var chatList: MutableList<Chat>,
    var fragmentManager: FragmentManager?,
    var onItemClickListener: OnItemClickListener
):
    RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    private val TAG: String? = InboxAdapter::class.java.name
    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvInboxName: TextView = itemView.findViewById(R.id.tvInboxName)
        var tvInboxMessage: TextView = itemView.findViewById(R.id.tvInboxMessage)
        var civInboxProfilePic: CircleImageView = itemView.findViewById(R.id.civInboxProfilePic)
        var cvInbox: CardView = itemView.findViewById(R.id.cvInbox)
        var cvProfileBG: CardView = itemView.findViewById(R.id.cvProfileBG)
        var ivInboxIsReadIndicator: ImageView = itemView.findViewById(R.id.ivInboxIsReadIndicator)
        var ivChatPaidCoin: ImageView = itemView.findViewById(R.id.ivChatPaidCoin)
    }

    fun setInboxLists(chatList: MutableList<Chat>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_inbox_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var chat = chatList[position]

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        holder.cvInbox.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.white))

        databseReference.child("chats").child(currentUser.uid).child(chat.receiver_user_id).limitToFirst(1).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val const_sender_user_id = snapshot.child("sender_user_id").value.toString()
                val const_receiver_user_id = snapshot.child("receiver_user_id").value.toString()
                if (snapshot.child("is_paid").exists()) {
                    val isPaid = snapshot.child("is_paid").value as Boolean
                    Controller.isPaid = isPaid
                    if (isPaid) {
                        holder.ivChatPaidCoin.visibility = View.VISIBLE
                    } else {
                        holder.ivChatPaidCoin.visibility = View.GONE
                    }
                } else {
                    holder.ivChatPaidCoin.visibility = View.VISIBLE
                }
            }

            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        if (Controller.isDarkTheme(Controller.mainActivity)) {
            holder.tvInboxMessage.setTextColor(Color.GRAY)
            holder.tvInboxName.setTextColor(Color.WHITE)
        } else {
            holder.tvInboxMessage.setTextColor(Color.GRAY)
            holder.tvInboxName.setTextColor(Color.BLACK)
        }
        if (chat.media_type.isEmpty()) {
            if (chat.message.isEmpty()) {
                holder.tvInboxMessage.visibility = View.GONE
            } else {
                holder.tvInboxMessage.text = chat.message
            }
        } else if (chat.media_type == Constants.Values.GALLERY || chat.media_type == Constants.Values.CAMERA && chat.sender_user_id == currentUser.uid) {
            holder.tvInboxMessage.text = "You have sent an image"
        } else if (chat.media_type == Constants.Values.GALLERY || chat.media_type == Constants.Values.CAMERA && chat.receiver_user_id == currentUser.uid) {
            holder.tvInboxMessage.text = "You have received an image"
        } else if (chat.media_type == Constants.Values.VOICE && chat.sender_user_id == currentUser.uid) {
            holder.tvInboxMessage.text = "You have sent an voice message"
        } else if (chat.media_type == Constants.Values.VOICE && chat.receiver_user_id == currentUser.uid) {
            holder.tvInboxMessage.text = "You have received an voice message"
        } else if (chat.media_type == Constants.Values.VIDEO && chat.sender_user_id == currentUser.uid) {
            holder.tvInboxMessage.text = "You have sent a video"
        } else if (chat.media_type == Constants.Values.VIDEO && chat.receiver_user_id == currentUser.uid) {
            holder.tvInboxMessage.text = "You have received a video"
        }

        if (chat.is_read) {
            holder.ivInboxIsReadIndicator.visibility = View.GONE
        } else {
            holder.ivInboxIsReadIndicator.visibility = View.INVISIBLE
        }

        val databaseRef: DatabaseReference = if (chat.receiver_user_id == currentUser.uid) {
            databseReference.child("users").child(chat.sender_user_id)
        } else {
            databseReference.child("users").child(chat.receiver_user_id)

        }



        databseReference.child("users").child(chat.user_key).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username: String = snapshot.child("username").value.toString()
                    holder.tvInboxName.text = username
                    if (snapshot.child("profile_image").exists()) {
                        val profile_image: String = snapshot.child("profile_image").value.toString()
                        if (Controller.isValidContextForGlide(mContext)) {
                            Glide.with(mContext).load(profile_image).error(R.drawable.user).into(holder.civInboxProfilePic)

                        }
                    } else {
                        if (Controller.isValidContextForGlide(mContext)) {
                            holder.civInboxProfilePic.setImageDrawable(getDrawable(mContext, R.drawable.user))
                        }
                        holder.cvProfileBG.setCardBackgroundColor(Controller.getRandomColor())
                        var layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                        layoutParams.leftMargin = 16
                        layoutParams.rightMargin = 16
                        layoutParams.topMargin = 16
                        layoutParams.bottomMargin = 16
                        holder.civInboxProfilePic.layoutParams = layoutParams
                        holder.civInboxProfilePic.setColorFilter(Color.WHITE)
                    }
                } else {
                    holder.tvInboxMessage.text = mContext.resources.getString(R.string.account_deleted)
                    holder.civInboxProfilePic.setImageDrawable(getDrawable(mContext, R.drawable.user))
                    holder.cvProfileBG.setCardBackgroundColor(Controller.getRandomColor())
                    var layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    layoutParams.leftMargin = 16
                    layoutParams.rightMargin = 16
                    layoutParams.topMargin = 16
                    layoutParams.bottomMargin = 16
                    holder.civInboxProfilePic.layoutParams = layoutParams
                    holder.civInboxProfilePic.setColorFilter(Color.WHITE)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.details}", )
            }
        })

        holder.cvInbox.setOnClickListener {
            val chatIntent = Intent(mContext, ChatActivity::class.java)
            if (currentUser.uid == chat.receiver_user_id) {
                chatIntent.putExtra("met_user_id", chat.sender_user_id)
            } else {
                chatIntent.putExtra("met_user_id", chat.receiver_user_id)
            }
            mContext.startActivity(chatIntent)
        }


        holder.cvInbox.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(p0: View?): Boolean {
//                val builder = AlertDialog.Builder(mContext)
//                    .setTitle("Delete Conversation")
//                    .setMessage("Are you sure you want to delete this conversation? By deleting you cannot restore it.")
//                    .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
//                        override fun onClick(p0: DialogInterface?, p1: Int) {
//
//                        }
//                    }).setNegativeButton("No", object : DialogInterface.OnClickListener {
//                        override fun onClick(p0: DialogInterface?, p1: Int) {
//                            p0?.dismiss()
//                        }
//
//                    })
//                builder.show()

                onItemClickListener.onItemClick(holder, chat, chatList, position)

                return false
            }
        })



    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    interface OnItemChange {
        fun notifyAdapter(position: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(holder: ViewHolder, chat: Chat, chatList: MutableList<Chat>, position: Int)
    }
}