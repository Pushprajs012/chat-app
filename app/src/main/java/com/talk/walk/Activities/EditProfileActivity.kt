package com.talk.walk.Activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.talk.walk.R
import com.talk.walk.Utils.Constants
import com.talk.walk.Utils.Controller
import com.talk.walk.Utils.FileUtil
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class EditProfileActivity : AppCompatActivity(), CoroutineScope {


    private val TAG: String? = EditProfileActivity::class.java.name
    private val GALLERY_REQUEST_CODE: Int = 1

    private lateinit var mContext: Context
    private lateinit var civProfilePic: CircleImageView
    private lateinit var tvChangeProfilePic: TextView
    private lateinit var etSignupUsername: EditText
    private lateinit var rgSignUpGender: RadioGroup
    private lateinit var rbSignUpGenderMale: RadioButton
    private lateinit var rbSignUpGenderFemale: RadioButton
    private lateinit var rbSignUpGenderOthers: RadioButton
    private lateinit var bSaveProfile: Button
    private lateinit var tvDeleteAccount: TextView
    private lateinit var ibBack: ImageButton

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private var ds_username: String = ""
    private lateinit var gender: String
    private var job: Job = Job()
    var firstLoopCounter = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        mContext = this

        ibBack = findViewById(R.id.ibBack)
        civProfilePic = findViewById(R.id.civProfilePic)
        tvChangeProfilePic = findViewById(R.id.tvChangeProfilePic)
        etSignupUsername = findViewById(R.id.etSignupUsername)
        rgSignUpGender = findViewById(R.id.rgSignUpGender)
        rbSignUpGenderMale = findViewById(R.id.rbSignUpGenderMale)
        rbSignUpGenderFemale = findViewById(R.id.rbSignUpGenderFemale)
        rbSignUpGenderOthers = findViewById(R.id.rbSignUpGenderOthers)
        bSaveProfile = findViewById(R.id.bSaveProfile)
        tvDeleteAccount = findViewById(R.id.tvDeleteAccount)

        auth = Firebase.auth
        if (auth.currentUser != null) {
            currentUser = auth.currentUser!!
            databseReference.child(Constants.Keys.USERS).child(currentUser.uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var username = snapshot.child(Constants.Keys.USERNAME).value.toString()
                        var gender = snapshot.child(Constants.Keys.GENDER).value.toString()
                        ds_username = username
                        etSignupUsername.setText(username)
                        when (gender) {
                            Constants.Values.MALE -> {
                                rbSignUpGenderMale.isChecked = true
                            }
                            Constants.Values.FEMALE -> {
                                rbSignUpGenderFemale.isChecked = true
                            }
                            else -> {
                                rbSignUpGenderOthers.isChecked = true
                            }
                        }

                        if (snapshot.child(Constants.Keys.PROFILE_IMAGE).exists()) {
//                            Glide.with(mContext).load(snapshot.child(Constants.Keys.PROFILE_IMAGE).value.toString()).error(R.drawable.user).into(civProfilePic)
                            Picasso.get().load(snapshot.child(Constants.Keys.PROFILE_IMAGE).value.toString()).error(R.drawable.user).into(civProfilePic)
                        } else {
                            Glide.with(mContext).load(R.drawable.user).fitCenter().into(civProfilePic)
                            if (Controller.isDarkTheme(this@EditProfileActivity)) {
                                civProfilePic.setColorFilter(Color.WHITE)
                            } else {
                                civProfilePic.setColorFilter(Color.BLACK)
                            }
                        }


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.details}")
                }

            })


            civProfilePic.setOnClickListener {
                checkStoragePermission()
            }

            tvChangeProfilePic.setOnClickListener {
                checkStoragePermission()
            }

            rgSignUpGender.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1) {
                    gender = (findViewById<View>(checkedId) as RadioButton).getText().toString()
                }
            }

            rbSignUpGenderFemale.isEnabled = false
            rbSignUpGenderMale.isEnabled = false
            rbSignUpGenderOthers.isEnabled = false

            bSaveProfile.setOnClickListener {
                var username = etSignupUsername.text.toString()
                Log.e(TAG, "onDataChange: $ds_username")
//                if (ds_username == username) {
//                    updateProfile(username)
//                } else {
//                    checkUsername(username);
//                }
                updateProfile(username)

            }

            tvDeleteAccount.setOnClickListener {
                val dialog = Dialog(mContext)
                dialog.setContentView(R.layout.dialog_delete_account_layout)
                dialog.window!!.setLayout(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
                dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()

                val tvDeleteCancel: TextView = dialog.findViewById(R.id.tvDeleteCancel)
                val tvDeleteOkay:TextView = dialog.findViewById(R.id.tvDeleteOkay)

                tvDeleteCancel.setOnClickListener {
                    dialog.dismiss()
                }

                tvDeleteOkay.setOnClickListener {
                    databseReference.child("chats").child(currentUser.uid).setValue(null)
                    databseReference.child(Constants.Keys.BLOCKS).child(currentUser.uid).setValue(null)
                    databseReference.child(Constants.Keys.NOTIFICATIONS).child(currentUser.uid).setValue(null)
                    databseReference.child(Constants.Keys.FRIENDS).child(currentUser.uid).setValue(null)
                    databseReference.child(Constants.Keys.FRIEND_REQUESTS).child(currentUser.uid).setValue(null)
                    databseReference.child("recent_message").child(currentUser.uid).setValue(null)
                    databseReference.child("chat_monitor").child(currentUser.uid).setValue(null)
                    databseReference.child(Constants.Keys.USERS).child(currentUser.uid).setValue(null).addOnSuccessListener {
                        auth.signOut()
                        currentUser.delete()
                        Toast.makeText(mContext, "Your account successfully delete", Toast.LENGTH_LONG).show()
                        val signUpIntent = Intent(mContext, SignUpActivity::class.java)
                        signUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(signUpIntent)
                    }
                }
            }
        } else {
        }

        ibBack.setOnClickListener {
            onBackPressed()
        }

        val adView = AdView(mContext)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = Controller.ADMOB_BANNER_ID
        MobileAds.initialize(mContext) {}
        val mAdView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun updateProfile(username: String) {
        if (username.isEmpty()) {
            etSignupUsername.setError("Name cannot be blank")
        } else {
            var dataMap: HashMap<String, Any> = HashMap<String, Any>()
            dataMap["username"] = username.toLowerCase()
//            dataMap["gender"] = gender
            databseReference.child("users").child(currentUser.uid).updateChildren(dataMap).addOnSuccessListener {
                Toast.makeText(mContext, "Profile updated successfully", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun checkUsername(username: String) {
        databseReference.child("usernames").child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(mContext, "Username already exists", Toast.LENGTH_LONG).show()
                } else {
                    updateProfile(username)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.details}")
            }

        })
    }

    private fun checkStoragePermission() {
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
                            .withTitle("Storage permission")
                            .withMessage("Storage permission is needed to get pictures.")
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

    private fun selectImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select image"
            ),
            GALLERY_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // Get the Uri of data
            val file_uri = data.data
            if (file_uri != null) {
//                if (Controller.isValidContextForGlide(mContext)) {
//                    Glide.with(mContext).load(file_uri).error(R.drawable.user).into(civProfilePic)
//                }
                try {
                    Picasso.get().load(file_uri).error(R.drawable.user).into(civProfilePic)
                } catch (e: Exception) {
                    Log.e(TAG, "onActivityResult: ${e.localizedMessage}")
                }

                launch {
                    val result = uploadMedia(file_uri)
                    onResult(result)
                }
            }
        }
//        else if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            val takenPhoto = BitmapFactory.decodeFile(
//                FileUtil.from(mContext, Uri.fromFile(filePhoto))
//                .toString())
//            getImageUri(mContext, takenPhoto)?.let { uploadMedia(it) }
//        }
    }

    private suspend fun uploadMedia(fileUri: Uri) {
        if (fileUri != null) {
            Toast.makeText(mContext, "Uploading...", Toast.LENGTH_LONG).show()
            var timeInMillis: Long = System.currentTimeMillis()
            val extension: String = MimeTypeMap.getFileExtensionFromUrl(FileUtil.from(mContext, fileUri).absolutePath);
            val compressedImageFile = Compressor.compress(mContext, File(FileUtil.from(mContext, fileUri).absolutePath)) { quality(40) }
            var fileInputStream: FileInputStream? = FileInputStream(compressedImageFile)
            val refStorage = FirebaseStorage.getInstance().reference.child("profile/images/${currentUser.uid}/${currentUser.uid}.$extension")
            if (fileInputStream != null) {
                refStorage.putStream(fileInputStream)
                    .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            val imageUrl = it.toString()
                            databseReference.child(Constants.Keys.USERS).child(currentUser.uid).child(Constants.Keys.PROFILE_IMAGE).setValue(imageUrl)
                        }
                    })

                    .addOnFailureListener(OnFailureListener { e ->
                        print(e.message)
                    })
            }
        }
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
}