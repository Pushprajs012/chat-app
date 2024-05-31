package com.talk.walk.Activities
//chat app
//chat time
import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Adapters.BottomViewPagerAdapter
import com.talk.walk.Fragments.ChatFragment
import com.talk.walk.Models.MainMenuItems
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import android.graphics.Color
import android.view.*

import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.material.badge.BadgeDrawable
import com.google.firebase.database.*
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.installations.FirebaseInstallations
import com.talk.walk.BuildConfig
import com.talk.walk.Fragments.ChatFragment2
import com.talk.walk.Fragments.MoreFragment
import com.talk.walk.Fragments.SearchFragment


class MainActivity : AppCompatActivity() {

    private val TAG: String? = MainActivity::class.java.name
    private lateinit var mContext: Context

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var vpMain: ViewPager2
    private lateinit var tvPointsCounter: TextView
    private lateinit var tvMainHeader: TextView
    private lateinit var dlMain: DrawerLayout
    private lateinit var cvPoints: CardView
    private lateinit var flMain: FrameLayout
    private lateinit var cvMainInvitePeople: CardView

    private var mainMenuItemList: MutableList<MainMenuItems> = mutableListOf<MainMenuItems>()

    private lateinit var chatFragment: ChatFragment
    private lateinit var chatFragment2: ChatFragment2
    private lateinit var searchFragment: SearchFragment
    private lateinit var moreFragment: MoreFragment

    private lateinit var bottomViewPagerAdapter: BottomViewPagerAdapter

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser
    private var username = ""
    private var notificationCounter: Int = 0
    private var friendBadgeCounter: Int = 0
    private lateinit var badgeDrawableMore: BadgeDrawable
    private lateinit var badgeDrawableFriends: BadgeDrawable
//test

    var deepLink: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        mContext = this
        Controller.mainActivity = this


        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        vpMain = findViewById(R.id.vpMain)
        tvPointsCounter = findViewById(R.id.tvPointsCounter)
        tvMainHeader = findViewById(R.id.tvMainHeader)
        dlMain = findViewById(R.id.dlMain)
        cvPoints = findViewById(R.id.cvPoints)
        flMain = findViewById(R.id.flMain)
        cvMainInvitePeople = findViewById(R.id.cvMainInvitePeople)

        bottomViewPagerAdapter = BottomViewPagerAdapter(this)
        vpMain.adapter = bottomViewPagerAdapter
        vpMain.setCurrentItem(0, false)
        vpMain.isUserInputEnabled = false
//        vpMain.offscreenPageLimit = 3

        auth = Firebase.auth

        if (auth.currentUser == null) {
            Toast.makeText(mContext, "Please login again", Toast.LENGTH_LONG).show()
            finish()
        } else {
            currentUser = auth.currentUser!!
            databseReference.child("constants").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Controller.ADMOB_BANNER_ID = snapshot.child("banner_admob_id").value.toString()
                        Controller.ADMOB_INTERSTITAL_ID = snapshot.child("interstitial_admob_id").value.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
            Firebase.dynamicLinks
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link
                        Log.e(TAG, "pendingDynamicLinkData onCreate: $deepLink")
                    }

                    // Handle the deep link. For example, open the linked
                    // content, or apply promotional credit to the user's
                    // account.
                    // ...

                    // ...
                }
                .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }

            Log.e(TAG, "onCreate: " + deepLink.toString())
            databseReference.child("users").child(currentUser.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.child("points").exists()) {
                                username = snapshot.child("username").value.toString()
                                val points = snapshot.child("points").getValue() as Long
                                tvPointsCounter.text = points.toString()

                                val token_id = FirebaseInstanceId.getInstance().token
                                databseReference.child("users")
                                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .child("device_token").setValue(token_id)
                            }
                            Log.d(TAG, "onDataChange: $snapshot")
                        } else {
//                        Toast.makeText(mContext, "User details are empty", Toast.LENGTH_LONG).show()
                            finish()
                        }


                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled: ${error.details}")
                    }

                })

            FirebaseInstallations.getInstance().id.addOnCompleteListener { task: Task<String?> ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.e("token ---->>", token!!)
                    databseReference.child("device_blocked").child(token).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                Controller.showTwoButtonDialog(mContext, "Blocked", "You are not authorizes to use this app.", true)
                            } else {
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
                }
            }

            vpMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val menu: Menu = bottomNavigationView.menu
                    if (position == 0) {
                        tvMainHeader.text = "Chats"
//                        menu.findItem(R.id.chat_bottom_menu_item).seticon
                    } else if (position == 1) {
                        tvMainHeader.text = "Friends"
                        bottomNavigationView.removeBadge(R.id.search_bottom_menu_litem)
                    } else if (position == 2) {
                        tvMainHeader.text = "More"
                        bottomNavigationView.removeBadge(R.id.more_bottom_menu_item)
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                }
            })
            getNotifications()

            cvMainInvitePeople.setOnClickListener {
                databseReference.child("constants")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.child("base_url").exists()) {
                                    var base_url = snapshot.child("base_url").value.toString()
                                    val user = Firebase.auth.currentUser!!
                                    val uid = user.uid
                                    val invitationLink =
                                        "${base_url + Constants.Urls.REFERRAL_URL}=$uid"
                                    Firebase.dynamicLinks.shortLinkAsync {
                                        link = Uri.parse(invitationLink)
                                        domainUriPrefix = base_url + Constants.Urls.REFERRAL_URL
                                        androidParameters(mContext.packageName) {
                                            minimumVersion = BuildConfig.VERSION_CODE
                                        }
                                    }.addOnSuccessListener { shortDynamicLink ->
                                        var mInvitationUrl = shortDynamicLink.shortLink

                                        val subject = String.format(
                                            "%s wants you to join ${getString(R.string.app_name)}.\n%s",
                                            username,
                                            mInvitationUrl.toString()
                                        )
                                        val invitationLink = mInvitationUrl.toString()
                                        val msg =
                                            "Let's play MyExampleGame together! Use my referrer link: $invitationLink"
                                        val msgHtml = String.format(
                                            "<p>Let's play MyExampleGame together! Use my " +
                                                    "<a href=\"%s\">referrer link</a>!</p>",
                                            invitationLink
                                        )

                                        val sendIntent = Intent()
                                        sendIntent.action = Intent.ACTION_SEND
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, subject)
                                        sendIntent.type = "text/plain"
                                        startActivity(sendIntent)
                                    }.addOnFailureListener {
                                        Log.e(TAG, "onItemClick: ${it.localizedMessage}")
                                        showTwoButtonDialog("Error", it.message.toString())

                                    }
                                } else {
                                    Toast.makeText(mContext, "Invite url failed", Toast.LENGTH_LONG)
                                        .show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }
        }

        chatFragment = ChatFragment()
        chatFragment2 = ChatFragment2()
        searchFragment = SearchFragment()
        moreFragment = MoreFragment()

        val fragmentManager = supportFragmentManager
        val active = arrayOf<Fragment>(chatFragment)

        fragmentManager.beginTransaction().add(R.id.flMain, chatFragment2, "1").commit()
        fragmentManager.beginTransaction().add(R.id.flMain, searchFragment, "2")
            .hide(searchFragment).commit()
        fragmentManager.beginTransaction().add(R.id.flMain, moreFragment, "3")
            .hide(moreFragment).commit()

        fragmentManager.beginTransaction().hide(active[0]).show(chatFragment2).commit()
        active[0] = chatFragment2

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.chat_bottom_menu_item -> {
                    cvMainInvitePeople.visibility = View.VISIBLE
                    fragmentManager.beginTransaction().hide(active[0]).show(chatFragment2).commit()
                    active[0] = chatFragment
                    vpMain.setCurrentItem(0, false)
                }
                R.id.search_bottom_menu_litem -> {
                    cvMainInvitePeople.visibility = View.VISIBLE
                    fragmentManager.beginTransaction().hide(active[0]).show(searchFragment).commit()
                    active[0] = searchFragment
                    vpMain.setCurrentItem(1, false)
                }
                R.id.more_bottom_menu_item -> {
                    cvMainInvitePeople.visibility = View.GONE
                    fragmentManager.beginTransaction().hide(active[0]).show(moreFragment).commit()
                    active[0] = moreFragment
                    vpMain.setCurrentItem(2, false)
                }
            }
            true
        }

        cvPoints.setOnClickListener {
            startActivity(Intent(mContext, PointsActivity::class.java))
        }

        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = Controller.ADMOB_BANNER_ID
        MobileAds.initialize(this) {}
        val mAdView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        Log.e(TAG, "onCreate: " + Controller.isDarkTheme(this))
        if (Controller.isDarkTheme(this)) {
            tvPointsCounter.setTextColor(Color.WHITE)
        } else {
            tvPointsCounter.setTextColor(Color.BLACK)
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

    private fun getNotifications() {
        notificationCounter = 0
        databseReference.child(Constants.Keys.NOTIFICATIONS).addChildEventListener(object :
            ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child(Constants.Keys.MET_USER_ID).value.toString() == currentUser.uid) {
                    if (snapshot.child("type").value.toString() == Constants.Keys.FRIEND_REQUESTS) {
                        if (snapshot.child("is_read").exists() && !(snapshot.child("is_read").value as Boolean)) {
                            notificationCounter++
                            bottomNavigationView.getOrCreateBadge(R.id.more_bottom_menu_item).number =
                                notificationCounter
                        }


                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child("type").value.toString() == Constants.Keys.FRIEND_REQUEST_ACCEPTED || snapshot.child(
                        "type"
                    ).value.toString() == Constants.Keys.FRIEND_REQUEST_REJECTED
                ) {
                    notificationCounter--
                    if (notificationCounter == 0 || notificationCounter < 0) {
                        bottomNavigationView.removeBadge(R.id.more_bottom_menu_item)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.details}")
            }

        })
        databseReference.child(Constants.Keys.FRIENDS).child(currentUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (i in snapshot.children.iterator()) {
                            i.key?.let {
                                databseReference.child(Constants.Keys.FRIENDS)
                                    .child(currentUser.uid).child(
                                    it
                                ).addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            if (snapshot.child("hasUserRead").exists()) {
                                                var hasUserRead =
                                                    snapshot.child("hasUserRead").value as Boolean
                                                if (!hasUserRead) {
                                                    friendBadgeCounter++
                                                    bottomNavigationView.getOrCreateBadge(R.id.search_bottom_menu_litem).number =
                                                        friendBadgeCounter

                                                } else {
//                                                    badgeDrawableFriends.setVisible(false)
                                                }
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e(TAG, "onCancelled: ${error.details}")
                                    }

                                })
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })
    }

    companion object {
        public fun refreshActivity(activity: Activity) {
            activity.finish();
            activity.overridePendingTransition(0, 0);
            activity.startActivity(activity.getIntent());
            activity.overridePendingTransition(0, 0);
        }
    }


    override fun onStart() {
        super.onStart()

//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    override fun onStop() {
        super.onStop()

            println("ssssssssssssssssssss")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Ddddddddddddddddddddddddddddddddd")

    }
}
