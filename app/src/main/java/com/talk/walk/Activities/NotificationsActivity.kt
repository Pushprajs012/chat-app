package com.talk.walk.Activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.talk.walk.Adapters.ChatMoreAdapter
import com.talk.walk.Adapters.NotificationAdapter
import com.talk.walk.Models.Notifications
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller

class NotificationsActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private lateinit var ibBack: ImageButton

    private lateinit var rvNotifications: RecyclerView
    private var notificationList: MutableList<Notifications> = mutableListOf()
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var tvNoNotifications: TextView

    private val TAG: String? = ChatMoreAdapter::class.java.name
    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        mContext = this

        auth = Firebase.auth
        if (auth.currentUser == null) {
            finish()
        } else {
            currentUser = auth.currentUser!!

            rvNotifications = findViewById(R.id.rvNotifications)

            notificationAdapter = NotificationAdapter(mContext, notificationList)
            rvNotifications.layoutManager = LinearLayoutManager(mContext)
            rvNotifications.adapter = notificationAdapter

        }

        ibBack = findViewById(R.id.ibBack)
        tvNoNotifications = findViewById(R.id.tvNoNotifications)

        tvNoNotifications.visibility = View.VISIBLE

        ibBack.setOnClickListener {
            onBackPressed()
        }

        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = Controller.ADMOB_BANNER_ID
        MobileAds.initialize(this) {}
        val mAdView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

//        Log.e(TAG, "onCreate: " + Controller.isDarkTheme(this))
//        if (Controller.isDarkTheme(this)) {
//            supportActionBar?.setBackgroundDrawable(
//                ColorDrawable(
//                    resources
//                .getColor(R.color.black2))
//            )
//        } else {
//            ?.setBackgroundDrawable(
//                ColorDrawable(
//                    resources
//                        .getColor(R.color.white2))
//            )
//        }
    }

    private fun getNotifications() {
        notificationList.clear()
        notificationAdapter.notifyDataSetChanged()
        databseReference.child(Constants.Keys.NOTIFICATIONS).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()) {
                    if (snapshot.child("met_user_id").value.toString() == currentUser.uid) {
                        tvNoNotifications.visibility = View.GONE
                        var notifications = snapshot.getValue<Notifications>()
                        if (notifications != null) {
                            if (notifications.type != null) {
                                if (notifications.type != "friend_request_cancelled") {
                                    notifications.let { notificationList.add(it) }
                                }
                            }
                        } else {
                            tvNoNotifications.visibility = View.GONE
                        }
                    } else {
                        tvNoNotifications.visibility = View.GONE
                    }
                    notificationAdapter.setNotificationList(notificationList)
                    notificationAdapter.notifyDataSetChanged()
                } else {
                    tvNoNotifications.visibility = View.VISIBLE
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.details}")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        getNotifications()
    }
}