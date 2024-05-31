package com.talk.walk.Adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Activities.ChatActivity
import com.talk.walk.Activities.PointsActivity
import com.talk.walk.Fragments.InsuffienctPointsBottmSheetFragment
import com.talk.walk.Models.Users
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import de.hdodenhof.circleimageview.CircleImageView

class SearchAdapter(
    var mContext: Context,
    var usersList: MutableList<Users>,
    var supportFragmentManager: FragmentManager
) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private val TAG: String = SearchAdapter::javaClass.name
    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvFriendName: TextView = itemView.findViewById(R.id.tvFriendName)
        var tvFriendGender: TextView = itemView.findViewById(R.id.tvFriendGender)
        var civFriendProfilePic: CircleImageView = itemView.findViewById(R.id.civFriendProfilePic)
        var cvFriends: CardView = itemView.findViewById(R.id.cvFriends)
        var cvProfileBG: CardView = itemView.findViewById(R.id.cvProfileBG)
    }

    fun setSearchList(usersList: MutableList<Users>) {
        this.usersList = usersList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_friend_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var users = usersList[position]

        auth = Firebase.auth
        currentUser = auth.currentUser!!
        holder.cvFriends.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.white))

        holder.tvFriendGender.text = users.gender
        holder.tvFriendName.text = users.username
        if (users.profile_image.isNotEmpty()) {
//            Picasso.get().load(users.profile_image).into(holder.civFriendProfilePic)
            Glide.with(mContext).load(users.profile_image).error(R.drawable.user).into(holder.civFriendProfilePic)
        } else {
            Glide.with(mContext).load(R.drawable.user).fitCenter().into(holder.civFriendProfilePic)
            holder.cvProfileBG.setCardBackgroundColor(Controller.getRandomColor())
            var layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            layoutParams.leftMargin = 16
            layoutParams.rightMargin = 16
            layoutParams.topMargin = 16
            layoutParams.bottomMargin = 16
            holder.civFriendProfilePic.layoutParams = layoutParams
            holder.civFriendProfilePic.setColorFilter(Color.WHITE)
        }

        holder.cvFriends.setOnClickListener {
            if (currentUser.uid == users.user_id) {
                val chatIntent =
                    Intent(mContext, ChatActivity::class.java)
                chatIntent.putExtra(
                    "met_user_id",
                    users.user_id
                )
                mContext.startActivity(chatIntent)
            } else {
                databseReference.child("chats").child(currentUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val count = snapshot.childrenCount as Long
                                checkPointsBeforeSending(count, users)
                            } else {
                                checkPointsBeforeSending(0, users)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            }

        }
    }

    private fun checkPointsBeforeSending(count: Long, users: Users) {
        databseReference.child("chats").child(currentUser.uid).child(users.user_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val chatIntent =
                        Intent(mContext, ChatActivity::class.java)
                    chatIntent.putExtra(
                        "met_user_id",
                        users.user_id
                    )
                    mContext.startActivity(chatIntent)
                } else {
                    databseReference.child(Constants.Keys.USERS).child(currentUser.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val points = snapshot.child("points").value as Long
                                    if (points >= 20) {
                                        if (count > 10) {
                                            showTwoButtonDialog("Limit Exceeded", "Your friend limit exceeded. You have to unfriend someone in order to send more friend request")
                                        } else {
                                            sendChat(users.user_id)
                                            databseReference.child(Constants.Keys.USERS).child(currentUser.uid).child("points").setValue(points - 20)
                                            if (currentUser.uid != users.user_id) {
                                                databseReference.child("already_met").child(currentUser.uid).child(users.user_id).child("met_user_id").setValue(users.user_id)
                                            }

                                            val chatIntent = Intent(mContext, ChatActivity::class.java)
                                            chatIntent.putExtra("met_user_id", users.user_id)
                                            mContext.startActivity(chatIntent)
                                        }
                                    } else {
                                        val insuffienctPointsBottmSheetFragment = InsuffienctPointsBottmSheetFragment.newInstance("You need 20 coins to chat somebody new.", "")
                                        insuffienctPointsBottmSheetFragment.show(supportFragmentManager, "insuffienctPointsBottmSheetFragment")
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(TAG, "onCancelled: ${error.details}")
                            }

                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun sendChat(met_user_id: String) {
        var chat_id = databseReference.child("chat").push().key.toString()
        var dataMap: HashMap<String, Any> = HashMap<String, Any>()
        dataMap["sender_user_id"] = currentUser.uid
        dataMap["receiver_user_id"] = met_user_id
        dataMap["chat_id"] = chat_id
        dataMap["timestamp"] = System.currentTimeMillis()
        dataMap["is_paid"] = true
        dataMap["type"] = "empty_message"
        databseReference.child("chats").child(currentUser.uid).child(met_user_id).child(chat_id).setValue(dataMap).addOnSuccessListener {
            databseReference.child("chats").child(met_user_id).child(currentUser.uid).child(chat_id).setValue(dataMap)
            databseReference.child("recent_message").child(currentUser.uid).child(met_user_id).setValue(dataMap)
            databseReference.child("recent_message").child(met_user_id).child(currentUser.uid).setValue(dataMap)
        }.addOnFailureListener {
            Log.e(TAG, "sendChat: ", it)
            Toast.makeText(mContext, "Failed to send message. $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun showTwoButtonDialog(tile: String, message: String) {
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

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun sendToGetCoins() {
        mContext.startActivity(Intent(mContext, PointsActivity::class.java))
    }

    override fun getItemCount(): Int {
        return usersList.size
    }
}