package com.talk.walk.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Activities.MatchPeopleActivity
import com.talk.walk.Adapters.InboxAdapter
import com.talk.walk.Models.Chat
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.*
import com.talk.walk.Utils.Controller
import java.util.ArrayList
import kotlin.properties.Delegates


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment(), InboxAdapter.OnItemChange, InboxAdapter.OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val TAG: String? = InboxAdapter::class.java.name

    private lateinit var mContext: Context

    private lateinit var bChatMeetNewPeople: Button
    private lateinit var rvInbox: RecyclerView
    private lateinit var srlChat: SwipeRefreshLayout
    private lateinit var tvNoChats: TextView

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private var chatList: ArrayList<Chat> = mutableListOf<Chat>() as ArrayList<Chat>
    var hashSet = HashSet<String>()
    private lateinit var inboxAdapter: InboxAdapter
    private var inboxPositon by Delegates.notNull<Int>()

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
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = view.context
        bChatMeetNewPeople = view.findViewById(R.id.bChatMeetNewPeople)
        rvInbox = view.findViewById(R.id.rvInbox)
        srlChat = view.findViewById(R.id.srlChat)
        tvNoChats = view.findViewById(R.id.tvNoChats)

        tvNoChats.visibility = View.GONE

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        inboxAdapter = InboxAdapter(mContext, chatList, fragmentManager, this)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        rvInbox.layoutManager = layoutManager
        rvInbox.adapter = inboxAdapter

        bChatMeetNewPeople.setOnClickListener {
            val meetNewPeopleIntent = Intent(activity, MatchPeopleActivity::class.java)
            startActivity(meetNewPeopleIntent)
        }
        chatList.clear()
        rvInbox.recycledViewPool.clear()
        inboxAdapter.notifyDataSetChanged()
        srlChat.setOnRefreshListener {
            getInbox()
        }

        val adView = AdView(mContext)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = Controller.ADMOB_BANNER_ID
        MobileAds.initialize(mContext) {}
        val mAdView: AdView = view.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun getInbox() {
//        srlChat.isRefreshing = true
        databseReference.child("recent_message").child(currentUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    tvNoChats.visibility = View.GONE
                    chatList.clear()
                    for (ds in snapshot.children.iterator()) {
                        val key = ds.key.toString()
                        tvNoChats.visibility = View.GONE
                        databseReference.child("recent_message").child(currentUser.uid).child(key).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val chat = snapshot.getValue(Chat::class.java)
                                    if (chat != null) {
                                        chatList.add(chat)
                                        chatList.sortWith(Comparator { e1, e2 ->
                                            e2.timestamp.compareTo(e1.timestamp)
                                        })
                                        chat.user_key = key
                                        tvNoChats.visibility = View.GONE
                                    }
                                    rvInbox.visibility = View.VISIBLE
                                    srlChat.isRefreshing = false
                                    inboxAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })

                    }
                } else {
                    srlChat.isRefreshing = false
                    tvNoChats.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.details}")

            }

        })


//        databseReference.child("recent_message").child(currentUser.uid).addChildEventListener(object: ChildEventListener {
//            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//                val chatMessage = p0.getValue(Chat::class.java) ?: return
//                latestMessagesMap[p0.key!!] = chatMessage
//                refreshRecyclerViewMessages()
//            }
//
//            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//                val chatMessage = p0.getValue(Chat::class.java) ?: return
//                latestMessagesMap[p0.key!!] = chatMessage
//                refreshRecyclerViewMessages()
//            }
//
//            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//
//            }
//            override fun onChildRemoved(p0: DataSnapshot) {
//
//            }
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//        })

    }

    override fun onResume() {
        super.onResume()
        getInbox()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        chatList.clear()
    }

    override fun onStop() {
        super.onStop()
        chatList.clear()
        inboxAdapter.notifyDataSetChanged()
    }

    override fun onDetach() {
        super.onDetach()
        chatList.clear()
        inboxAdapter.notifyDataSetChanged()
    }

    override fun notifyAdapter(position: Int) {
        this.inboxPositon = position
    }

    override fun onItemClick(
        holder: InboxAdapter.ViewHolder,
        chat: Chat,
        chatList: MutableList<Chat>,
        position: Int
    ) {
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

        tvDialogTwoButtonHeader.text = "Delete Conversation?"
        tvDialogTwoButtonMessage.text =
            "Are you sure you want to delete this conversation? By deleting you cannot restore it."

        tvDeleteCancel.setOnClickListener {
            dialog.dismiss()
        }

        tvDeleteOkay.setOnClickListener {
            val query: Query
            query = if (currentUser.uid == chat.sender_user_id) {
                databseReference.child("chats").child(chat.sender_user_id).child(chat.receiver_user_id)
            } else {
                databseReference.child("chats").child(chat.receiver_user_id).child(chat.sender_user_id)
            }
            val query2: Query
            query2 = if (currentUser.uid == chat.sender_user_id) {
                databseReference.child("recent_message").child(chat.sender_user_id).child(chat.receiver_user_id)
            } else {
                databseReference.child("recent_message").child(chat.receiver_user_id).child(chat.sender_user_id)

            }
            query.setValue(null).addOnSuccessListener {
                if (chat.sender_user_id == currentUser.uid) {
                    databseReference.child("already_met").child(chat.sender_user_id).child(chat.receiver_user_id).setValue(null)
                }
                query2.setValue(null)
                Toast.makeText(mContext, "Chat deleted", Toast.LENGTH_LONG).show()
                dialog.dismiss()
                chatList.clear()
                rvInbox.recycledViewPool.clear()
                getInbox()
            }
        }
    }
}