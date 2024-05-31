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
import com.talk.walk.Adapters.BlockedFriendsAdapter
import com.talk.walk.Models.BlockedFriends
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller

class BlockFriendsActivity : AppCompatActivity() {


    private val TAG: String? = BlockFriendsActivity::class.java.name

    private lateinit var mContext: Context

    private lateinit var ibBack: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var tvNoBlockFriends: TextView
    private var blockedFriendsList: MutableList<BlockedFriends> = mutableListOf()
    private lateinit var blockedFriendsAdapter: BlockedFriendsAdapter


    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private lateinit var rvBlockedFriends: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_friends)

        mContext = this

        ibBack = findViewById(R.id.ibBack)
        rvBlockedFriends = findViewById(R.id.rvBlockedFriends)
        tvNoBlockFriends = findViewById(R.id.tvNoBlockFriends)

        tvNoBlockFriends.visibility = View.VISIBLE

        auth = Firebase.auth
        if (auth.currentUser == null) {
            finish()
        } else {
            blockedFriendsAdapter = BlockedFriendsAdapter(mContext, blockedFriendsList)
            rvBlockedFriends.layoutManager = LinearLayoutManager(mContext)
            rvBlockedFriends.adapter = blockedFriendsAdapter

            currentUser = auth.currentUser!!
            databseReference.child(Constants.Keys.BLOCKS).child(currentUser.uid).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        tvNoBlockFriends.visibility = View.GONE
                        var blockedFriends = snapshot.getValue<BlockedFriends>()
                        if (blockedFriends != null) {
                            if (blockedFriends.user_id == currentUser.uid) {
                                blockedFriends?.let { blockedFriendsList.add(it) }
                            }
                        }
                    } else {
                        tvNoBlockFriends.visibility = View.VISIBLE
                    }
                    blockedFriendsAdapter.setBlockedFriendsList(blockedFriendsList)
                    blockedFriendsAdapter.notifyDataSetChanged()
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {


                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    var key = snapshot.key.toString()
                    for (blockedFriends in blockedFriendsList) {
                        if (blockedFriends.met_user_id == key) {
                            blockedFriendsList.remove(blockedFriends)
                            blockedFriendsAdapter.notifyDataSetChanged()
                            break
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })
        }

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
    }
}