package com.talk.walk.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Models.BlockedFriends
import com.talk.walk.R
import com.talk.walk.Utils.Constants

class BlockedFriendsAdapter(var mContext: Context, var blockedFriendList: MutableList<BlockedFriends>):
    RecyclerView.Adapter<BlockedFriendsAdapter.ViewHolder>() {

    private val TAG: String? = BlockedFriendsAdapter::class.java.name
    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvBlockedPersonName: TextView = itemView.findViewById(R.id.tvBlockedPersonName)
        var tvBlockedGender: TextView = itemView.findViewById(R.id.tvBlockedGender)
        var bBlockedUnBlock: Button = itemView.findViewById(R.id.bBlockedUnBlock)
    }

    fun setBlockedFriendsList(blockedFriendList: MutableList<BlockedFriends>) {
        this.blockedFriendList = blockedFriendList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_blocked_friends_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var blockedFriends = blockedFriendList[position]

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        databseReference.child(Constants.Keys.USERS).child(blockedFriends.met_user_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var username: String = snapshot.child(Constants.Keys.USERNAME).value.toString()
                    var gender: String = snapshot.child(Constants.Keys.GENDER).value.toString()
                    holder.tvBlockedGender.text = gender
                    holder.tvBlockedPersonName.text = username
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.details}")
            }
        })

        holder.bBlockedUnBlock.setOnClickListener {
            databseReference.child(Constants.Keys.BLOCKS).child(blockedFriends.user_id).child(blockedFriends.met_user_id).setValue(null).addOnSuccessListener {
                databseReference.child(Constants.Keys.BLOCKS).child(blockedFriends.met_user_id).child(blockedFriends.user_id).setValue(null)
                Toast.makeText(mContext, "UnBlocked Successfully", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return blockedFriendList.size
    }
}