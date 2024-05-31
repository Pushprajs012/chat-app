package com.talk.walk.Utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AppService: Service() {

    private val TAG: String? = AppService::class.java.name

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { onTaskRemoved(it) }
        auth = Firebase.auth
        if (auth.currentUser != null) {
            currentUser = auth.currentUser!!
        }

        Thread {
            startJob()
        }

        return START_STICKY
    }

    private fun startJob() {
        try {
            Thread.sleep(900000)
            deleteMediaMessages()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        startJob()
    }

    private fun deleteMediaMessages() {
        Log.d(TAG, "onStartCommand: AppService Running!")
        databseReference.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children.iterator()) {
                    val key = i.key.toString()
                    databseReference.child("chats").child(key).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (j in snapshot.children.iterator()) {
                                val secondKey = j.key.toString()
                                databseReference.child("chats").child(key).child(secondKey).addChildEventListener(object : ChildEventListener {
                                    override fun onChildAdded(
                                        snapshot: DataSnapshot,
                                        previousChildName: String?,
                                    ) {
                                        Log.d(TAG, "onChildAdded: ${snapshot.child("timestamp").value as? Long}")
                                        if (snapshot.child("timestamp").exists()) {
                                            val timestamp: Long = (snapshot.child("timestamp").getValue() as? Long)!!
                                            val chat_id = snapshot.key.toString()
                                            if (Math.abs(timestamp - System.currentTimeMillis()) >= 10000) {
                                                if (snapshot.child("media_url").exists()) {
                                                    databseReference.child("chats").child(key).child(secondKey)
                                                        .child(chat_id).setValue(null)
                                                    databseReference.child("chats").child(secondKey).child(key)
                                                        .child(chat_id).setValue(null)

                                                }
                                            }
                                        }

                                    }

                                    override fun onChildChanged(
                                        snapshot: DataSnapshot,
                                        previousChildName: String?,
                                    ) {

                                    }

                                    override fun onChildRemoved(snapshot: DataSnapshot) {

                                    }

                                    override fun onChildMoved(
                                        snapshot: DataSnapshot,
                                        previousChildName: String?,
                                    ) {

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e(TAG, "onCancelled: ${error.details}")
                                    }

                                })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "onCancelled: ${error.details}")
                        }

                    })
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.details}")
            }

        })
    }

    override fun onBind(p0: Intent?): IBinder? {


        return null
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }
}