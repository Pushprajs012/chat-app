package com.talk.walk.Fragments

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.talk.walk.Activities.SearchActivity
import com.talk.walk.Adapters.FriendsAdapter
import com.talk.walk.Models.Friends
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mContext: Context

    private lateinit var rvFriends: RecyclerView
    private lateinit var srlSearch: SwipeRefreshLayout
    private lateinit var etPlaceboSearch: EditText
    private lateinit var tvNoFriends: TextView

    private lateinit var friendsAdapter: FriendsAdapter
    private var friendList: MutableList<Friends> = mutableListOf()

    private val TAG: String? = SearchFragment::class.java.name
    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = view.context

        rvFriends = view.findViewById(R.id.rvFriends)
        srlSearch = view.findViewById(R.id.srlSearch)
        etPlaceboSearch = view.findViewById(R.id.etPlaceboSearch)
        tvNoFriends = view.findViewById(R.id.tvNoFriends)

        tvNoFriends.visibility = View.VISIBLE

        friendsAdapter = FriendsAdapter(mContext, friendList)
        rvFriends.layoutManager = LinearLayoutManager(mContext)
        rvFriends.adapter = friendsAdapter

        auth = Firebase.auth
        if (auth.currentUser != null) {
            currentUser = auth.currentUser!!
        }

        srlSearch.setOnRefreshListener {
            getFriends()
        }

        etPlaceboSearch.setOnClickListener {
            val searchIntent = Intent(mContext, SearchActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(Controller.mainActivity, etPlaceboSearch, "robot")
            // start the new activity
            startActivity(searchIntent, options.toBundle())
//            Controller.mainActivity.overridePendingTransition(0,0)
            // define a click listener
        }

        val adView = AdView(mContext)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = Controller.ADMOB_BANNER_ID
        MobileAds.initialize(mContext) {}
        val mAdView: AdView = view.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }

    private fun getFriends() {
        if (auth.currentUser != null) {
            srlSearch.isRefreshing = true
            friendList.clear()
            databseReference.child(Constants.Keys.FRIENDS).child(currentUser.uid).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        rvFriends.getRecycledViewPool().clear()
                        tvNoFriends.visibility = View.GONE
                        var friends = snapshot.getValue<Friends>()
                        if (friends != null) {
                            friendList.add(friends)
                        }
                        friendsAdapter.setFriendsList(friendList)
                        friendsAdapter.notifyDataSetChanged()
                        srlSearch.isRefreshing = false
                    } else {
                        srlSearch.isRefreshing = false
                        tvNoFriends.visibility = View.VISIBLE
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
                    tvNoFriends.visibility = View.GONE
                }

            })
            srlSearch.isRefreshing = false
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        getFriends()
    }
}