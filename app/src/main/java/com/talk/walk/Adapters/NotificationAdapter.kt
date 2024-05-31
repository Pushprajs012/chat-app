package com.talk.walk.Adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
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
import com.talk.walk.Models.Notifications
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(var mContext: Context, var notificationList: MutableList<Notifications>):
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private val TAG: String? = NotificationAdapter::class.java.name
    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ibAccept: Button = itemView.findViewById(R.id.ibAccept)
        val ibReject: Button = itemView.findViewById(R.id.ibReject)
        val tvPersonName: TextView = itemView.findViewById(R.id.tvPersonName)
        val tvPersonGender: TextView = itemView.findViewById(R.id.tvPersonGender)
        val tvFriendRequestAccepted: TextView = itemView.findViewById(R.id.tvFriendRequestAccepted)
        val civNotificationProfilePic: CircleImageView = itemView.findViewById(R.id.civNotificationProfilePic)
        val cvProfileBG: CardView = itemView.findViewById(R.id.cvProfileBG)
    }

    @JvmName("setNotificationList1")
    fun setNotificationList(notificationList: MutableList<Notifications>) {
        this.notificationList = notificationList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_friend_request_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var notifications = notificationList[position]

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        when (notifications.type) {
            Constants.Keys.FRIEND_REQUESTS -> {
                holder.tvFriendRequestAccepted.visibility = View.GONE
                holder.ibAccept.setOnClickListener {
                    var dataMap: HashMap<String, Any> = HashMap<String, Any>()
                    dataMap["user_id"] = notifications.user_id
                    dataMap["met_user_id"] = notifications.met_user_id
                    dataMap["timestamp"] = System.currentTimeMillis()
                    dataMap["hasUserRead"] = false
                    databseReference.child(Constants.Keys.FRIENDS).child(notifications.user_id).child(notifications.met_user_id).setValue(dataMap).addOnSuccessListener {
                        databseReference.child(Constants.Keys.FRIENDS).child(notifications.met_user_id).child(notifications.user_id).setValue(dataMap)
                        databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(notifications.user_id).child(notifications.met_user_id).setValue(null)
                        databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(notifications.met_user_id).child(notifications.user_id).setValue(null)
                        databseReference.child(Constants.Keys.NOTIFICATIONS).child(notifications.met_user_id).child(Constants.Keys.TYPE).setValue(Constants.Keys.FRIEND_REQUEST_ACCEPTED)
                        holder.ibAccept.visibility = View.GONE
                        holder.ibReject.visibility = View.GONE
                        holder.tvFriendRequestAccepted.visibility = View.VISIBLE
                    }
                }

                holder.ibReject.setOnClickListener {
                    databseReference.child(Constants.Keys.NOTIFICATIONS).child(notifications.met_user_id).child(Constants.Keys.TYPE).setValue(Constants.Keys.FRIEND_REQUEST_REJECTED).addOnSuccessListener {
                        databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(notifications.user_id).child(notifications.met_user_id).setValue(null)
                        databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(notifications.met_user_id).child(notifications.user_id).setValue(null)
                        holder.ibAccept.visibility = View.GONE
                        holder.ibReject.visibility = View.GONE
                        holder.tvFriendRequestAccepted.visibility = View.VISIBLE
                        holder.tvFriendRequestAccepted.text = "Friend Request Rejected"
                    }
                }
            }
            Constants.Keys.FRIEND_REQUEST_ACCEPTED -> {
                holder.ibAccept.visibility = View.GONE
                holder.ibReject.visibility = View.GONE
                holder.tvFriendRequestAccepted.visibility = View.VISIBLE
            }
            Constants.Keys.FRIEND_REQUEST_REJECTED -> {
                holder.ibAccept.visibility = View.GONE
                holder.ibReject.visibility = View.GONE
                holder.tvFriendRequestAccepted.visibility = View.VISIBLE
                holder.tvFriendRequestAccepted.text = "Friend Request Rejected"
            }
        }

        databseReference.child(Constants.Keys.USERS).child(notifications.user_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("username").value.toString()
                    val gender = snapshot.child("gender").value.toString()
                    if (snapshot.child(Constants.Keys.PROFILE_IMAGE).exists()) {
                        Glide.with(mContext).load(snapshot.child(Constants.Keys.PROFILE_IMAGE).value.toString()).into(holder.civNotificationProfilePic)
                    } else {
                        holder.civNotificationProfilePic.setImageDrawable(
                            AppCompatResources.getDrawable(
                                mContext,
                                R.drawable.user
                            )
                        )
                        holder.cvProfileBG.setCardBackgroundColor(Controller.getRandomColor())
                        var layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                        layoutParams.leftMargin = 16
                        layoutParams.rightMargin = 16
                        layoutParams.topMargin = 16
                        layoutParams.bottomMargin = 16
                        holder.civNotificationProfilePic.layoutParams = layoutParams
                        holder.civNotificationProfilePic.setColorFilter(Color.WHITE)
                    }
                    if (notifications.type == Constants.Keys.FRIEND_REQUEST_ACCEPTED) {
                        holder.tvPersonName.text = "$username"
                    } else {
                        holder.tvPersonName.text = "$username has sent you friend request"
                    }
                    holder.tvPersonGender.text = gender
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.details}")
            }

        })
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}