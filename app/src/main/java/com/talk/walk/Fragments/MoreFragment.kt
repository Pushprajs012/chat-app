package com.talk.walk.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.talk.walk.Activities.BlockFriendsActivity
import com.talk.walk.Activities.EditProfileActivity
import com.talk.walk.Activities.NotificationsActivity
import com.talk.walk.Activities.PointsActivity
import com.talk.walk.Activities.WebViewActivity
import com.talk.walk.Adapters.DrawerAdapter
import com.talk.walk.BuildConfig
import com.talk.walk.Models.MainMenuItems
import com.talk.walk.MyApplication
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import de.hdodenhof.circleimageview.CircleImageView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MoreFragment : Fragment(), DrawerAdapter.OnItemClickListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val TAG: String? = EditProfileActivity::class.java.name
    private val GALLERY_REQUEST_CODE: Int = 1

    private lateinit var mContext: Context
    private lateinit var rvMore1: RecyclerView
    private lateinit var rvMore2: RecyclerView
    private lateinit var tvMoreEditProfile: TextView
    private lateinit var tvMorePersonName: TextView
    private lateinit var tvMorePersonGender: TextView
    private lateinit var civMoreProfile: CircleImageView
    private lateinit var tvMoreUserId: TextView

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private var mainMenuItemList: MutableList<MainMenuItems> = mutableListOf<MainMenuItems>()
    private var mainMenuItemList2: MutableList<MainMenuItems> = mutableListOf<MainMenuItems>()
    private lateinit var drawerAdapter: DrawerAdapter
    private lateinit var drawerAdapter2: DrawerAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var notificationCounter: Int = 0
    private var friendBadgeCounter: Int = 0
    private lateinit var myApplication: MyApplication
    private var username = ""
    private var gender: String = ""
    private val db = FirebaseFirestore.getInstance()

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
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = view.context
        sharedPreferences = mContext.getSharedPreferences(
            Constants.Keys.DARK_MODE,
            AppCompatActivity.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()

        rvMore1 = view.findViewById(R.id.rvMore1)
        rvMore2 = view.findViewById(R.id.rvMore2)
        tvMoreEditProfile = view.findViewById(R.id.tvMoreEditProfile)
        tvMorePersonName = view.findViewById(R.id.tvMorePersonName)
        tvMorePersonGender = view.findViewById(R.id.tvMorePersonGender)
        civMoreProfile = view.findViewById(R.id.civMoreProfile)
        tvMoreUserId = view.findViewById(R.id.tvMoreUserId)

        mainMenuItemList.add(
            MainMenuItems(
                R.drawable.ic_outline_notifications_24,
                getString(R.string.notifications)
            )
        )
        mainMenuItemList.add(
            MainMenuItems(
                R.drawable.ic_baseline_block_24,
                getString(R.string.blocked_friends)
            )
        )
        mainMenuItemList.add(
            MainMenuItems(
                R.drawable.ic_baseline_stars_24,
                getString(R.string.get_coins)
            )
        )
        if (activity?.let { Controller.isDarkTheme(it) } == true) {
            mainMenuItemList.add(
                MainMenuItems(
                    R.drawable.ic_outline_dark_mode_24,
                    getString(R.string.switch_to_light_mode)
                )
            )
        } else {
            mainMenuItemList.add(
                MainMenuItems(
                    R.drawable.ic_outline_dark_mode_24,
                    getString(R.string.switch_to_dark_mode)
                )
            )
        }
        mainMenuItemList2.add(
            MainMenuItems(
                R.drawable.ic_outline_person_add_alt_24,
                getString(R.string.invite_friend)
            )
        )
        mainMenuItemList2.add(
            MainMenuItems(
                R.drawable.ic_outline_contact_page_24,
                getString(R.string.contact_us)
            )
        )
        mainMenuItemList2.add(
            MainMenuItems(
                R.drawable.ic_outline_rate_review_24,
                getString(R.string.write_a_review)
            )
        )
        mainMenuItemList2.add(
            MainMenuItems(
                R.drawable.accept,
                getString(R.string.terms_of_service)
            )
        )
        mainMenuItemList2.add(
            MainMenuItems(
                R.drawable.insurance,
                getString(R.string.privacy_policy)
            )
        )
//        mainMenuItemList.add(MainMenuItems(null, "line_breaker"))
//        mainMenuItemList.add(MainMenuItems(null, getString(R.string.logout)))
        drawerAdapter = DrawerAdapter(mContext, mainMenuItemList, activity, this)
        drawerAdapter2 = DrawerAdapter(mContext, mainMenuItemList2, activity, this)
        rvMore1.layoutManager = LinearLayoutManager(mContext)
        rvMore1.adapter = drawerAdapter
        rvMore2.layoutManager = LinearLayoutManager(mContext)
        rvMore2.adapter = drawerAdapter2

        auth = Firebase.auth

        if (auth.currentUser == null) {
            Toast.makeText(mContext, "Please login again", Toast.LENGTH_LONG).show()
        } else {
            currentUser = auth.currentUser!!
            tvMoreUserId.text = currentUser.uid
            databseReference.child("users").child(currentUser.uid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child("points").exists()) {
                            username = snapshot.child("username").value.toString()
                            gender = snapshot.child("gender").value.toString()
                            tvMorePersonName.text = username
                            tvMorePersonGender.text = gender

                            val token_id = FirebaseInstanceId.getInstance().token
                            databseReference.child("users")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                .child("device_token").setValue(token_id)
                        }
                        if (snapshot.child("profile_image").exists()) {
                            try {
                                Picasso.get().load(snapshot.child("profile_image").value.toString())
                                    .into(civMoreProfile)
                            } catch (e: Exception) {
                                Log.e(TAG, "onDataChange: ", e)
                            }
                        } else {
                            if (Controller.isValidContextForGlide(mContext)) {
                                Glide.with(mContext).load(R.drawable.user)
                                    .placeholder(R.drawable.user).error(R.drawable.user)
                                    .into(civMoreProfile)
                                if (Controller.isDarkTheme(Controller.mainActivity)) {
                                    civMoreProfile.setColorFilter(Color.WHITE)
                                } else {
                                    civMoreProfile.setColorFilter(Color.BLACK)
                                }
                            }
                            if (activity?.let { Controller.isDarkTheme(it) } == true) {
                                civMoreProfile.setColorFilter(Color.WHITE)
                            } else {
                                civMoreProfile.setColorFilter(Color.BLACK)
                            }
                        }
                        Log.d(TAG, "onDataChange: $snapshot")
                    } else {
//                        Toast.makeText(mContext, "User details are empty", Toast.LENGTH_LONG).show()

                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })
            databseReference.child("constants").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child("show_profile_token").exists()) {
                            if (snapshot.child("show_profile_token").value as Boolean) {
                             //   tvMoreUserId.visibility = View.VISIBLE
                            } else {
                                tvMoreUserId.visibility = View.GONE
                            }
                        } else {
                            tvMoreUserId.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {


                }

            })
            getNotifications()


        }



        tvMoreEditProfile.setOnClickListener {
            val editProfileIntent = Intent(mContext, EditProfileActivity::class.java)
            startActivity(editProfileIntent)
        }

        val adView = AdView(mContext)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = Controller.ADMOB_BANNER_ID
        MobileAds.initialize(mContext) {}
        val mAdView: AdView = view.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun getNotifications() {
        databseReference.child(Constants.Keys.NOTIFICATIONS).addChildEventListener(object :
            ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child(Constants.Keys.MET_USER_ID).value.toString() == currentUser.uid) {
                    if (snapshot.child("type").value.toString() == Constants.Keys.FRIEND_REQUESTS) {
                        if (snapshot.child("is_read").exists() && !(snapshot.child("is_read").value as Boolean)) {
                            notificationCounter++
                            drawerAdapter.setNotificationCounter(notificationCounter)
                        }
//                        ivNotificationIndicator.visibility = View.VISIBLE

                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child("type").value.toString() == Constants.Keys.FRIEND_REQUEST_ACCEPTED || snapshot.child(
                        "type"
                    ).value.toString() == Constants.Keys.FRIEND_REQUEST_REJECTED
                ) {
                    notificationCounter--
                    drawerAdapter.setNotificationCounter(notificationCounter)
                    if (notificationCounter == 0 || notificationCounter < 0) {

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
//                                                bottomNavigationView.getOrCreateBadge(R.id.search_bottom_menu_litem).number = friendBadgeCounter
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MoreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MoreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(mainMenuItems: MainMenuItems) {
        if (mainMenuItems.title == getString(R.string.my_info)) {

        } else if (mainMenuItems.title == getString(R.string.notifications)) {
            databseReference.child(Constants.Keys.NOTIFICATIONS).child(currentUser.uid).child("is_read").setValue(true)
            val notificationsIntent = Intent(mContext, NotificationsActivity::class.java)
            startActivity(notificationsIntent)
        } else if (mainMenuItems.title == getString(R.string.blocked_friends)) {
            val blockFriendsIntent = Intent(mContext, BlockFriendsActivity::class.java)
            startActivity(blockFriendsIntent)
        } else if (mainMenuItems.title == getString(R.string.contact_us)) {
            databseReference.child("constants").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child("contact_email").exists()) {
                            val contact_email = snapshot.child("contact_email").value.toString()
                            val email = Intent(Intent.ACTION_SEND)
                            email.putExtra(Intent.EXTRA_EMAIL, arrayOf(contact_email))
                            email.putExtra(Intent.EXTRA_TEXT, "User ID: ${currentUser.uid}")
                            email.type = "message/rfc822"
                            startActivity(Intent.createChooser(email, "Send Mail Using"))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


        } else if (mainMenuItems.title == getString(R.string.write_a_review)) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${mContext.packageName}")
                )
            )
        } else if (mainMenuItems.title == getString(R.string.terms_of_service)) {
            db.collection("constants").document("constants").get().addOnCompleteListener {
                if (it.isSuccessful){

                    val webIntent = Intent(mContext, WebViewActivity::class.java)
                    webIntent.putExtra("url", it.result.get("terms_of_service_url").toString())
                    webIntent.putExtra("type", mainMenuItems.title)
                    startActivity(webIntent)

                }

            }
        } else if (mainMenuItems.title == getString(R.string.privacy_policy)) {
            db.collection("constants").document("constants").get().addOnCompleteListener {
                if (it.isSuccessful){

                    val webIntent = Intent(mContext, WebViewActivity::class.java)
                    webIntent.putExtra("url", it.result.get("privacy_policy_url").toString())
                    webIntent.putExtra("type", mainMenuItems.title)
                    startActivity(webIntent)

                }

            }

        } else if (mainMenuItems.title == getString(R.string.get_coins)) {
            startActivity(Intent(mContext, PointsActivity::class.java))
        } else if (mainMenuItems.title == getString(R.string.switch_to_light_mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            editor.putBoolean(Constants.Keys.IS_DARK_MODE_ENABLED, false)
            editor.apply()
        } else if (mainMenuItems.title == getString(R.string.switch_to_dark_mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            editor.putBoolean(Constants.Keys.IS_DARK_MODE_ENABLED, true)
            editor.apply()
        } else if (mainMenuItems.title == getString(R.string.invite_friend)) {
         //  Firebase.database()

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
}