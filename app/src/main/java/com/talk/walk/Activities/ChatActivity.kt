package com.talk.walk.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.talk.walk.Adapters.AttachmentAdapter
import com.talk.walk.Adapters.ChatAdapter
import com.talk.walk.Models.Attachment
import com.talk.walk.Models.Chat
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import kotlin.collections.HashMap
import com.karumi.dexter.PermissionToken

import com.karumi.dexter.listener.PermissionDeniedResponse

import com.karumi.dexter.listener.PermissionGrantedResponse

import com.karumi.dexter.listener.single.PermissionListener

import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import android.webkit.MimeTypeMap
import android.media.MediaRecorder
import com.talk.walk.Utils.FileUtil

import androidx.constraintlayout.widget.ConstraintLayout
import com.karumi.dexter.MultiplePermissionsReport

import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.graphics.Bitmap
import android.graphics.Color
import android.os.*
import android.view.Gravity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.talk.walk.Fragments.ChatMoreBottomSheetFragment
import com.talk.walk.Fragments.InsuffienctPointsBottmSheetFragment
import com.talk.walk.Models.ChatMore
import com.talk.walk.Utils.Controller
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import android.media.ThumbnailUtils
import android.media.MediaMetadataRetriever
import android.view.WindowManager
import java.lang.Exception
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.google.android.material.internal.ViewUtils.dpToPx
import android.util.TypedValue

import android.os.Build
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.handleCoroutineException


@Suppress("BlockingMethodInNonBlockingContext")
class ChatActivity : AppCompatActivity(), AttachmentAdapter.OnItemClickListener,
    ChatMoreBottomSheetFragment.ChatMoreItemCLickListener, CoroutineScope {

    private val THUMBNAIL_SIZE: Int = 64
    private lateinit var mAdView: AdView
    private lateinit var filePhoto: File
    private val GALLERY_IMAGE_REQUEST_CODE: Int = 1
    private val CAMERA_REQUEST_CODE: Int = 2
    private val GALLERY_VIDEO_REQUEST_CODE: Int = 3
    private val TAG: String? = ChatActivity::class.java.name
    private lateinit var mContext: Context

    private lateinit var clRootChat: ConstraintLayout
    private lateinit var rvChat: RecyclerView
    private lateinit var rvChatAttachment: RecyclerView
    private lateinit var ibBack: ImageButton
    private lateinit var tvChatPersonName: TextView
    private lateinit var etChatMessage: EditText
    private lateinit var ibChatAddAttachment: ImageView
    private lateinit var ivChatPaidCoin: ImageView
    private lateinit var cvChatAudioPlayPause: CardView
    private lateinit var ivChatAudioPlayPause: ImageView
    private lateinit var cvChatAudioStop: CardView
    private lateinit var clChat: ConstraintLayout
    private lateinit var cvChatAudio: ConstraintLayout
    private lateinit var tvChatAudioRecordingStatus: TextView
    private lateinit var cvChatSend: CardView
    private lateinit var cvChatSend2: CardView
    private lateinit var ibChatMore: ImageButton
    private lateinit var cvChatUserDisconnected: CardView
    private lateinit var cvChatAddAttachment: CardView
    private lateinit var tvChatReconnect: TextView
    private lateinit var tvChatUserDisconnected: TextView
    private lateinit var tvChatBlockIndicator: TextView
    private lateinit var pbChatUploadMedia: ProgressBar
    private lateinit var tvChatOnlineIndicator: TextView
    private lateinit var civChatProfile: CircleImageView
    private lateinit var cvChatProfileBG: CardView
    private lateinit var ivChatSend: ImageView
    private lateinit var tvCannotSendMoreMsgIndicator: TextView
    private lateinit var clChatShadow: View

    private var chatList: MutableList<Chat> = mutableListOf()
    private var attachmentList: MutableList<Attachment> = mutableListOf()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var attachmentAdapter: AttachmentAdapter


    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private lateinit var met_user_id: String
    private lateinit var media_type: String
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private lateinit var videoThumbnail: Bitmap
    private var job: Job = Job()
    var isMetUserOnline = false
    private var receiver_message_counter = 0L
    private var sender_message_counter = 0L
    var const_sender_user_id = ""
    var const_receiver_user_id = ""
    var isPaid = false
    var is_chat_disconnected: Boolean = false
    var isMetUserAccountDeleted: Boolean = false
    private var isbot: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        mContext = this

        met_user_id = intent.getStringExtra("met_user_id").toString()

        clRootChat = findViewById(R.id.clRootChat)
        ibBack = findViewById(R.id.ibBack)
        rvChat = findViewById(R.id.rvChat)
        tvChatPersonName = findViewById(R.id.tvChatPersonName)
        cvChatSend = findViewById(R.id.cvChatSend)
        etChatMessage = findViewById(R.id.etChatMessage)
        ibChatAddAttachment = findViewById(R.id.ibChatAddAttachment)
        rvChatAttachment = findViewById(R.id.rvChatAttachment)
        cvChatAudioPlayPause = findViewById(R.id.cvChatAudioPlayPause)
        cvChatAudioStop = findViewById(R.id.cvChatAudioStop)
        cvChatSend2 = findViewById(R.id.cvChatSend2)
        ivChatAudioPlayPause = findViewById(R.id.ivChatAudioPlayPause)
        cvChatAudio = findViewById(R.id.cvChatAudioItem)
        clChat = findViewById(R.id.clChat)
        tvChatAudioRecordingStatus = findViewById(R.id.tvChatAudioRecordingStatus)
        ibChatMore = findViewById(R.id.ibChatMore)
        cvChatUserDisconnected = findViewById(R.id.cvChatUserDisconnected)
        tvChatReconnect = findViewById(R.id.tvChatReconnect)
        tvChatUserDisconnected = findViewById(R.id.tvChatUserDisconnected)
        tvChatBlockIndicator = findViewById(R.id.tvChatBlockIndicator)
        pbChatUploadMedia = findViewById(R.id.pbChatUploadMedia)
        tvChatOnlineIndicator = findViewById(R.id.tvChatOnlineIndicator)
        cvChatAddAttachment = findViewById(R.id.cvChatAddAttachment)
        civChatProfile = findViewById(R.id.civChatProfile)
        cvChatProfileBG = findViewById(R.id.cvChatProfileBG)
        ivChatSend = findViewById(R.id.ivChatSend)
        ivChatPaidCoin = findViewById(R.id.ivChatPaidCoin)
        tvCannotSendMoreMsgIndicator = findViewById(R.id.tvCannotSendMoreMsgIndicator)
        clChatShadow = findViewById(R.id.clChatShadow)

        rvChatAttachment.visibility = View.GONE
        cvChatAudio.visibility = View.GONE
        cvChatUserDisconnected.visibility = View.GONE
        tvChatBlockIndicator.visibility = View.GONE
        tvChatReconnect.visibility = View.GONE
        pbChatUploadMedia.visibility = View.GONE

        chatAdapter = ChatAdapter(mContext, chatList)
        rvChat.layoutManager = LinearLayoutManager(mContext)
        rvChat.adapter = chatAdapter

        attachmentList.add(
            0,
            Attachment("Gallery", getDrawable(R.drawable.ic_baseline_insert_photo_24))
        )
        attachmentList.add(
            1,
            Attachment("Camera", getDrawable(R.drawable.ic_baseline_camera_alt_24))
        )
        attachmentList.add(
            2,
            Attachment("Voice", getDrawable(R.drawable.ic_baseline_keyboard_voice_24))
        )
        attachmentList.add(3, Attachment("Video", getDrawable(R.drawable.ic_baseline_videocam_24)))
        attachmentAdapter = AttachmentAdapter(mContext, attachmentList, this)
        rvChatAttachment.layoutManager = GridLayoutManager(mContext, 4)
        rvChatAttachment.adapter = attachmentAdapter

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        if (currentUser == null) {
            Toast.makeText(mContext, "Please login again", Toast.LENGTH_LONG).show()
            finish()
        } else {
            if (met_user_id != null) {
                databseReference.child("users").child(met_user_id)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.child("username").exists()) {
                                    val username = snapshot.child("username").value.toString()
                                    tvChatPersonName.text = username
                                    isMetUserAccountDeleted = false
                                    checkIsMetUserPaidOrNot()
                                } else {
                                    tvChatPersonName.text =
                                        resources.getString(R.string.account_deleted)
                                    isMetUserAccountDeleted = true
                                }

                                if (snapshot.child(Constants.Keys.PROFILE_IMAGE).exists()) {
//                                    if (Controller.isValidContextForGlide(mContext)) {
//                                        Glide.with(mContext).load(snapshot.child(Constants.Keys.PROFILE_IMAGE).value.toString())
//                                            .into(civChatProfile)
//                                    }
                                    try {
                                        Picasso.get()
                                            .load(snapshot.child(Constants.Keys.PROFILE_IMAGE).value.toString())
                                            .into(civChatProfile)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "onDataChange: ${e.localizedMessage}")
                                    }
                                } else {
                                    civChatProfile.setImageDrawable(
                                        AppCompatResources.getDrawable(
                                            mContext,
                                            R.drawable.user
                                        )
                                    )
                                    cvChatProfileBG.setCardBackgroundColor(Controller.getRandomColor())
                                    var layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                    )
                                    layoutParams.leftMargin = 16
                                    layoutParams.rightMargin = 16
                                    layoutParams.topMargin = 16
                                    layoutParams.bottomMargin = 16
                                    civChatProfile.layoutParams = layoutParams
                                    civChatProfile.setColorFilter(Color.WHITE)
                                }
                                checkMetUserIsOnlineOrNot()

                            } else {
                                isMetUserAccountDeleted = false
                                tvChatBlockIndicator.text = getString(R.string.account_deleted)
                                ibChatAddAttachment.isEnabled = false
                                etChatMessage.isEnabled = false
                                cvChatSend.isEnabled = false
                                cvChatSend2.isEnabled = false
                                cvChatSend.setCardBackgroundColor(resources.getColor(R.color.colorGray2))
                                ibChatAddAttachment.setColorFilter(resources.getColor(R.color.colorGray2))
                                cvChatSend2.setCardBackgroundColor(resources.getColor(R.color.colorGray2))
                                cvChatAddAttachment.setCardBackgroundColor(resources.getColor(R.color.colorGray2))
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "onCancelled: ${error.details}")
                        }

                    })

                databseReference.child("botusers")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (userSnapshot in snapshot.children) {
                                val userValue = userSnapshot.getValue(String::class.java)
                                if (userValue != null) {
                                    if (met_user_id == userValue) {
                                        isbot = true
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }


                    })



                if (currentUser.uid == met_user_id) {
                    updateViewForDisconnectUser()
                    cvChatUserDisconnected.visibility = View.GONE
                    tvChatReconnect.visibility = View.GONE
                } else {
                    updateViewForReconnectUser()
                }
            } else {
                Toast.makeText(mContext, "Invalid user id", Toast.LENGTH_LONG).show()
                finish()
            }



            ibChatAddAttachment.visibility = View.VISIBLE

            if (Controller.isDarkTheme(this)) {
                ibChatAddAttachment.setColorFilter(Color.WHITE)
            } else {
                ibChatAddAttachment.setColorFilter(Color.WHITE)
            }


        }

        cvChatSend.setOnClickListener {
            sendChat()

            if (isbot) {
                receiveans()

            }


        }

        ibBack.setOnClickListener {
            onBackPressed()
        }

        ibChatAddAttachment.setOnClickListener {
            if (rvChatAttachment.visibility == View.GONE) {
                animationup()
            } else {
                animationdown();
            }
        }

        cvChatAudioPlayPause.setOnClickListener {
            if (state) {
                pauseRecording()
            } else {
                resumeRecording()
            }
        }

        cvChatAudioStop.setOnClickListener {
            stopRecording()
        }

        cvChatSend2.setOnClickListener {
            if (state) {
                stopRecording()
            }
            var audioFile = File(output)
            clChat.visibility = View.VISIBLE
            cvChatAudio.visibility = View.GONE
            launch {
                val result = uploadMedia(Uri.fromFile(audioFile))
                onResult(result)
            }
        }

        ibChatMore.setOnClickListener {
            val chatMoreBottomSheetFragment =
                ChatMoreBottomSheetFragment.newInstance(met_user_id, "")
            chatMoreBottomSheetFragment.show(supportFragmentManager, "chatMoreBottomSheetFragment")
        }

        tvChatReconnect.setOnClickListener {
            reconnectUser()

        }

        if (auth.currentUser != null) {
            if (met_user_id != null) {
                checkChatDisconnects()
            }
        }

        if (isMetUserAccountDeleted) {
            isMetUserAccountDeleted = false
            tvChatBlockIndicator.text = getString(R.string.account_deleted)
            ibChatAddAttachment.isEnabled = false
            etChatMessage.isEnabled = false
            cvChatSend.isEnabled = false
            cvChatSend2.isEnabled = false
            cvChatSend.setCardBackgroundColor(resources.getColor(R.color.colorGray2))
            ibChatAddAttachment.setColorFilter(resources.getColor(R.color.colorGray2))
            cvChatSend2.setCardBackgroundColor(resources.getColor(R.color.colorGray2))
            cvChatAddAttachment.setCardBackgroundColor(resources.getColor(R.color.colorGray2))
        }

        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = Controller.ADMOB_BANNER_ID
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        clChatShadow.visibility = View.GONE

        clRootChat.getViewTreeObserver()
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val heightDiff: Int =
                        clRootChat.getRootView().getHeight() - clRootChat.getHeight()
                    if (heightDiff > dpToPx(
                            mContext,
                            200
                        )
                    ) { // if more than 200 dp, it's probably a keyboard...
                        // ... do something here
                        mAdView.visibility = View.GONE
                        clChatShadow.visibility = View.VISIBLE
                    } else {

                        mAdView.visibility = View.GONE
                        clChatShadow.visibility = View.GONE
                    }
                }
            })

        if (Build.VERSION.SDK_INT >= 11) {
            rvChat.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (bottom < oldBottom) {
                    rvChat.postDelayed(Runnable {
                        (rvChat.getAdapter()?.getItemCount())?.minus(1)?.let {
                            rvChat.smoothScrollToPosition(
                                it
                            )
                        }
                    }, 100)
                }
            })
        }

        mAdView.visibility = View.GONE

    }

    fun dpToPx(context: Context, valueInDp: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
    }

    private fun reconnectUser() {
        databseReference.child("users").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var points = snapshot.child("points").value as Long
                        if (points == 0L) {
                            val insuffienctPointsBottmSheetFragment =
                                InsuffienctPointsBottmSheetFragment.newInstance(
                                    "You need 10 coins to reconnect with stranger",
                                    ""
                                )
                            insuffienctPointsBottmSheetFragment.show(
                                supportFragmentManager,
                                "insuffienctPointsBottmSheetFragment"
                            )
                        } else {
                            databseReference.child("chats").child(currentUser.uid)
                                .child(met_user_id).setValue(null).addOnSuccessListener {
                                    databseReference.child("chats").child(met_user_id)
                                        .child(currentUser.uid).setValue(null)
                                        .addOnSuccessListener {
                                            var chat_id =
                                                databseReference.child("chat").push().key.toString()
                                            var dataMap: HashMap<String, Any> =
                                                HashMap<String, Any>()
                                            dataMap["sender_user_id"] = currentUser.uid
                                            dataMap["receiver_user_id"] = met_user_id
                                            dataMap["chat_id"] = chat_id
                                            dataMap["timestamp"] = System.currentTimeMillis()
                                            dataMap["is_paid"] = false
                                            dataMap["type"] = ""
                                            if (Controller.isPaid) {
                                                dataMap["is_paid"] = true
                                            }
                                            databseReference.child("chats").child(currentUser.uid)
                                                .child(met_user_id).child(chat_id).setValue(dataMap)
                                                .addOnSuccessListener {
                                                    databseReference.child("chats")
                                                        .child(met_user_id)
                                                        .child(currentUser.uid).child(chat_id)
                                                        .setValue(dataMap).addOnSuccessListener {
                                                            databseReference.child("chats")
                                                                .child(currentUser.uid)
                                                                .child(met_user_id).limitToFirst(1)
                                                                .addListenerForSingleValueEvent(
                                                                    object :
                                                                        ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            if (snapshot.exists()) {
                                                                                for (ds in snapshot.children.iterator()) {
                                                                                    val key =
                                                                                        ds.key.toString()
                                                                                    databseReference.child(
                                                                                        "chats"
                                                                                    )
                                                                                        .child(
                                                                                            currentUser.uid
                                                                                        )
                                                                                        .child(
                                                                                            met_user_id
                                                                                        )
                                                                                        .child(key)
                                                                                        .child("is_paid")
                                                                                        .setValue(
                                                                                            true
                                                                                        )
                                                                                    databseReference.child(
                                                                                        "chats"
                                                                                    )
                                                                                        .child(
                                                                                            met_user_id
                                                                                        )
                                                                                        .child(
                                                                                            currentUser.uid
                                                                                        )
                                                                                        .child(key)
                                                                                        .child("is_paid")
                                                                                        .setValue(
                                                                                            true
                                                                                        )
                                                                                    break
                                                                                }

                                                                            }
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {

                                                                        }

                                                                    })
                                                            databseReference.child(Constants.Keys.CHAT_DISCONNECTS)
                                                                .child(currentUser.uid)
                                                                .child(met_user_id)
                                                                .setValue(null)
                                                            databseReference.child(Constants.Keys.CHAT_DISCONNECTS)
                                                                .child(met_user_id)
                                                                .child(currentUser.uid)
                                                                .setValue(null)
                                                            databseReference.child(Constants.Keys.USERS)
                                                                .child(currentUser.uid)
                                                                .child("points")
                                                                .setValue(points - 10)
                                                            Toast.makeText(
                                                                mContext,
                                                                "You have reconnected again",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            updateViewForReconnectUser()
                                                            chatList.clear()
                                                            chatAdapter.notifyDataSetChanged()
                                                            refreshActivity()
                                                        }
                                                }.addOnFailureListener {
                                                    Log.e(TAG, "sendChat: ", it)
                                                }
                                        }

                                }


                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })
    }

    private fun checkChatMonitor() {
        databseReference.child("chat_counter_monitor").child(currentUser.uid).child(met_user_id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        setChatCounter()
                        receiver_message_counter =
                            snapshot.child("receiver_message_counter").value as Long
                        sender_message_counter =
                            snapshot.child("sender_message_counter").value as Long
                    } else {
                        setChatCounter()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun setChatCounter() {
        val dataMap: HashMap<String, Any> = HashMap()
        dataMap["sender_message_counter"] = sender_message_counter
        dataMap["receiver_message_counter"] = receiver_message_counter
        Log.e(TAG, "onDataChange: $sender_message_counter $receiver_message_counter")
        databseReference.child("chat_monitor").child(currentUser.uid)
            .child(met_user_id).setValue(dataMap)
        databseReference.child("chat_monitor").child(met_user_id)
            .child(currentUser.uid).setValue(dataMap)
    }

    private fun checkIsMetUserPaidOrNot() {
        databseReference.child("chats").child(currentUser.uid).child(met_user_id)
            .limitToFirst(1).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                    const_sender_user_id = snapshot.child("sender_user_id").value.toString()
                    const_receiver_user_id =
                        snapshot.child("receiver_user_id").value.toString()
                    Controller.const_receiver_user_id = const_receiver_user_id
                    Controller.const_sender_user_id = const_sender_user_id
                    if (snapshot.child("is_paid").exists()) {
                        isPaid = snapshot.child("is_paid").value as Boolean
                        Controller.isPaid = isPaid
                        if (isPaid) {
                            ivChatPaidCoin.visibility = View.VISIBLE
                        } else {
                            ivChatPaidCoin.visibility = View.GONE
                        }
                    } else {
                        ivChatPaidCoin.visibility = View.VISIBLE
                    }
                }

                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun checkMetUserIsOnlineOrNot() {
        databseReference.child(Constants.Keys.USERS).child(met_user_id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child("is_online").exists()) {
                            if (snapshot.child("is_online").exists()) {
                                isMetUserOnline = snapshot.child("is_online").value as Boolean
                                if (isMetUserOnline) {
                                    tvChatOnlineIndicator.text = "online"
                                } else {
                                    tvChatOnlineIndicator.text = "offline"
                                }
                            } else {
                                isMetUserOnline = false
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }


    private fun getChats(is_chat_disconnected: Boolean) {
        chatList.clear()
        chatList.add(
            0,
            Chat(
                "",
                false,
                "",
                "",
                "chat_connected_indicator",
                "",
                "",
                "",
                "",
                false,
                "",
                false,
                0,
                0
            )
        )
        databseReference.child("chats").child(currentUser.uid).child(met_user_id)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        val chat = snapshot.getValue<Chat>()
                        if (chat != null) {
                            if (chat.sender_user_id == const_receiver_user_id && chat.type.isEmpty() && (chat.message.isNotEmpty() || chat.media_url.isNotEmpty())) {
                                receiver_message_counter++
                            }
                            if (chat.sender_user_id == const_sender_user_id && chat.type.isEmpty() && (chat.message.isNotEmpty() || chat.media_url.isNotEmpty())) {
                                sender_message_counter++
                            }
                            chatList.add(chat)
                        }
                        Log.e(
                            TAG,
                            "onChildAdded: sender_message_counter: $sender_message_counter receiver_message_counter: $receiver_message_counter"
                        )
                        rvChat.smoothScrollToPosition(chatList.size - 1)
                        chatAdapter.setChatList(chatList)
                        chatAdapter.notifyDataSetChanged()


                        if (chat != null) {
                            if (!is_chat_disconnected) {
                                if (isPaid) {
                                    if (const_sender_user_id == currentUser.uid) {
                                        if (sender_message_counter != 0L) {
                                            if (receiver_message_counter <= 0 && sender_message_counter >= 1) {
                                                updateViewForDisconnectUser()
                                                tvChatReconnect.visibility = View.GONE
                                                cvChatUserDisconnected.visibility = View.GONE
                                                Controller.canSendFriendRequest = false
                                                ibChatMore.setColorFilter(resources.getColor(R.color.colorDarkGray))
                                                tvCannotSendMoreMsgIndicator.visibility =
                                                    View.VISIBLE
                                                ibChatMore.isEnabled = false
                                            } else {
                                                updateViewForReconnectUser()
                                                Controller.canSendFriendRequest = true
                                                tvCannotSendMoreMsgIndicator.visibility = View.GONE
                                            }
                                        }
                                    }
                                }
                            }

                        }

                    }
                }

                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?,
                ) {
                    val chat: Chat? = snapshot.getValue(Chat::class.java)
//                String key = dataSnapshot.getKey();
//                int index = mKeys.indexOf(key);
//                chatList.set(index, chat);
//                chatAdapter.notifyDataSetChanged();

                    //                String key = dataSnapshot.getKey();
//                int index = mKeys.indexOf(key);
//                chatList.set(index, chat);
//                chatAdapter.notifyDataSetChanged();
                    val key: String? = snapshot.key

                    for (i in chatList.indices) {
                        // Find the item to remove and then remove it by index
                        if (chatList[i].chat_id == key) {
                            if (chat != null) {
                                chatList[i] = chat
                                chatAdapter.notifyItemRangeChanged(i, chatList.size)
                            }
                            break
                        }
                    }

//                    chatAdapter.notifyDataSetChanged()
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val chat = snapshot.getValue<Chat>()
                    chatList.remove(chat)
                    chatAdapter.notifyDataSetChanged()
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })

    }

    private fun checkChatBlock() {
        databseReference.child("blocks").child(currentUser.uid).child(met_user_id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user_id = snapshot.child("user_id").value.toString()
                        val blocked_user_id = snapshot.child("met_user_id").value.toString()
                        Controller.blocked_met_user_id = blocked_user_id
                        Controller.blocked_user_id = user_id
                        updateViewForDisconnectUser()
                        if (user_id == currentUser.uid) {
                            ibChatMore.isEnabled = true
                            ibChatMore.setColorFilter(Color.BLACK)
                        }
                        cvChatUserDisconnected.visibility = View.GONE
                        tvChatReconnect.visibility = View.GONE
                        if (blocked_user_id == currentUser.uid) {
                            tvChatBlockIndicator.visibility = View.VISIBLE
                            Controller.isChatUserBlocked = true
                        } else {
                            tvChatBlockIndicator.visibility = View.VISIBLE
                            tvChatBlockIndicator.text = "User has been blocked"
                            Controller.isChatUserBlocked = true
                        }
                    } else {
                        tvChatBlockIndicator.visibility = View.GONE
                        Controller.isChatUserBlocked = false
                        updateViewForReconnectUser()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })
    }

    private fun checkChatDisconnects() {
        databseReference.child(Constants.Keys.CHAT_DISCONNECTS).child(currentUser.uid)
            .child(met_user_id).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var receiver_user_id = snapshot.child("met_user_id").value.toString()
                        updateViewForDisconnectUser()
                        is_chat_disconnected = true
//                    if (currentUser.uid == receiver_user_id) {
//                        tvChatReconnect.visibility = View.GONE
//                        tvChatBlockIndicator.visibility = View.VISIBLE
//                    }
                        getChats(is_chat_disconnected)
                        checkChatMonitor()
                    } else {
                        checkChatBlock()
                        updateViewForReconnectUser()
                        is_chat_disconnected = false
                        getChats(is_chat_disconnected)
                        checkChatMonitor()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })
    }


    private fun animationup() {
        val clk_rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        val fade_in = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        clk_rotate.fillAfter = true
        fade_in.fillAfter = true
        // assigning that animation to
        // the image and start animation
        ibChatAddAttachment.startAnimation(clk_rotate)
        rvChatAttachment.startAnimation(fade_in)
        rvChatAttachment.visibility = View.VISIBLE
    }

    private fun animationdown() {
        val clk_antirotate = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise)
        val fade_out = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        clk_antirotate.fillAfter = true
//                fade_out.fillAfter = true
        // assigning that animation to
        // the image and start animation
        ibChatAddAttachment.startAnimation(clk_antirotate)
        rvChatAttachment.startAnimation(fade_out)
        rvChatAttachment.visibility = View.GONE
    }

    private fun sendChat() {
        var message = etChatMessage.text.toString()
        if (message.isEmpty()) {
            Toast.makeText(mContext, "Cannot send empty message", Toast.LENGTH_LONG).show()
        } else {
            var chat_id = databseReference.child("chat").push().key.toString()
            var dataMap: HashMap<String, Any> = HashMap<String, Any>()
            dataMap["sender_user_id"] = currentUser.uid
            dataMap["receiver_user_id"] = met_user_id
            dataMap["message"] = message
            dataMap["chat_id"] = chat_id
            dataMap["timestamp"] = System.currentTimeMillis()
            dataMap["met_user_id"] = met_user_id
            dataMap["type"] = ""
            if (!isbot){
            etChatMessage.text.clear()}
            databseReference.child("chats").child(currentUser.uid).child(met_user_id).child(chat_id)
                .setValue(dataMap).addOnSuccessListener {
                    databseReference.child("chats").child(met_user_id).child(currentUser.uid)
                        .child(chat_id).setValue(dataMap)
                    databseReference.child("recent_message").child(met_user_id)
                        .child(currentUser.uid).setValue(dataMap)
                    databseReference.child("recent_message").child(currentUser.uid)
                        .child(met_user_id).setValue(dataMap)
                    if (!isMetUserOnline) {
                        databseReference.child("notifications").child(met_user_id)
                            .child(currentUser.uid).child(chat_id).setValue(dataMap)
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "sendChat: ", it)
                    Toast.makeText(mContext, "Failed to send message. $it", Toast.LENGTH_LONG)
                        .show()
                }


        }

    }


    override fun onItemClick(position: Int, attachment: Attachment) {
        media_type = attachment.attachment_name
        when (attachment.attachment_name) {
            Constants.Values.GALLERY -> {
                openGallery()
            }

            Constants.Values.CAMERA -> {
                openCamera()
            }

            Constants.Values.VOICE -> {
                recordVoice()
            }

            Constants.Values.VIDEO -> {
                openVideoGallery()
            }
        }
    }

    private fun openVideoGallery() {
        Dexter.withContext(mContext)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    selectVideoFromGallery()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            }).check()
    }

    private fun recordVoice() {
        Dexter.withContext(mContext)
            .withPermissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    output =
                        mContext.externalCacheDir!!.absolutePath + "/${currentUser.uid}.mp3"
                    mediaRecorder = MediaRecorder()
                    mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                    mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    mediaRecorder?.setOutputFile(output)
                    startRecording()
                    val clk_antirotate =
                        AnimationUtils.loadAnimation(mContext, R.anim.rotate_anticlockwise)
                    val fade_out = AnimationUtils.loadAnimation(mContext, R.anim.fade_out)
                    clk_antirotate.fillAfter = true
                    //fade_out.fillAfter = true
                    // assigning that animation to
                    // the image and start animation
                    ibChatAddAttachment.startAnimation(clk_antirotate)
                    rvChatAttachment.startAnimation(fade_out)
                    rvChatAttachment.visibility = View.GONE
                    clChat.visibility = View.GONE
                    cvChatAudio.visibility = View.VISIBLE
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken,
                ) {
                    val dialogMultiplePermissionsListener: MultiplePermissionsListener =
                        DialogOnAnyDeniedMultiplePermissionsListener.Builder
                            .withContext(mContext)
                            .withTitle("Audio Permission")
                            .withMessage("Audio permission is required to record your audio")
                            .withButtonText(android.R.string.ok)
                            .withIcon(R.mipmap.ic_launcher_round)
                            .build()
                    dialogMultiplePermissionsListener.onPermissionRationaleShouldBeShown(
                        permissions,
                        token
                    )
                }
            }).check()
    }

    private fun openCamera() {
        Dexter.withContext(mContext)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    filePhoto = getPhotoFile(currentUser.uid)
                    val providerFile = FileProvider.getUriForFile(
                        mContext,
                        "$packageName.provider",
                        filePhoto
                    )
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
                    startActivityForResult(takePhotoIntent, CAMERA_REQUEST_CODE)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    val dialogPermissionListener: PermissionListener =
                        DialogOnDeniedPermissionListener.Builder
                            .withContext(mContext)
                            .withTitle(getString(R.string.camera_permission))
                            .withMessage(getString(R.string.camera_permission_message))
                            .withButtonText(android.R.string.ok)
                            .withIcon(R.mipmap.ic_launcher_round)
                            .build()
                    dialogPermissionListener.onPermissionDenied(response)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun openGallery() {
        Dexter.withContext(mContext)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    selectImageFromGallery()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    val dialogPermissionListener: PermissionListener =
                        DialogOnDeniedPermissionListener.Builder
                            .withContext(mContext)
                            .withTitle(getString(R.string.storage_permission))
                            .withMessage(getString(R.string.storage_permission_message))
                            .withButtonText(android.R.string.ok)
                            .withIcon(R.mipmap.ic_launcher_round)
                            .build()
                    dialogPermissionListener.onPermissionDenied(response)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            var file_uri = data.data
            if (retriveVideoFrameFromVideo(file_uri) == null) {
                Toast.makeText(mContext, "Video Upload Failed", Toast.LENGTH_LONG).show()
            } else {
                videoThumbnail = getThumbVideo(mContext, file_uri)!!
                Log.e(TAG, "onActivityResult: $videoThumbnail")
                launch {
                    val result = file_uri?.let { uploadMedia(it) }
                    if (result != null) {
                        onResult(result)
                    }
                }

            }
//        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        } else if (requestCode == GALLERY_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val resultUri = data.data
            if (resultUri != null) {

                UCrop.of(
                    resultUri,
                    Uri.fromFile(
                        File.createTempFile(
                            currentUser.uid,
                            ".jpg",
                            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        )
                    )
                )
                    .withMaxResultSize(1920, 1080)
                    .start(this@ChatActivity)
            };
//                val file_uri = data.data
//                if (resultUri != null) {
//                    launch {
//                        val result = uploadMedia(resultUri)
//                        onResult(result)
//                    }
//                }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val takenPhoto = BitmapFactory.decodeFile(
                FileUtil.from(mContext, Uri.fromFile(filePhoto)).toString()
            )
            val file_uri = Controller.handleSamplingAndRotationBitmap(
                mContext,
                getImageUri(mContext, takenPhoto)
            )?.let {
                getImageUri(mContext, it)
            }
            if (file_uri != null) {
                launch {
                    val result = uploadMedia(file_uri)
                    onResult(result)
                }
            }
//            getImageUri(mContext, takenPhoto)?.let { uploadMedia(it) }
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val file_uri = data?.let { UCrop.getOutput(it) };
            if (file_uri != null) {
                launch {
                    val result = uploadMedia(file_uri)
                    onResult(result)
                }
            }
        }
    }

    fun getThumbVideo(context: Context?, videoUri: Uri?): Bitmap? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, videoUri)
            bitmap = mediaMetadataRetriever.getFrameAtTime(
                1000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }

    private fun getPowerOfTwoForSampleRatio(ratio: Double): Int {
        val k = Integer.highestOneBit(Math.floor(ratio).toInt())
        return if (k == 0) 1 else k
    }

    private fun isFileLessThan40MB(file: File): Boolean {
        val maxFileSize = 40 * 1024 * 1024
        val l = file.length()
        val fileSize = l.toString()
        val finalFileSize = fileSize.toInt()
        val file_size: Int = java.lang.String.valueOf(file.length()).toInt()
        Log.e(TAG, "isFileLessThan1MB: $file_size")
        return file_size <= maxFileSize
    }

    private fun isFileLessThan1MB(file: File): Boolean {
        val maxFileSize = 1 * 1024 * 1024
        val l = file.length()
        val fileSize = l.toString()
        val finalFileSize = fileSize.toInt()
        val file_size: Int = java.lang.String.valueOf(file.length()).toInt()
        Log.e(TAG, "isFileLessThan1MB: $file_size")
        return file_size <= maxFileSize
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            tvChatAudioRecordingStatus.text = "Recording"
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            recordingStopped = true
            tvChatAudioRecordingStatus.text = "Recording Stopped"
            cvChatSend2.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if (state) {
            if (!recordingStopped) {
                tvChatAudioRecordingStatus.text = "Recording Paused"
                mediaRecorder?.pause()
                recordingStopped = true
                cvChatSend2.visibility = View.VISIBLE
                ivChatAudioPlayPause.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_play_arrow_24))
            } else {
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        tvChatAudioRecordingStatus.text = "Recording Resumed"
        Handler().postDelayed({
            tvChatAudioRecordingStatus.text = "Recording"
        }, 2000)
        if (mediaRecorder != null) {
            mediaRecorder?.resume()
        }

        ivChatAudioPlayPause.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24))
        recordingStopped = false
    }

    private fun selectImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select image"
            ),
            GALLERY_IMAGE_REQUEST_CODE
        )

//        CropImage.activity()
//            .setGuidelines(CropImageView.Guidelines.ON)
//            .setMinCropResultSize(512, 512)
//            .start(this@ChatActivity)
    }

    private fun selectVideoFromGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select video"
            ),
            GALLERY_VIDEO_REQUEST_CODE
        )
    }


    @Throws(Throwable::class)
    fun retriveVideoFrameFromVideo(videoPath: Uri?): Bitmap? {
        val filePathColumn =
            arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            videoPath?.let { getContentResolver().query(it, filePathColumn, null, null, null) }
        cursor?.moveToFirst()
        val columnIndex: Int? = cursor?.getColumnIndex(filePathColumn[0])
        val picturePath = cursor!!.getString(columnIndex!!)
        cursor.close();

        return ThumbnailUtils.createVideoThumbnail(
            FileUtil.from(mContext, videoPath).absolutePath,
            MediaStore.Video.Thumbnails.MICRO_KIND
        )
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(
                inContext.contentResolver,
                inImage,
                currentUser.uid,
                null
            )
        return Uri.parse(path)
    }

    private suspend fun uploadMedia(fileUri: Uri) {
        etChatMessage.text.clear()
        if (fileUri != null) {
            if (media_type == Constants.Keys.GALLERY) {
                if (isFileLessThan1MB(FileUtil.from(mContext, fileUri))) {
                    uploadImageFinal(fileUri)
                } else {
                    showTwoButtonDialog(
                        "Upload Size",
                        "Upload size of image should be less than 1 MB."
                    )
                }
            } else if (media_type == Constants.Keys.VIDEO) {
                if (isFileLessThan40MB(FileUtil.from(mContext, fileUri))) {
                    uploadVideoFinal(fileUri)
                } else {
                    showTwoButtonDialog(
                        "Upload Size",
                        "Upload size of video should be less than 40 MB."
                    )
                }
            } else if (media_type == Constants.Keys.CAMERA) {
                uploadImageFinal(fileUri)
            } else if (media_type == Constants.Keys.VOICE) {
                recordingStopped = true
                ivChatAudioPlayPause.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24))
                uploadMediaFinal(fileUri)
            }
        } else {
            Toast.makeText(mContext, "Invalid Media File", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun uploadImageFinal(fileUri: Uri) {
        pbChatUploadMedia.isIndeterminate = true
        pbChatUploadMedia.visibility = View.GONE
        val clk_antirotate = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise)
        val fade_out = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        if (media_type != "Audio") {
            clk_antirotate.fillAfter = true
//                fade_out.fillAfter = true
            // assigning that animation to
            // the image and start animation
            ibChatAddAttachment.startAnimation(clk_antirotate)
            rvChatAttachment.startAnimation(fade_out)
            rvChatAttachment.visibility = View.GONE
        }
        Toast.makeText(mContext, "Uploading...", Toast.LENGTH_LONG).show()
        var timeInMillis: Long = System.currentTimeMillis()
        val extension: String =
            MimeTypeMap.getFileExtensionFromUrl(FileUtil.from(mContext, fileUri).absolutePath)
        val stream = ByteArrayOutputStream()

        val compressedImageFile = Compressor.compress(
            mContext,
            File(FileUtil.from(mContext, fileUri).absolutePath)
        ) { quality(50) }
        var fileInputStream: FileInputStream? = FileInputStream(compressedImageFile)

        val refStorage =
            FirebaseStorage.getInstance().reference.child("chats/${currentUser.uid}/$media_type/$timeInMillis.$extension")


        var chat_id = databseReference.child("chat").push().key.toString()
        var dataMap: HashMap<String, Any> = HashMap<String, Any>()
        dataMap["sender_user_id"] = currentUser.uid
        dataMap["receiver_user_id"] = met_user_id
        dataMap["message"] = ""
        dataMap["chat_id"] = chat_id
        dataMap["media_type"] = media_type
        dataMap["media_url"] = ""
        dataMap["timestamp"] = timeInMillis
        dataMap["is_read"] = isMetUserOnline
        dataMap["met_user_id"] = met_user_id
        dataMap["is_uploading"] = true
        dataMap["upload_progress"] = 0
        dataMap["video_thumbnail"] = ""

        databseReference.child("chats").child(currentUser.uid).child(met_user_id)
            .child(chat_id).setValue(dataMap).addOnSuccessListener {
                databseReference.child("chats").child(met_user_id)
                    .child(currentUser.uid)
                    .child(chat_id).setValue(dataMap)
                databseReference.child("recent_message").child(met_user_id)
                    .child(currentUser.uid).setValue(dataMap)
                databseReference.child("recent_message").child(currentUser.uid)
                    .child(met_user_id).setValue(dataMap)
                if (fileInputStream != null) {
                    refStorage.putStream(fileInputStream)
                        .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                                val imageUrl = it.toString()

                                databseReference.child("chats").child(currentUser.uid)
                                    .child(met_user_id).child(chat_id)
                                    .child("media_url")
                                    .setValue(imageUrl).addOnSuccessListener {
                                        databseReference.child("chats")
                                            .child(met_user_id)
                                            .child(currentUser.uid).child(chat_id)
                                            .child("media_url").setValue(imageUrl)
                                        databseReference.child("chats")
                                            .child(met_user_id)
                                            .child(currentUser.uid).child(chat_id)
                                            .child("is_uploading").setValue(false)
                                        databseReference.child("chats")
                                            .child(currentUser.uid)
                                            .child(met_user_id).child(chat_id)
                                            .child("is_uploading").setValue(false)
                                        databseReference.child("chats")
                                            .child(met_user_id)
                                            .child(currentUser.uid).child(chat_id)
                                            .child("upload_progress").setValue(0)
                                        databseReference.child("chats")
                                            .child(currentUser.uid)
                                            .child(met_user_id).child(chat_id)
                                            .child("upload_progress").setValue(0)
                                        //                                                    refreshActivity()
                                    }
                            }
                        })

                        ?.addOnFailureListener(OnFailureListener { e ->
                            print(e.message)
                        })!!.addOnCompleteListener {
                            pbChatUploadMedia.visibility = View.GONE
                        }.addOnProgressListener {
                            pbChatUploadMedia.isIndeterminate = false
                            val progress: Double =
                                100.0 * it.getBytesTransferred() / it.getTotalByteCount()
                            pbChatUploadMedia.progress = progress.toInt()
                            databseReference.child("chats").child(currentUser.uid)
                                .child(met_user_id).child(chat_id)
                                .child("upload_progress")
                                .setValue(progress)
                        }
                }
            }.addOnFailureListener {
                Log.e(TAG, "sendChat: ", it)
                Toast.makeText(
                    mContext,
                    "Failed to send message. $it",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private suspend fun uploadMediaFinal(fileUri: Uri) {
        pbChatUploadMedia.isIndeterminate = true
        pbChatUploadMedia.visibility = View.VISIBLE
        var videoThumbnailUrl: String = ""
        val clk_antirotate = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise)
        val fade_out = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        if (media_type != "Audio") {
            clk_antirotate.fillAfter = true
//                fade_out.fillAfter = true
            // assigning that animation to
            // the image and start animation
            ibChatAddAttachment.startAnimation(clk_antirotate)
            rvChatAttachment.startAnimation(fade_out)
            rvChatAttachment.visibility = View.GONE
        }
        Toast.makeText(mContext, "Uploading...", Toast.LENGTH_LONG).show()
        var timeInMillis: Long = System.currentTimeMillis()
        val extension: String =
            MimeTypeMap.getFileExtensionFromUrl(FileUtil.from(mContext, fileUri).absolutePath)
        if (media_type == Constants.Keys.VIDEO) {
            val videoThumbnailRefStorage =
                FirebaseStorage.getInstance().reference.child("chats/${currentUser.uid}/$media_type/thumbnail/$timeInMillis.$extension")
            getImageUri(mContext, videoThumbnail)?.let { videoThumbnailRefStorage.putFile(it) }
                ?.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        videoThumbnailUrl = it.toString()
                    }
                })
        }
        var fileInputStream: FileInputStream? = null
        fileInputStream =
            if (media_type == Constants.Keys.GALLERY || media_type == Constants.Keys.CAMERA) {
                val compressedImageFile = Compressor.compress(
                    mContext,
                    File(FileUtil.from(mContext, fileUri).absolutePath)
                ) { quality(50) }
                FileInputStream(compressedImageFile)
            } else {
                FileInputStream(File(FileUtil.from(mContext, fileUri).absolutePath))
            }
        val refStorage =
            FirebaseStorage.getInstance().reference.child("chats/${currentUser.uid}/$media_type/$timeInMillis.$extension")
        if (fileInputStream != null) {
            refStorage.putStream(fileInputStream)
                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        var chat_id = databseReference.child("chat").push().key.toString()
                        var dataMap: HashMap<String, Any> = HashMap<String, Any>()
                        dataMap["sender_user_id"] = currentUser.uid
                        dataMap["receiver_user_id"] = met_user_id
                        dataMap["message"] = ""
                        dataMap["chat_id"] = chat_id
                        dataMap["media_type"] = media_type
                        dataMap["media_url"] = imageUrl
                        dataMap["timestamp"] = timeInMillis
                        dataMap["is_read"] = isMetUserOnline
                        dataMap["met_user_id"] = met_user_id
                        if (media_type == Constants.Keys.VIDEO) {
                            dataMap["video_thumbnail"] = videoThumbnailUrl
                        }
                        databseReference.child("chats").child(currentUser.uid)
                            .child(met_user_id).child(chat_id).setValue(dataMap)
                            .addOnSuccessListener {
                                databseReference.child("chats").child(met_user_id)
                                    .child(currentUser.uid).child(chat_id).setValue(dataMap)
                                databseReference.child("recent_message").child(met_user_id)
                                    .child(currentUser.uid).setValue(dataMap)
                                databseReference.child("recent_message").child(currentUser.uid)
                                    .child(met_user_id).setValue(dataMap)
                                etChatMessage.text.clear()
                            }.addOnFailureListener {
                                Log.e(TAG, "sendChat: ", it)
                                Toast.makeText(
                                    mContext,
                                    "Failed to send message. $it",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                })

                ?.addOnFailureListener(OnFailureListener { e ->
                    print(e.message)
                })!!.addOnCompleteListener {
                    pbChatUploadMedia.visibility = View.GONE
                }.addOnProgressListener {
                    pbChatUploadMedia.isIndeterminate = false
                    val progress: Double =
                        100.0 * it.getBytesTransferred() / it.getTotalByteCount()
                    pbChatUploadMedia.progress = progress.toInt()
                }
        }
    }

    private suspend fun uploadVideoFinal(fileUri: Uri) {
        pbChatUploadMedia.isIndeterminate = true
        pbChatUploadMedia.visibility = View.GONE
        var videoThumbnailUrl: String = ""
        val clk_antirotate = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise)
        val fade_out = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        if (media_type != "Audio") {
            clk_antirotate.fillAfter = true
//                fade_out.fillAfter = true
            // assigning that animation to
            // the image and start animation
            ibChatAddAttachment.startAnimation(clk_antirotate)
            rvChatAttachment.startAnimation(fade_out)
            rvChatAttachment.visibility = View.GONE
        }
        Toast.makeText(mContext, "Uploading...", Toast.LENGTH_LONG).show()
        var timeInMillis: Long = System.currentTimeMillis()
        val extension: String =
            MimeTypeMap.getFileExtensionFromUrl(FileUtil.from(mContext, fileUri).absolutePath)
        val videoThumbnailRefStorage =
            FirebaseStorage.getInstance().reference.child("chats/${currentUser.uid}/$media_type/thumbnail/$timeInMillis.$extension")
        val stream = ByteArrayOutputStream()
        videoThumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val data = stream.toByteArray()
//        getImageUri(mContext, videoThumbnail)?.let { videoThumbnailRefStorage.putFile(it) }
        var fileInputStream: FileInputStream? =
            FileInputStream(File(FileUtil.from(mContext, fileUri).absolutePath))

        val refStorage =
            FirebaseStorage.getInstance().reference.child("chats/${currentUser.uid}/$media_type/$timeInMillis.$extension")

        var chat_id = databseReference.child("chat").push().key.toString()
        var dataMap: HashMap<String, Any> = HashMap<String, Any>()
        dataMap["sender_user_id"] = currentUser.uid
        dataMap["receiver_user_id"] = met_user_id
        dataMap["message"] = ""
        dataMap["chat_id"] = chat_id
        dataMap["media_type"] = media_type
        dataMap["media_url"] = ""
        dataMap["timestamp"] = timeInMillis
        dataMap["is_read"] = isMetUserOnline
        dataMap["met_user_id"] = met_user_id
        dataMap["is_uploading"] = true
        dataMap["upload_progress"] = 0
        dataMap["video_thumbnail"] = ""

        databseReference.child("chats").child(currentUser.uid).child(met_user_id)
            .child(chat_id).setValue(dataMap).addOnSuccessListener {
                databseReference.child("chats").child(met_user_id)
                    .child(currentUser.uid)
                    .child(chat_id).setValue(dataMap)
                databseReference.child("recent_message").child(met_user_id)
                    .child(currentUser.uid).setValue(dataMap)
                databseReference.child("recent_message").child(currentUser.uid)
                    .child(met_user_id).setValue(dataMap)
                if (fileInputStream != null) {
                    videoThumbnailRefStorage.putBytes(data)
                        .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                                videoThumbnailUrl = it.toString()
                                databseReference.child("chats")
                                    .child(met_user_id)
                                    .child(currentUser.uid).child(chat_id)
                                    .child("video_thumbnail").setValue(videoThumbnailUrl)
                                databseReference.child("chats")
                                    .child(currentUser.uid)
                                    .child(met_user_id).child(chat_id)
                                    .child("video_thumbnail").setValue(videoThumbnailUrl)
                                refStorage.putStream(fileInputStream)
                                    .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                                            val videoUrl = it.toString()
                                            databseReference.child("chats").child(currentUser.uid)
                                                .child(met_user_id).child(chat_id)
                                                .child("media_url")
                                                .setValue(videoUrl).addOnSuccessListener {
                                                    databseReference.child("chats")
                                                        .child(met_user_id)
                                                        .child(currentUser.uid).child(chat_id)
                                                        .child("media_url").setValue(videoUrl)
                                                    databseReference.child("chats")
                                                        .child(met_user_id)
                                                        .child(currentUser.uid).child(chat_id)
                                                        .child("is_uploading").setValue(false)
                                                    databseReference.child("chats")
                                                        .child(currentUser.uid)
                                                        .child(met_user_id).child(chat_id)
                                                        .child("is_uploading").setValue(false)
                                                    databseReference.child("chats")
                                                        .child(met_user_id)
                                                        .child(currentUser.uid).child(chat_id)
                                                        .child("upload_progress").setValue(0)
                                                    databseReference.child("chats")
                                                        .child(currentUser.uid)
                                                        .child(met_user_id).child(chat_id)
                                                        .child("upload_progress").setValue(0)
                                                    //                                                    refreshActivity()
                                                }
                                        }
                                    })

                                    ?.addOnFailureListener(OnFailureListener { e ->
                                        print(e.message)
                                    })!!.addOnCompleteListener {
                                        pbChatUploadMedia.visibility = View.GONE
                                    }.addOnProgressListener {
                                        pbChatUploadMedia.isIndeterminate = false
                                        val progress: Double =
                                            100.0 * it.getBytesTransferred() / it.getTotalByteCount()
                                        pbChatUploadMedia.progress = progress.toInt()
                                        databseReference.child("chats").child(currentUser.uid)
                                            .child(met_user_id).child(chat_id)
                                            .child("upload_progress")
                                            .setValue(progress)
                                    }
                            }
                        })

                }
            }.addOnFailureListener {
                Log.e(TAG, "sendChat: ", it)
                Toast.makeText(
                    mContext,
                    "Failed to send message. $it",
                    Toast.LENGTH_LONG
                ).show()
            }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (auth.currentUser != null) {
            databseReference.child(Constants.Keys.USERS).child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            databseReference.child("users").child(currentUser.uid)
                                .child("is_online")
                                .setValue(false)
                        } else {
                            auth.signOut()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }


    override fun onItemClick(chatMore: ChatMore?) {
    }

    override fun onDisconnected(is_disconnected: Boolean) {
        if (is_disconnected) {
            tvChatReconnect.visibility = View.GONE
            tvChatReconnect.gravity = Gravity.CENTER
            updateViewForDisconnectUser()
            databseReference.child("chats").child(currentUser.uid).child(met_user_id).setValue(null)
                .addOnSuccessListener {
//                    databseReference.child("chats").child(met_user_id).child(currentUser.uid)
//                        .setValue(null)
                    onBackPressed()
                }
        }
    }

    override fun onBlocked(isBlocked: Boolean) {
        updateViewForDisconnectUser()
        cvChatUserDisconnected.visibility = View.GONE
        tvChatReconnect.visibility = View.GONE
    }

    override fun onChatCleared(isCleared: Boolean) {
        if (isCleared) {
            chatList.clear()
            chatAdapter.notifyDataSetChanged()
//            getChats()
            refreshActivity()
        }
    }

    override fun onUnBlocked(isUnBlocked: Boolean) {
        if (isUnBlocked) {
//            refreshActivity()
        }
    }

    private fun refreshActivity() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private fun updateViewForDisconnectUser() {
        cvChatSend2.visibility = View.GONE
        cvChatUserDisconnected.visibility = View.VISIBLE
        tvChatReconnect.visibility = View.VISIBLE
        ibChatAddAttachment.isEnabled = false
        etChatMessage.isEnabled = false
        cvChatSend.isEnabled = false
        cvChatSend2.isEnabled = false
        ibChatMore.isEnabled = false
        cvChatSend.setCardBackgroundColor(resources.getColor(R.color.colorGray2))
        ibChatAddAttachment.setColorFilter(resources.getColor(R.color.colorDarkGray))
        ivChatSend.setColorFilter(resources.getColor(R.color.colorDarkGray))
        ibChatMore.setColorFilter(resources.getColor(R.color.colorDarkGray))
        cvChatSend2.setCardBackgroundColor(resources.getColor(R.color.colorGray2))
        cvChatAddAttachment.setCardBackgroundColor(resources.getColor(R.color.colorGray2))
    }

    private fun updateViewForReconnectUser() {
        cvChatSend2.visibility = View.GONE
        cvChatUserDisconnected.visibility = View.GONE
        tvChatReconnect.visibility = View.GONE
        ibChatAddAttachment.isEnabled = true
        etChatMessage.isEnabled = true
        cvChatSend.isEnabled = true
        cvChatSend2.isEnabled = true
        ibChatMore.isEnabled = true
        cvChatSend.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        ibChatMore.setColorFilter(resources.getColor(R.color.black))
        ibChatAddAttachment.setColorFilter(resources.getColor(R.color.white2))
        ivChatSend.setColorFilter(resources.getColor(R.color.white2))
        cvChatSend2.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        cvChatAddAttachment.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun onResult(result: Unit) {
        Log.d(TAG, "onResult: $result")
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

    private fun showImageVideoSelectDialog(file_uri: Uri) {
        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.dialog_image_video_select_layout)
        dialog.window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val tvDialogImageVideoHeader: TextView = dialog.findViewById(R.id.tvDialogImageVideoHeader)
        val ivDialogImageVideo: ImageView = dialog.findViewById(R.id.ivDialogImageVideo)
        val tvDialogImageVideoChangeImage: TextView =
            dialog.findViewById(R.id.tvDialogImageVideoChangeImage)
        val tvDialogImageVideoUpload: TextView = dialog.findViewById(R.id.tvDialogImageVideoUpload)

        if (media_type == Constants.Keys.VIDEO) {
            tvDialogImageVideoHeader.text = "Video Selected"
            tvDialogImageVideoChangeImage.text = "Change Video"
        } else {
            tvDialogImageVideoHeader.text = "Image Selected"
            tvDialogImageVideoChangeImage.text = "Change Image"
        }

        Glide.with(mContext).load(file_uri).into(ivDialogImageVideo)

        tvDialogImageVideoChangeImage.setOnClickListener {
            if (media_type == Constants.Keys.VIDEO) {
                selectVideoFromGallery()
            } else {
                selectImageFromGallery()
            }
        }

        tvDialogImageVideoUpload.setOnClickListener {
            dialog.dismiss()
            if (media_type == Constants.Keys.VIDEO) {
                if (retriveVideoFrameFromVideo(file_uri) == null) {
                    Toast.makeText(mContext, "Video Upload Failed", Toast.LENGTH_LONG).show()
                } else {
                    videoThumbnail = getThumbVideo(mContext, file_uri)!!
                    Log.e(TAG, "onActivityResult: $videoThumbnail")
                    launch {
                        val result = uploadMedia(file_uri)
                        onResult(result)
                    }

                }
            } else {
                launch {
                    val result = uploadMedia(file_uri)
                    onResult(result)
                }
            }
        }

        dialog.setOnDismissListener {
            media_type = ""

        }
    }


    override fun onStart() {
        super.onStart()
        if (auth.getCurrentUser() != null) {
            databseReference.child(Constants.Keys.USERS).child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            databseReference.child("users").child(currentUser.uid)
                                .child("is_online")
                                .setValue(true)
                            checkMetUserIsOnlineOrNot()
                        } else {
                            auth.signOut()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    override fun onStop() {
        super.onStop()
        if (auth.getCurrentUser() != null) {
            databseReference.child(Constants.Keys.USERS).child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            databseReference.child("users").child(currentUser.uid)
                                .child("is_online")
                                .setValue(false)
                        } else {
                            auth.signOut()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun receiveans() {
        var ansString = ""
        val handler = Handler(Looper.getMainLooper())
      val delaylist= arrayListOf(500L,1000L,2000L,3000L,4000L,5000L,10000L,20000L,30000L,40000L,50000L)//,60000L,70000L,80000L,90000L,100000L,200000L)


        var message = etChatMessage.text.toString()
        if (!message.isEmpty()) {
            etChatMessage.text.clear()

            if (message.toLowerCase()=="hiii" || message.toLowerCase()
                    =="hi" || message.toLowerCase()
                    =="hii" || message=="hlw" || message.toLowerCase()
                    .toLowerCase()=="hello"
            ) {

                databseReference.child("question ").child("hello")
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {


                            var ansList = mutableListOf<String>()
                            for (childSnapshot in snapshot.children) {
                                val stringValue = childSnapshot.getValue(String::class.java)

                                if (stringValue != null) {
                                    ansList.add(stringValue)

                                }
                            }
                            if (ansList.isNotEmpty()) {

                                // Get a random index
                                val randomIndex = (0 until ansList.size).random()

                                // Get the string at the random index
                                ansString = ansList[randomIndex]

                                val randomdelayindex=(0 until delaylist.size).random()

                                 val runnable = Runnable {
                                    recivechat(ansString)

                                }
                                handler.postDelayed(runnable,delaylist[randomdelayindex])
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            } else {

                    println("mmmmmmmmmmmmmmmmmmmmm"+message)
                databseReference.child("question ").child(message.replace(" ", "").toLowerCase())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val ansList = mutableListOf<String>()
                            for (childSnapshot in snapshot.children) {
                                val stringValue = childSnapshot.getValue(String::class.java)
                                if (stringValue != null) {
                                    ansList.add(stringValue)
                                }
                            }
                            if (ansList.isNotEmpty()) {
                                // Get a random index
                                val randomIndex = (0 until ansList.size).random()

                                // Get the string at the random index
                                ansString = ansList[randomIndex]

                                val randomdelayindex=(0 until delaylist.size).random()
                                val runnable = Runnable {
                                    recivechat(ansString)
                                    println("fffffffffffffffffffff")

                                }
                                handler.postDelayed(runnable,delaylist[randomdelayindex])
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

            }



        }
    }

    private fun recivechat(chatmessage:String){
        if (chatmessage.isNotEmpty()) {
            var chat_id = databseReference.child("chat").push().key.toString()


            var dataMap: HashMap<String, Any> = HashMap<String, Any>()
            dataMap["sender_user_id"] = met_user_id
            dataMap["receiver_user_id"] = currentUser.uid
            dataMap["message"] = chatmessage
            dataMap["chat_id"] = chat_id
            dataMap["timestamp"] = System.currentTimeMillis()
            dataMap["met_user_id"] = currentUser.uid
            dataMap["type"] = ""


            databseReference.child("chats").child(met_user_id).child(currentUser.uid)
                .child(chat_id)
                .setValue(dataMap).addOnSuccessListener {
                    databseReference.child("chats").child(currentUser.uid).child(met_user_id)
                        .child(chat_id).setValue(dataMap)
                    databseReference.child("recent_message").child(currentUser.uid)
                        .child(met_user_id).setValue(dataMap)
                    databseReference.child("recent_message").child(met_user_id)
                        .child(currentUser.uid).setValue(dataMap)
                    if (!isMetUserOnline) {
                        databseReference.child("notifications").child(currentUser.uid)
                            .child(met_user_id).child(chat_id).setValue(dataMap)
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "reciveChat: ", it)

                }


        }
    }
}