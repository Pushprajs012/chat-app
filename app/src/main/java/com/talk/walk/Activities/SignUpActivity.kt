package com.talk.walk.Activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import java.util.*
import kotlin.collections.HashMap

class SignUpActivity : AppCompatActivity() {

    private val TAG: String? = SignUpActivity::class.java.name
    private lateinit var rgSignUpGender: RadioGroup
    private lateinit var rbSignUpGenderMale: RadioButton
    private lateinit var rbSignUpGenderFemale: RadioButton
    private lateinit var rbSignUpGenderOthers: RadioButton
    private lateinit var etSignupUsername: EditText
    private lateinit var bSignUp: Button
    private lateinit var pbSignUp: ProgressBar
    private lateinit var ivSignUpBG: ImageView

    private lateinit var username: String
    private var gender: String = ""
    private var isAlreadyAvailable : Boolean = false

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    var deepLink: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etSignupUsername = findViewById(R.id.etSignupUsername);
        rgSignUpGender = findViewById(R.id.rgSignUpGender);
        rbSignUpGenderMale = findViewById(R.id.rbSignUpGenderMale);
        rbSignUpGenderFemale = findViewById(R.id.rbSignUpGenderFemale);
        rbSignUpGenderOthers = findViewById(R.id.rbSignUpGenderOthers);
        bSignUp = findViewById(R.id.bSignUp)
        pbSignUp = findViewById(R.id.pbSignUp)
        ivSignUpBG = findViewById(R.id.ivSignUpBG)

        auth = Firebase.auth

        pbSignUp.visibility = View.GONE
        bSignUp.visibility = View.VISIBLE

        rgSignUpGender.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                gender = (findViewById<View>(checkedId) as RadioButton).getText().toString()
//                (findViewById<View>(checkedId) as RadioButton).setButtonTintList(ColorStateList.valueOf(
//                    ContextCompat.getColor(this, R.color.colorPrimary)))
//                (findViewById<View>(checkedId) as RadioButton).setTextColor(resources.getColor(R.color.colorPrimary))
                Log.e(TAG, "onCreate: $checkedId")
            }
        }

        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    Log.e(TAG, "pendingDynamicLinkData onCreate: $deepLink")
                }

                // Handle the deep link. For example, open the linked
                // content, or apply promotional credit to the user's
                // account.
                // ...

                // ...
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }


        bSignUp.setOnClickListener {
            Log.e(TAG, "onCreate: " + deepLink.toString())
            username = etSignupUsername.text.toString()
            pbSignUp.visibility = View.VISIBLE
            bSignUp.visibility = View.GONE
            Controller.hideKeyboard(this)

            when {
                username.isEmpty() -> {
                    etSignupUsername.error = "Please enter name"
                    pbSignUp.visibility = View.GONE
                    bSignUp.visibility = View.VISIBLE
                }
                username.contains(resources.getString(R.string.account_deleted)) || username == resources.getString(R.string.account_deleted) -> {
                    Toast.makeText(this, "You cannot have that username", Toast.LENGTH_LONG).show()
                    pbSignUp.visibility = View.GONE
                    bSignUp.visibility = View.VISIBLE
                }
                gender.isEmpty() -> {
                    Toast.makeText(this, "Please select your gender", Toast.LENGTH_LONG).show()
                    pbSignUp.visibility = View.GONE
                    bSignUp.visibility = View.VISIBLE
                }
                else -> {
                    FirebaseInstallations.getInstance().id.addOnCompleteListener { task: Task<String?> ->
                        if (task.isSuccessful) {
                            val token = task.result
                            Log.e("token ---->>", token!!)
                            databseReference.child("device_blocked").child(token).addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {

                                        Controller.showTwoButtonDialog(this@SignUpActivity, "Blocked", "You are not authorizes to use this app.", true)
                                    } else {
                                        registerUser()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    registerUser()
                                }

                            })
                        } else {
                            registerUser()
                        }
                    }


                }
            }
        }
    }

    private fun registerUser() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    pbSignUp.visibility = View.GONE
                    bSignUp.visibility = View.VISIBLE
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed. " + task.exception,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task: Task<String?> ->
            if (task.isSuccessful) {
                val token = task.result
                Log.e("token ---->>", token!!)

                var dataMap: HashMap<String, Any> = HashMap<String, Any>()
                if (user != null) {
                    dataMap["user_id"] = user.uid
                    dataMap["username"] = username.toLowerCase()
                    dataMap["gender"] = gender
                    dataMap["device_token"] = token
                    dataMap["is_searching"] = false
                    dataMap["is_online"] = true
                    dataMap["is_user_blocked"] = false
                    if (deepLink == null) {
                        dataMap["points"] = 30
                    } else {
                        dataMap["points"] = 30 + 50
                    }

                    databseReference.child("users").child(user.uid).setValue(dataMap).addOnSuccessListener {
//                        databseReference.child("usernames").child(username).child("username").setValue(username)
                        Toast.makeText(this@SignUpActivity, "Successfully registered", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

            } else {
                pbSignUp.visibility = View.GONE
                bSignUp.visibility = View.VISIBLE
            }
        }


    }
}

