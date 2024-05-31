package com.talk.walk.Adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
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
import com.talk.walk.Models.MainMenuItems
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import de.hdodenhof.circleimageview.CircleImageView

class DrawerAdapter(
    var mContext: Context,
    var mainMenuItemsList: MutableList<MainMenuItems>,
    var activity: FragmentActivity?,
    val onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val LINE_BREAKER_VIEW: Int = 2
    private val TAG: String? = DrawerAdapter::class.java.name
    private val MENU_ITEM_VIEW: Int = 1
    private val NAV_HEADER_VIEW: Int = 0

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private var counter: Int = 0
    private var notificationCounter: Int = 0

    fun setMainMenuItemList(mainMenuItemsList: MutableList<MainMenuItems>) {
        this.mainMenuItemsList = mainMenuItemsList
        notifyDataSetChanged()
    }

    fun setNotificationCounter(notificationCounter: Int) {
        this.notificationCounter = notificationCounter
        notifyDataSetChanged()
    }


    inner class NavHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDrawerProfilePic: CircleImageView = itemView.findViewById(R.id.ivDrawerProfilePic)
        val tvDrawerPersonName: TextView = itemView.findViewById(R.id.tvDrawerPersonName)
        val bDrawerEditProfile: Button = itemView.findViewById(R.id.bDrawerEditProfile)
        val tvBuilderName: TextView = itemView.findViewById(R.id.tvBuilderName)

    }

    inner class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDrawerIcon: ImageView = itemView.findViewById(R.id.ivDrawerIcon)
        val tvDrawerTitle: TextView = itemView.findViewById(R.id.tvDrawerTitle)
        val tvCounter: TextView = itemView.findViewById(R.id.tvCounter)
        val cvCounter: CardView = itemView.findViewById(R.id.cvCounter)
    }

    inner class LineBreakerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vDrawerLineBreaker: View = itemView.findViewById(R.id.vDrawerLineBreaker)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var mainMenuItems = mainMenuItemsList[position]

        val itemViewType: Int = getItemViewType(position)
        auth = Firebase.auth
        if (auth.currentUser != null) {
            currentUser = auth.currentUser!!
            if (itemViewType == MENU_ITEM_VIEW) {
                val holder = (holder as MenuItemViewHolder)
                Glide.with(mContext).load(mainMenuItems.image).into(holder.ivDrawerIcon)
                if (mainMenuItems.title != mContext.resources.getString(R.string.get_coins)) {
                    if (activity?.let { Controller.isDarkTheme(it) } == true) {
                        holder.ivDrawerIcon.setColorFilter(Color.WHITE)
                    } else {
                        holder.ivDrawerIcon.setColorFilter(Color.BLACK)
                    }
                }
                holder.tvDrawerTitle.text = mainMenuItems.title
                holder.itemView.setOnClickListener {
                    onItemClickListener.onItemClick(mainMenuItems)
                }
                if (mainMenuItems.title == mContext.getString(R.string.notifications)) {
                    getNotificationCount(holder, mainMenuItems)
                }
                if (notificationCounter <= 0 && mainMenuItems.title == mContext.resources.getString(
                        R.string.notifications
                    )) {
                    holder.cvCounter.visibility = View.GONE
                } else if (notificationCounter >= 0 && mainMenuItems.title == mContext.resources.getString(
                        R.string.notifications
                    )) {
                    holder.cvCounter.visibility = View.VISIBLE
                    holder.tvCounter.text = notificationCounter.toString()
                } else {
                    holder.cvCounter.visibility = View.GONE
                }
            }
        }
//        if (itemViewType == NAV_HEADER_VIEW) {
//            val holder = (holder as NavHeaderViewHolder)
//
//            holder.tvBuilderName.text = currentUser.uid
//            holder.tvBuilderName.visibility = View.GONE
//            databseReference.child(Constants.Keys.USERS).child(currentUser.uid)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.exists()) {
//                            val username =
//                                snapshot.child(Constants.Keys.USERNAME).value.toString()
//                            holder.tvDrawerPersonName.text = "@$username"
//                            if (snapshot.child(Constants.Keys.PROFILE_IMAGE).exists()) {
//                                Glide.with(mContext)
//                                    .load(snapshot.child(Constants.Keys.PROFILE_IMAGE).value.toString())
//                                    .into(holder.ivDrawerProfilePic)
//                            } else {
//                                Glide.with(mContext)
//                                    .load(R.drawable.user).fitCenter()
//                                    .into(holder.ivDrawerProfilePic)
//                            }
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.e(TAG, "onCancelled: ${error.details}")
//                    }
//
//                })
//            holder.bDrawerEditProfile.setOnClickListener {
//                val editProfileIntent = Intent(mContext, EditProfileActivity::class.java)
//                mContext.startActivity(editProfileIntent)
//            }
//        }

    }

    private fun getNotificationCount(
        holder: MenuItemViewHolder,
        mainMenuItems: MainMenuItems
    ) {
        databseReference.child(Constants.Keys.NOTIFICATIONS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child(Constants.Keys.MET_USER_ID).value.toString() == currentUser.uid) {
                            holder.cvCounter.visibility = View.VISIBLE
                            counter++
                            holder.tvCounter.text = counter.toString()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })
    }

    override fun getItemViewType(position: Int): Int {
        var mainMenuItems = mainMenuItemsList[position]
        return MENU_ITEM_VIEW

    }

    override fun getItemCount(): Int {
        return mainMenuItemsList.size
    }

    interface OnItemClickListener {
        fun onItemClick(mainMenuItems: MainMenuItems)
    }
}