package com.talk.walk;


import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.talk.walk.Utils.Constants;
import com.talk.walk.Utils.Controller;

//import com.pushbots.push.Pushbots;

public class MyApplication extends Application implements LifecycleObserver {
    private static final String TAG = MyApplication.class.getSimpleName();
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        mDatabase = FirebaseDatabase.getInstance(Constants.Urls.DATABASE_URL).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();

        if (currentUser != null) {
            System.out.println("userrrrrrrrrrrrr"+currentUser.getUid());
            setupUserStatusOnDisconnect();
        }
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    private void onAppBackgrounded() {
//        Log.d("MyApp", "App in background");
//
//        if (mAuth.getCurrentUser() != null) {
//            mDatabase.child(Constants.Keys.USERS).child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        mDatabase.child("users").child(currentUser.getUid()).child("is_online").setValue(false);
//                    } else {
//                        mAuth.signOut();
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }
//
//    }
//
//    private void showUserBlocked() {
//        Controller.Companion.showTwoButtonDialog(this, "Blocked", "You have been blocked", false);
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    private void onAppForegrounded() {
//        Log.d("MyApp", "App in foreground");
//        if (mAuth.getCurrentUser() != null) {
//            mDatabase.child(Constants.Keys.USERS).child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        mDatabase.child("users").child(currentUser.getUid()).child("is_online").setValue(true);
//                    } else {
//                        mAuth.signOut();
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }
//
//    }
    private void setupUserStatusOnDisconnect() {
        DatabaseReference userStatusRef = mDatabase.child(Constants.Keys.USERS).child(currentUser.getUid()).child("is_online");

        // User status set to false when the connection is lost
        userStatusRef.onDisconnect().setValue(false);

        // Optionally, you can also set it to true when the app is started
        userStatusRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Myapp", "User is_online status set to true on app start");
            } else {
                Log.e("Myapp", "Failed to set user is_online status", task.getException());
            }
        });
    }

}