package com.talk.walk.Adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Activities.NotificationsActivity
import com.talk.walk.Models.Chat
import com.talk.walk.Models.ChatMore
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import com.talk.walk.Utils.Controller.Companion.canSendFriendRequest
import java.util.HashMap

class ChatMoreAdapter(
    var mContext: Context,
    var chatMoreList: MutableList<ChatMore>,
    var met_user_id: String,
    val onItemClickListener: OnItemClickListener,
    var onActionListener: OnActionListener,
    var dialog: Dialog
) :
    RecyclerView.Adapter<ChatMoreAdapter.ViewHolder>() {

    private val TAG: String? = ChatMoreAdapter::class.java.name
    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private var name: String = ""
    var isUserBlocked = false
    private var isNewFriendRequest: Boolean = false


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvChatMoreName: TextView = itemView.findViewById(R.id.tvChatMoreName)
        val cvChatMore: CardView = itemView.findViewById(R.id.cvChatMore)
        val ivChatMore: ImageView = itemView.findViewById(R.id.ivChatMore)
        val cvChatMoreCancel: CardView = itemView.findViewById(R.id.cvChatMoreCancel)
        val tvChatMoreCancel: TextView = itemView.findViewById(R.id.tvChatMoreCancel)

    }

    fun updateView(name: String) {
        this.name = name
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_chat_more_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMore = chatMoreList[position]
        auth = Firebase.auth
        currentUser = auth.currentUser!!

        holder.cvChatMoreCancel.visibility = View.GONE

        holder.tvChatMoreName.text = chatMore.name
        Glide.with(mContext).load(chatMore.drawable).into(holder.ivChatMore)
        if (Controller.isDarkTheme(Controller.mainActivity)) {
            holder.ivChatMore.setColorFilter(Color.WHITE)
        } else {
            holder.ivChatMore.setColorFilter(Color.BLACK)
        }

        if (Controller.isChatUserBlocked && Controller.blocked_user_id == currentUser.uid && (chatMore.name == mContext.getString(
                R.string.block
            ) || holder.tvChatMoreName.text == "Blocked")
        ) {
            holder.cvChatMore.isEnabled = true
        } else if (Controller.isChatUserBlocked) {
            holder.cvChatMore.isEnabled = false
        } else {
            holder.cvChatMore.isEnabled = true
        }

        if (chatMore.name == mContext.getString(R.string.block)) {
            databseReference.child("blocks").child(currentUser.getUid()).child(met_user_id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user_id = snapshot.child("user_id").value.toString()
                            val blocked_user_id = snapshot.child("met_user_id").value.toString()
                            if (user_id == currentUser.getUid()) {
                                holder.tvChatMoreName.text = "Blocked"
                                isUserBlocked = true
                            } else {
                                isUserBlocked = false
                            }

                            Log.e(TAG, "onBindViewHolder: $isUserBlocked")

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled: " + error.details)
                    }
                })
        } else if (chatMore.name == mContext.getString(R.string.add_friend) || name == mContext.resources.getString(
                R.string.friend_request_sent
            )
        ) {
            Log.e(TAG, "onBindViewHolder: ${Controller.const_receiver_user_id}")
            databseReference.child("chats").child(currentUser.uid).child(met_user_id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (ds in snapshot.children) {
                                val chat = ds.getValue(Chat::class.java)!!
                                if (chat.receiver_user_id.equals(currentUser.uid) && chat.sender_user_id != currentUser.uid) {
//                                    holder.cvChatMore.isEnabled = false
                                    break
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val type = snapshot.child("type").value.toString()
                            val friend_user_id = snapshot.child("user_id").value.toString()
                            val friend_met_user_id = snapshot.child("met_user_id").value.toString()
                            if (friend_user_id == currentUser.uid && friend_met_user_id == met_user_id) {
                                if (type == Constants.Keys.FRIEND_REQUEST_REJECTED) {
                                    holder.tvChatMoreName.text = "Friend Request Rejected"
                                    holder.tvChatMoreName.setTextColor(mContext.resources.getColor(R.color.colorPink))
                                } else {
                                    checkFriendRequest(holder, chatMore)
                                }
                            } else {
                                checkFriendRequest(holder, chatMore)
                            }

                        } else {
                            checkFriendRequest(holder, chatMore)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        } else if (chatMore.name.equals(Constants.Keys.FRIENDS, ignoreCase = true)) {
            holder.cvChatMore.isEnabled = false
        }



        holder.cvChatMore.setOnClickListener {
            if (chatMore.name == mContext.getString(R.string.block) && holder.tvChatMoreName.text == "Blocked") {
                databseReference.child("blocks").child(currentUser.getUid()).child(met_user_id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val user_id = snapshot.child("user_id").value.toString()
                                val blocked_user_id = snapshot.child("met_user_id").value.toString()
                                if (user_id == currentUser.getUid()) {
                                    holder.tvChatMoreName.text = "Blocked"
                                    unBlockUser(holder, chatMore)

                                }
                            } else {
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "onCancelled: " + error.details)
                        }
                    })
            } else if (chatMore.name == mContext.getString(R.string.report)) {
                databseReference.child("reports").child(currentUser.uid).child(met_user_id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(mContext, "You have reported", Toast.LENGTH_LONG)
                                    .show()
                            } else {
                                onItemClickListener.onItemClick(chatMore, false)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "onCancelled: ${error.details}")
                        }

                    })
            } else if (chatMore.name == mContext.getString(R.string.add_friend) && holder.tvChatMoreName.text.toString()
                    .equals(mContext.getString(R.string.add_friend))
            ) {
                if (Controller.const_receiver_user_id != currentUser.uid) {
                    if (canSendFriendRequest) {
                        databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(met_user_id)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val type = snapshot.child("type").value.toString()
                                        if (type == Constants.Keys.FRIEND_REQUEST_REJECTED) {
                                            holder.tvChatMoreName.text = "Friend Request Rejected"
                                            holder.cvChatMore.isEnabled = false
                                            holder.cvChatMoreCancel.visibility = View.GONE
                                        } else if (type == Constants.Keys.FRIEND_REQUESTS) {
                                            holder.tvChatMoreName.text = "Friend Request Sent"
                                            holder.cvChatMore.isEnabled = false
                                            holder.cvChatMoreCancel.visibility = View.GONE

                                        } else {
                                            checkPointsBeforeSendingFriendRequest(chatMore, holder)
                                        }
                                    } else {
                                        checkPointsBeforeSendingFriendRequest(chatMore, holder)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })

                    } else {
                        showCannotSendFriendRequestDialog(
                            chatMore,
                            "You cannot send friend request until you reply stranger back.",
                            holder
                        )
                    }
                } else {
//                    showCannotSendFriendRequestDialog(chatMore,"You cannot send friend request until stranger replies you back.", holder)
                    checkPointsBeforeSendingFriendRequest(chatMore, holder)
                }

            } else if (chatMore.name == mContext.getString(R.string.add_friend) && holder.tvChatMoreName.text.toString()
                    .equals("New Friend Request", ignoreCase = true)
            ) {
                dialog.dismiss()
                val notificationIntent = Intent(mContext, NotificationsActivity::class.java)
                mContext.startActivity(notificationIntent)
            } else {
                onItemClickListener.onItemClick(chatMore, false)
            }

        }

//        if (chatMore.name.equals("Friend request sent", ignoreCase = true)) {
//            databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(currentUser.uid).child(met_user_id).addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        val sent_user_id = snapshot.child("user_id").value.toString()
//                        if (sent_user_id == currentUser.uid) {
//                            holder.cvChatMoreCancel.visibility = View.VISIBLE
//                        } else {
//                            holder.cvChatMoreCancel.visibility = View.GONE
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                }
//            })
//        }


        holder.cvChatMoreCancel.setOnClickListener {
            if (holder.tvChatMoreName.text.toString()
                    .equals("Friend request sent", ignoreCase = true)
            ) {
                databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(currentUser.uid)
                    .child(met_user_id).setValue(null).addOnSuccessListener {
                    Toast.makeText(mContext, "Friend request cancelled", Toast.LENGTH_LONG).show()
                    databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(met_user_id)
                        .child(currentUser.uid).setValue(null)
                    databseReference.child(Constants.Keys.NOTIFICATIONS).child(met_user_id)
                        .setValue(null)
                    databseReference.child(Constants.Keys.NOTIFICATIONS).child(currentUser.uid)
                        .setValue(null)
                    holder.cvChatMoreCancel.visibility = View.GONE
                    holder.tvChatMoreName.text = "Add Friend"
                    dialog.dismiss()
                }
            }
        }
    }

    private fun checkFriendRequest(holder: ViewHolder, chatMore: ChatMore) {
        databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(currentUser.uid)
            .child(met_user_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    checkFriends(holder, chatMore, snapshot)
                } else {
                    holder.tvChatMoreName.text = mContext.getString(R.string.add_friend)
                    checkFriends(holder, chatMore, snapshot)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.details}")
            }

        })
    }

    private fun checkFriends(holder: ViewHolder, chatMore: ChatMore, snapshot: DataSnapshot) {
        databseReference.child(Constants.Keys.FRIENDS).child(currentUser.uid).child(met_user_id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot2: DataSnapshot) {
                    if (snapshot2.exists()) {
                        holder.tvChatMoreName.text = "Friends"
                        holder.cvChatMore.isEnabled = false
                    } else {
                        if (snapshot.child("user_id").value.toString() == met_user_id && snapshot.child(
                                "met_user_id"
                            ).value.toString() == currentUser.uid
                        ) {
                            holder.cvChatMore.isEnabled = false
                            if (snapshot.child("user_id").value.toString() == currentUser.uid) {
                                holder.tvChatMoreName.text = "Friend request sent"
                                holder.cvChatMoreCancel.visibility = View.VISIBLE
                            } else {
                                holder.cvChatMoreCancel.visibility = View.GONE
//                                holder.tvChatMoreName.text = "New Friend Request"
                                isNewFriendRequest = true
                                holder.cvChatMore.isEnabled = true
                            }
                        } else {
                            holder.cvChatMore.isEnabled = false
                            if (snapshot.child("user_id").value.toString() == currentUser.uid) {
                                holder.tvChatMoreName.text = "Friend request sent"
                                holder.cvChatMoreCancel.visibility = View.VISIBLE
                            } else {
                                holder.cvChatMoreCancel.visibility = View.GONE
//                                holder.tvChatMoreName.text = "New Friend Request"
                                isNewFriendRequest = true
                                holder.cvChatMore.isEnabled = true
                            }
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })
    }

    private fun showCannotSendFriendRequestDialog(
        chatMore: ChatMore,
        message: String,
        holder: ViewHolder
    ) {
        showTwoButtonDialog("Friend Request", message)
    }

    private fun checkPointsBeforeSendingFriendRequest(
        chatMore: ChatMore,
        holder: ViewHolder
    ) {
        databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.childrenCount > 10L) {
                            showTwoButtonDialog(
                                "Limit Exceeded",
                                "Your friend limit exceeded. You have to unfriend someone inorder to send more friend request"
                            )
                        } else {
                            databseReference.child(Constants.Keys.FRIEND_REQUESTS)
                                .child(currentUser.uid).child(met_user_id)
                                .addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            Toast.makeText(
                                                mContext,
                                                "You have already sent friend request",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            databseReference.child(Constants.Keys.FRIEND_REQUESTS)
                                                .child(met_user_id)
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if (snapshot.exists()) {
                                                            val type =
                                                                snapshot.child("type").value.toString()
                                                            if (type == Constants.Keys.FRIEND_REQUEST_REJECTED) {
                                                                holder.tvChatMoreName.text =
                                                                    "Friend Request Rejected"
                                                                holder.cvChatMore.isEnabled = false
                                                            } else {
                                                                sendFriendRequest(
                                                                    chatMore.name,
                                                                    holder
                                                                )
                                                            }
                                                        } else {
                                                            sendFriendRequest(chatMore.name, holder)
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {

                                                    }

                                                })
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e(
                                            TAG,
                                            "onCancelled: " + error.details
                                        )
                                    }
                                })
                        }
                    } else {
                        sendFriendRequest(chatMore.name, holder)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showTwoButtonDialog(
        tile: String,
        message: String
    ) {
        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.dialog_delete_account_layout)
        dialog.window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val tvDialogTwoButtonMessage: TextView = dialog.findViewById(R.id.tvDialogTwoButtonMessage)
        val tvDialogTwoButtonHeader: TextView = dialog.findViewById(R.id.tvDialogTwoButtonHeader)
        val tvDeleteCancel: TextView = dialog.findViewById(R.id.tvDeleteCancel)
        val tvDeleteOkay: TextView = dialog.findViewById(R.id.tvDeleteOkay)

        tvDialogTwoButtonHeader.text = tile
        tvDialogTwoButtonMessage.text = message

        tvDeleteCancel.setOnClickListener {
            dialog.dismiss()
        }

        tvDeleteCancel.visibility = View.GONE

        tvDeleteOkay.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun sendFriendRequest(name: String, holder: ViewHolder) {
        val dataMap = HashMap<String, Any>()
        dataMap["user_id"] = currentUser.uid
        dataMap["met_user_id"] = met_user_id
        dataMap["timestamp"] = System.currentTimeMillis()
        dataMap["type"] = Constants.Keys.FRIEND_REQUESTS
        dataMap["is_read"] = false
        databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(currentUser.uid)
            .child(met_user_id)
            .setValue(dataMap).addOnCompleteListener(
                OnCompleteListener<Void?> { task ->
                    if (task.isSuccessful) {
                        databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(met_user_id)
                            .child(currentUser.uid).setValue(dataMap)
                        val notification_id: String = databseReference.push().getKey().toString()
                        dataMap["notification_id"] = notification_id
                        databseReference.child(Constants.Keys.NOTIFICATIONS).child(met_user_id)
                            .setValue(dataMap)
                        Toast.makeText(mContext, "Friend request sent", Toast.LENGTH_SHORT).show()
                        holder.tvChatMoreName.text = "Friend request sent"
                        dialog.dismiss()
                        //                    chatMoreAdapter.updateView(name);
                    }
                })
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    private fun unBlockUser(holder: ViewHolder, chatMore: ChatMore) {
        databseReference.child("blocks").child(currentUser.uid).child(met_user_id).setValue(null)
            .addOnCompleteListener(
                OnCompleteListener<Void?> { task ->
                    if (task.isSuccessful) {
                        databseReference.child("blocks").child(met_user_id).child(currentUser.uid)
                            .setValue(null)
                        Toast.makeText(mContext, "UnBlocked Successfully", Toast.LENGTH_SHORT)
                            .show()
                        holder.tvChatMoreName.text = "Block"
                        onActionListener.onUnBlockAction(chatMore, false)
                        dialog.dismiss()
                    }
                })
    }

    override fun getItemCount(): Int {
        return chatMoreList.size
    }

    interface OnItemClickListener {
        fun onItemClick(chatMore: ChatMore, isAlreadyBlocked: Boolean)
    }

    interface OnActionListener {
        fun onUnBlockAction(chatMore: ChatMore, isUnBlock: Boolean)
    }
}