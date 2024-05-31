package com.talk.walk.Activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
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
import com.talk.walk.Adapters.SearchAdapter
import com.talk.walk.Models.Users
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import android.widget.TextView.OnEditorActionListener


class SearchActivity : AppCompatActivity() {

    private val TAG: String? = SearchActivity::class.java.name

    private lateinit var mContext: Context

    private lateinit var ibSearchBack: ImageButton
    private lateinit var ibSearchClear: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var rvSearch: RecyclerView
    private lateinit var tvSearchNoUserFound: TextView

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private lateinit var searchAdapter: SearchAdapter
    private var searchList: MutableList<Users> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        mContext = this

        ibSearchBack = findViewById(R.id.ibSearchBack)
        ibSearchClear = findViewById(R.id.ibSearchClear)
        etSearch = findViewById(R.id.etSearch)
        rvSearch = findViewById(R.id.rvSearch)
        tvSearchNoUserFound = findViewById(R.id.tvSearchNoUserFound)

        tvSearchNoUserFound.visibility = View.GONE

        auth = Firebase.auth
        if (auth.currentUser != null) {
            currentUser = auth.currentUser!!
            searchAdapter = SearchAdapter(mContext, searchList, supportFragmentManager)
            rvSearch.layoutManager = LinearLayoutManager(mContext)
            rvSearch.adapter = searchAdapter

            etSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val search_value = etSearch.text.toString()
                    if (search_value.isEmpty()) {
                        Toast.makeText(mContext, "Cannot search empty", Toast.LENGTH_LONG).show()
                    } else {
                        searchList.clear()
                        searchAdapter.notifyDataSetChanged()
                        search(search_value)
                    }
                    true
                } else false
            })



        }



        if (Controller.isDarkTheme(this)) {
            etSearch.setBackgroundResource(R.drawable.dark_mode_edittext_bg)
        } else {
            etSearch.setBackgroundResource(R.drawable.rounded_gray_bg)
        }

        etSearch.requestFocus()
        Controller.showKeyboard(this)

        ibSearchBack.setOnClickListener {
            onBackPressed()
        }

        ibSearchClear.setOnClickListener {
            etSearch.setText("")
        }

        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = Controller.ADMOB_BANNER_ID
        MobileAds.initialize(this) {}
        val mAdView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun search(p0: String) {
        databseReference.child(Constants.Keys.USERS).orderByChild("username").startAt(p0).endAt(p0+"\uf8ff").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()) {
                    tvSearchNoUserFound.visibility = View.GONE
                    var search = snapshot.getValue<Users>()
                    if (search != null) {
                        searchList.add(search)
                    }

                    searchAdapter.setSearchList(searchList)
                    searchAdapter.notifyDataSetChanged()
                } else {
                    tvSearchNoUserFound.visibility = View.VISIBLE
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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    override fun onResume() {
        super.onResume()

    }
}