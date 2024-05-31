package com.talk.walk.Adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Activities.ChatActivity
import com.talk.walk.Models.Friends
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import de.hdodenhof.circleimageview.CircleImageView

class FriendsAdapter(var mContext: Context, var friendList: MutableList<Friends>):
    RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    private val TAG: String? = FriendsAdapter::class.java.name
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
        var bUnFriend: Button = itemView.findViewById(R.id.bUnFriend)
    }

    fun setFriendsList(friendList: MutableList<Friends>) {
        this.friendList = friendList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_friend_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var friends = friendList[position]
        holder.bUnFriend.visibility = View.VISIBLE
        var username = ""
        auth = Firebase.auth
        if (auth.currentUser != null) {
            currentUser = auth.currentUser!!
            val databaseRef: DatabaseReference = if (friends.met_user_id == currentUser.uid) {
                databseReference.child(Constants.Keys.USERS).child(friends.user_id)
            } else {
                databseReference.child(Constants.Keys.USERS).child(friends.met_user_id)
            }
            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        username = snapshot.child(Constants.Keys.USERNAME).value.toString()
                        holder.tvFriendName.text = username
                        holder.tvFriendGender.text = snapshot.child(Constants.Keys.GENDER).value.toString()
                        if (snapshot.child(Constants.Keys.PROFILE_IMAGE).exists()) {
                            Glide.with(mContext).load(snapshot.child(Constants.Keys.PROFILE_IMAGE).value.toString()).into(holder.civFriendProfilePic)
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
                    } else {
                        Glide.with(mContext).load(R.drawable.user).fitCenter().apply(RequestOptions()).into(holder.civFriendProfilePic)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })

            holder.cvFriends.setOnClickListener {
                val chatIntent = Intent(mContext, ChatActivity::class.java)
                if (currentUser.uid == friends.met_user_id) {
                    chatIntent.putExtra("met_user_id", friends.user_id)
                } else {
                    chatIntent.putExtra("met_user_id", friends.met_user_id)
                }
                mContext.startActivity(chatIntent)
            }

            holder.bUnFriend.setOnClickListener {
                showTwoButtonDialog("Unfollow $username?", "If you change our mind, you'll have to request $username again.", holder, friends)
            }
        }
        if (!friends.hasUserRead) {
            if (currentUser.uid == friends.user_id) {
                databseReference.child(Constants.Keys.FRIENDS).child(friends.user_id).child(friends.met_user_id).child("hasUserRead").setValue(true)
            } else {
                databseReference.child(Constants.Keys.FRIENDS).child(friends.met_user_id).child(friends.user_id).child("hasUserRead").setValue(true)
            }
            holder.cvFriends.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorLightGreen2))
        } else {
            holder.cvFriends.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.white))
        }

    }

    private fun showTwoButtonDialog(
        tile: String,
        message: String,
        holder: ViewHolder,
        friends: Friends
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

        tvDeleteCancel.visibility = View.VISIBLE

        tvDeleteOkay.setOnClickListener {
            databseReference.child(Constants.Keys.FRIENDS).child(friends.user_id).child(friends.met_user_id).setValue(null).addOnSuccessListener {
                databseReference.child(Constants.Keys.FRIENDS).child(friends.met_user_id).child(friends.user_id).setValue(null)
                databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(friends.met_user_id).child(friends.user_id).setValue(null)
                databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(friends.user_id).child(friends.met_user_id).setValue(null)
                friendList.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                notifyItemRangeRemoved(holder.adapterPosition, friendList.size)
                dialog.dismiss()
            }
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}
