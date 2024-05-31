package com.talk.walk.Activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.talk.walk.R

import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.talk.walk.BuildConfig
import com.talk.walk.Utils.Constants

//import com.pushbots.push.Pushbots


class SplashScreenActivity : AppCompatActivity() {

    private val TAG: String? = SplashScreenActivity::class.java.name
    private lateinit var auth: FirebaseAuth
    private lateinit var tvAppVersion: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        sharedPreferences = getSharedPreferences(Constants.Keys.DARK_MODE, MODE_PRIVATE)

        if (sharedPreferences.contains(Constants.Keys.IS_DARK_MODE_ENABLED)) {
            val isDarkModeEnabled = sharedPreferences.getBoolean(Constants.Keys.IS_DARK_MODE_ENABLED, false)
            if (isDarkModeEnabled) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

//        startService(Intent(applicationContext, AppService::class.java))
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            val w: Window = window
//            w.setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//            )
//            window.statusBarColor = ContextCompat.getColor(this,R.color.white);
//        }
        tvAppVersion = findViewById(R.id.tvAppVersion)
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth
        val currentUser = auth.currentUser
        tvAppVersion.text = "ver." + BuildConfig.VERSION_NAME
        Handler(Looper.getMainLooper()).postDelayed({
            /* Create an Intent that will start the Menu-Activity. */
            if (currentUser == null) {
                val mainIntent = Intent(this, SignUpActivity::class.java)
                startActivity(mainIntent)
                finish()
            } else{
                val mainIntent = Intent(this, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
        }, 3000)


    }
}