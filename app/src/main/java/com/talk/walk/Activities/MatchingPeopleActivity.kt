package com.talk.walk.Activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
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
import com.talk.walk.Models.MatchPeople
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller

class MatchingPeopleActivity : AppCompatActivity() {

    private var foundSomeone: Boolean = false
    private val TAG: String? = MatchingPeopleActivity::class.java.name
    private lateinit var mContext: Context

    private lateinit var bStopWaiting: Button
    private lateinit var ibBack: ImageButton

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    var counter = 0
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var gender: String
    private var matchOnlyOnline:Boolean = false
    private var haveFoundMatch: Boolean = false
    private var loopCounterUsers = 0L
    private var loopCounterAlreadyMet = 0L
    private var profile_points = 0L
    private var usersHasReachedEnd = false
    private var alreadyMetReachedEnd = false
    private var matchingPeopleList: MutableList<MatchPeople> = mutableListOf()
    private var usersList1: MutableList<String> = mutableListOf()
    private var alreadyMetList2: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching_people)

        mContext = this

        gender = intent.getStringExtra("gender")!!
        profile_points = intent.getLongExtra("profile_points", 0L)
        matchOnlyOnline = intent.getBooleanExtra("matchOnlyOnline",false)

        bStopWaiting = findViewById(R.id.bStopWaiting)
        ibBack = findViewById(R.id.ibBack)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        if (currentUser == null) {
            Toast.makeText(mContext, "Please login again", Toast.LENGTH_LONG).show()
            finish()
        } else {
            countDownTimer = object :  CountDownTimer(30000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d(TAG, "onTick: $counter")
                    counter++
                }
                override fun onFinish() {
                    Toast.makeText(mContext, "Could not find new people. Please try again later", Toast.LENGTH_LONG).show()
                    finish()
                }
            }.start()
            var query = databseReference.child("users")
            var query2 = databseReference.child("already_met").child(currentUser.uid)
//            databseReference.child("already_met").child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot6: DataSnapshot) {
//                    if (snapshot6.exists()) {
//                        for ()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (i in snapshot.children.iterator()) {
                            usersList1.add(i.key!!)
                            if (loopCounterUsers == snapshot.childrenCount-1) {
                                usersHasReachedEnd = true
                                query2.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot2: DataSnapshot) {
                                        if (snapshot2.exists()) {
                                            for (j in snapshot2.children.iterator()) {
                                                alreadyMetList2.add(j.key!!)
                                                if (loopCounterAlreadyMet == snapshot2.childrenCount-1) {
                                                    alreadyMetReachedEnd = true
                                                    if (usersHasReachedEnd && alreadyMetReachedEnd) {
                                                        onCall()
                                                    }
                                                }
                                                loopCounterAlreadyMet++
                                            }
                                            Log.e(TAG, "onCreate2: $alreadyMetList2")

                                        } else if (usersHasReachedEnd) {
                                            alreadyMetReachedEnd = true
                                            onCall()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                })
                                break
                            }
                            loopCounterUsers++
                        }
                        Log.e(TAG, "onCreate1: $usersList1")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
//            Log.e(TAG, "onCreate: $difference")
//            query.addListenerForSingleValueEvent(object: ValueEventListener {
//                override fun onDataChange(snapshot4: DataSnapshot) {
//                    if (snapshot4.exists()) {
//                        for (users_snapshot in snapshot4.children.iterator()) {
//                            if (!foundSomeone) {
//                                val rand = Random()
//                                val abcd: Int = rand.nextInt(users_snapshot.childrenCount.toInt() - 0) + 1
//                                Log.d(TAG, "onDataChange: $abcd")
//                                query2.limitToLast(abcd).addListenerForSingleValueEvent(object : ValueEventListener {
//                                    override fun onDataChange(snapshot2: DataSnapshot) {
//                                        if (snapshot2.exists() && !foundSomeone) {
//                                            for (met_user_data in snapshot2.children.iterator()) {
//                                                val met_user_key = met_user_data.key.toString()
//                                                if (users_snapshot.child("user_id").value.toString() != met_user_key) {
//                                                    matchingPeopleList.add(MatchPeople(users_snapshot.child("user_id")))
//                                                }
//
//
//
//                                                if (users_snapshot.child("user_id").value.toString() != currentUser.uid &&
//                                                    users_snapshot.child("user_id").value.toString() != met_user_key &&
//                                                    currentUser.uid != met_user_data.child("met_user_id").value) {
//                                                    if (users_snapshot.child("gender").value.toString() == gender) {
//
//                                                        if (loopCounter == snapshot2.childrenCount.toInt()-1) {
//                                                            if (gender != Constants.Keys.ANYONE) {
//                                                                databseReference.child("users").child(currentUser.uid).child("is_searching").setValue(true)
//                                                                if (gender == Constants.Values.MALE || gender == Constants.Values.FEMALE) {
//                                                                    databseReference.child("users").child(currentUser.uid).child("points").setValue(profile_points-10)
//                                                                    foundSomeone = true
//                                                                    query.removeEventListener(this)
//                                                                    query2.removeEventListener(this)
//                                                                    countDownTimer.cancel()
//                                                                    databseReference.child("already_met").child(currentUser.uid).child(users_snapshot.child("user_id").value.toString()).child("met_user_id").setValue(users_snapshot.child("user_id").value.toString())
//                                                                    databseReference.child("users").child(users_snapshot.child("user_id").value.toString()).child("is_searching").setValue(false)
//                                                                    val chatIntent = Intent(mContext, ChatActivity::class.java)
//                                                                    chatIntent.putExtra("met_user_id", users_snapshot.child("user_id").value.toString())
//                                                                    startActivity(chatIntent)
//                                                                    finish()
//                                                                    Log.d(TAG, "onChildAdded: " + users_snapshot.child("username").value.toString())
//                                                                    break
//                                                                }
//                                                            }
//
//                                                        }
//                                                        loopCounter++
//                                                    } else {
//                                                        if (gender != Constants.Keys.ANYONE) {
//                                                            databseReference.child("users").child(currentUser.uid).child("is_searching").setValue(true)
//                                                            if (gender == Constants.Values.MALE || gender == Constants.Values.FEMALE) {
//                                                                databseReference.child("users").child(currentUser.uid).child("points").setValue(profile_points-10)
//                                                                foundSomeone = true
//                                                                query.removeEventListener(this)
//                                                                query2.removeEventListener(this)
//                                                                countDownTimer.cancel()
//                                                                databseReference.child("already_met").child(currentUser.uid).child(users_snapshot.child("user_id").value.toString()).child("met_user_id").setValue(users_snapshot.child("user_id").value.toString())
//                                                                databseReference.child("users").child(users_snapshot.child("user_id").value.toString()).child("is_searching").setValue(false)
//                                                                val chatIntent = Intent(mContext, ChatActivity::class.java)
//                                                                chatIntent.putExtra("met_user_id", users_snapshot.child("user_id").value.toString())
//                                                                startActivity(chatIntent)
//                                                                finish()
//                                                                Log.d(TAG, "onChildAdded: " + users_snapshot.child("username").value.toString())
//                                                                break
//                                                            }
//                                                        }
//
//                                                    }
//
//                                                }
//                                            }
//                                        } else {
//                                            if (!foundSomeone) {
//                                                if (users_snapshot.child("user_id").value.toString() != currentUser.uid) {
//                                                    if (users_snapshot.child("gender").value.toString() == gender) {
//                                                        if (gender != Constants.Keys.ANYONE) {
//                                                            databseReference.child("users").child(currentUser.uid).child("is_searching").setValue(true)
//                                                            if (gender == Constants.Values.MALE || gender == Constants.Values.FEMALE) {
//                                                                databseReference.child("users").child(currentUser.uid).child("points").setValue(profile_points-10)
//                                                            }
//                                                        }
//                                                        foundSomeone = true
//                                                        query.removeEventListener(this)
//                                                        query2.removeEventListener(this)
//                                                        countDownTimer.cancel()
//                                                        databseReference.child("already_met").child(currentUser.uid).child(users_snapshot.child("user_id").value.toString()).child("met_user_id").setValue(users_snapshot.child("user_id").value.toString())
//                                                        databseReference.child("users").child(users_snapshot.child("user_id").value.toString()).child("is_searching").setValue(false)
//                                                        val chatIntent = Intent(mContext, ChatActivity::class.java)
//                                                        chatIntent.putExtra("met_user_id", users_snapshot.child("user_id").value.toString())
//                                                        startActivity(chatIntent)
//                                                        finish()
//                                                    } else {
//                                                        if (gender != Constants.Keys.ANYONE) {
//                                                            databseReference.child("users").child(currentUser.uid).child("is_searching").setValue(true)
//                                                            if (gender == Constants.Values.MALE || gender == Constants.Values.FEMALE) {
//                                                                databseReference.child("users").child(currentUser.uid).child("points").setValue(profile_points-10)
//                                                            }
//                                                        }
//                                                        foundSomeone = true
//                                                        query.removeEventListener(this)
//                                                        query2.removeEventListener(this)
//                                                        countDownTimer.cancel()
//                                                        databseReference.child("already_met").child(currentUser.uid).child(users_snapshot.child("user_id").value.toString()).child("met_user_id").setValue(users_snapshot.child("user_id").value.toString())
//                                                        databseReference.child("users").child(users_snapshot.child("user_id").value.toString()).child("is_searching").setValue(false)
//                                                        val chatIntent = Intent(mContext, ChatActivity::class.java)
//                                                        chatIntent.putExtra("met_user_id", users_snapshot.child("user_id").value.toString())
//                                                        startActivity(chatIntent)
//                                                        finish()
//                                                    }
//                                                }
//                                            }
//
//                                        }
//                                    }
//
//                                    override fun onCancelled(error: DatabaseError) {
//                                        Log.e(TAG, "onCancelled: addValueEventListener ${error.details}")
//                                    }
//
//                                })
//                            }
//
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e(TAG, "onCancelled: addValueEventListener ${error.details}")
//                }
//
//            })


        }

        bStopWaiting.setOnClickListener {
            onBackPressed()
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

    private fun onCall() {
        if (usersHasReachedEnd && alreadyMetReachedEnd) {
            val difference = usersList1.filterNotIn(alreadyMetList2)
            val difference2 = !usersList1.containsAll(alreadyMetList2)
            for (i in difference) {
                if (i != null) {
                    if (i != currentUser.uid) {
                        databseReference.child("users").child(i)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        if (snapshot.child("is_online").exists()) {
                                            if (snapshot.child("is_online").value as Boolean) {

                                                if (snapshot.child("gender").exists() && !foundSomeone) {
                                                    if (snapshot.child("gender").value.toString() == gender) {
                                                        sendToChat(i)
                                                    } else if (gender == Constants.Keys.ANYONE) {
                                                        sendToChat(i)
                                                    }
                                                }
                                            }
                                        } else {
//                                            finish()
                                        }

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                    }
                }
            }

         }
    }

    private fun sendToChat(i: String) {
        foundSomeone = true
        countDownTimer.cancel()
        if (gender != Constants.Keys.ANYONE) {
            databseReference.child("users").child(currentUser.uid).child("points").setValue(profile_points-10)
        }
        sendChat(i)
        databseReference.child("already_met").child(currentUser.uid).child(i).child("met_user_id").setValue(i)
        databseReference.child("users").child(i).child("is_searching").setValue(false)
        databseReference.child("users").child(currentUser.uid).child("is_searching").setValue(false)

        val chatIntent = Intent(mContext, ChatActivity::class.java)
        chatIntent.putExtra("met_user_id", i)
        startActivity(chatIntent)
        finish()
    }

    private fun sendChat(met_user_id: String) {
        var chat_id = databseReference.child("chat").push().key.toString()
        var dataMap: HashMap<String, Any> = HashMap<String, Any>()
        dataMap["sender_user_id"] = currentUser.uid
        dataMap["receiver_user_id"] = met_user_id
        dataMap["chat_id"] = chat_id
        dataMap["timestamp"] = System.currentTimeMillis()
        dataMap["is_paid"] = false
        dataMap["type"] = ""
        databseReference.child("chats").child(currentUser.uid).child(met_user_id).child(chat_id).setValue(dataMap).addOnSuccessListener {
                databseReference.child("chats").child(met_user_id).child(currentUser.uid).child(chat_id).setValue(dataMap)
            }.addOnFailureListener {
                Log.e(TAG, "sendChat: ", it)
                Toast.makeText(mContext, "Failed to send message. $it", Toast.LENGTH_LONG).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        countDownTimer.cancel()
        databseReference.child("users").child(currentUser.uid).child("is_searching").setValue(false)
    }

    override fun onStart() {
        super.onStart()
        if (!currentUser.isAnonymous) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_LONG).show()
            auth.signOut()
            finish()
        } else {
            databseReference.child("users").child(currentUser.uid).child("is_online").setValue(true)
        }
    }

    private fun <T> Collection<T>.filterNotIn(collection: Collection<T>): Collection<T> {
        val set = collection.toSet()
        return filterNot { set.contains(it) }
    }

    override fun onStop() {
        super.onStop()
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                databseReference.child("users").child(currentUser.uid).child("is_online").setValue(false)
            }, 3500)
        }
    }
}