package com.talk.walk.Activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Adapters.SelectMatchPeopleAdapter
import com.talk.walk.Adapters.SelectMatchPeopleAdapter.DeviceClickListener
import com.talk.walk.Fragments.InsuffienctPointsBottmSheetFragment
import com.talk.walk.Models.SelectPeople
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller

class MatchPeopleActivity : AppCompatActivity(), DeviceClickListener {

    private val TAG: String? = MatchPeopleActivity::class.java.name
    private lateinit var mContext: Context

    private lateinit var rvMatchPeopleSelect: RecyclerView
    private lateinit var bStartChat: Button
    private lateinit var ibBack: ImageButton
    private lateinit var tvPointsCounter: TextView
    private lateinit var switchMatchOnline: Switch

    private lateinit var selectMatchPeopleAdapter: SelectMatchPeopleAdapter
    private var selectPeopleList: List<SelectPeople> = listOf()

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private var selectPeople: SelectPeople = SelectPeople()
    private var profilePoints: Long = 0
    private var matchOnlyOnline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_people)

        mContext = this

        rvMatchPeopleSelect = findViewById(R.id.rvMatchPeopleSelect)
        bStartChat = findViewById(R.id.bStartChat)
        ibBack = findViewById(R.id.ibBack)
        tvPointsCounter = findViewById(R.id.tvPointsCounter)
        switchMatchOnline = findViewById(R.id.switchMatchOnline)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        if (currentUser == null) {
            Toast.makeText(mContext, "Please login again", Toast.LENGTH_LONG).show()
            finish()
        } else {
            databseReference.child("users").child(currentUser.uid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val points = snapshot.child("points").getValue() as Long
                        profilePoints = points
                        tvPointsCounter.text = profilePoints.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }
            })
            if (Controller.isDarkTheme(this)) {
            } else {
            }
        }

        switchMatchOnline.setOnCheckedChangeListener { p0, p1 -> matchOnlyOnline = p1 }

        selectPeopleList = listOf(
            SelectPeople("Male", R.drawable.man, false, 10),
            SelectPeople("Anyone", R.drawable.mother, true, 0),
            SelectPeople("Female", R.drawable.woman, false, 10)
        )

        selectMatchPeopleAdapter = SelectMatchPeopleAdapter(mContext, selectPeopleList, this)
        rvMatchPeopleSelect.layoutManager = GridLayoutManager(mContext, 3)
        rvMatchPeopleSelect.adapter = selectMatchPeopleAdapter

        bStartChat.setOnClickListener {
            databseReference.child("chats").child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val count = snapshot.childrenCount as Long
                        if (count > 10L) {

                            val builder = AlertDialog.Builder(mContext)
                                .setTitle("Limit Exceeded")
                                .setMessage("Your chat limit exceeded. Delete one of your conversations to chat again")
                                .setPositiveButton("Okay", object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                            p0?.dismiss()
                                    }
                                })

                            builder.show()
                        } else {
                            sendToMatchingPeople()
                        }
                    } else {
                        sendToMatchingPeople()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }

        tvPointsCounter.setOnClickListener {
            startActivity(Intent(mContext, PointsActivity::class.java))
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

    private fun sendToMatchingPeople() {
        if (selectPeople.name.isEmpty()) {

            selectPeople = SelectPeople("Anyone", R.drawable.mother, true, 0)
        }
        if (selectPeople.name.isNotEmpty()) {

            if (profilePoints == 0L && selectPeople.name != Constants.Keys.ANYONE) {
                val insuffienctPointsBottmSheetFragment = InsuffienctPointsBottmSheetFragment.newInstance("You need 10 coins to match new people.", "")
                insuffienctPointsBottmSheetFragment.show(supportFragmentManager, "insuffienctPointsBottmSheetFragment")
            } else {
                val matchingPeopleIntent = Intent(mContext, MatchingPeopleActivity::class.java)
                matchingPeopleIntent.putExtra("gender", selectPeople.name)
                matchingPeopleIntent.putExtra("profile_points", profilePoints)
                matchingPeopleIntent.putExtra("matchOnlyOnline", matchOnlyOnline)
                startActivity(matchingPeopleIntent)
                finish()
            }
        } else {
            Toast.makeText(mContext, "Please select option", Toast.LENGTH_LONG).show()

        }
    }

    override fun onDeviceClick(int: Int, selectPeople: SelectPeople) {
        this.selectPeople = selectPeople
    }

    override fun onStart() {
        super.onStart()
        if (!currentUser.isAnonymous) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_LONG).show()
            auth.signOut()
            finish()
        } else {
            databseReference.child("users").child(currentUser!!.uid).child("is_online").setValue(true)
        }
    }

    override fun onStop() {
        super.onStop()
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                databseReference.child("users").child(currentUser!!.uid).child("is_online").setValue(false)
            }, 3500)
        }
    }
}