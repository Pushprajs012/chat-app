package com.talk.walk.Utils

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.app.*
import android.content.Context
import android.content.Intent

import android.graphics.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.talk.walk.Activities.MainActivity
import com.talk.walk.R
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG: String? = MyFirebaseMessagingService::class.java.name
    private lateinit var mContext: Context

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private var icon_bitmap: Bitmap? = null

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        auth = Firebase.auth
        currentUser = auth.currentUser!!

        mContext = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            showNotification(p0.data)
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private fun showNotification(data: Map<String, String>) {
        val mAuth = FirebaseAuth.getInstance()
        createNotificationChannel()
        val title = data["title"]
        val body = data["body"]
        val icon = data["icon"]
        val type = data["type"]
        val to_user_id = data["to_user_id"]
        val from_user_id = data["from_user_id"]
        if (currentUser != null) {
            icon_bitmap = getBitmapfromUrl(icon)
            if (type == "message_notification") {


                // Create an Intent for the activity you want to start
                val resultIntent = Intent(this, MainActivity::class.java)
                resultIntent.putExtra("messageNotification", "messageNotification")
                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                val stackBuilder = TaskStackBuilder.create(this)
                stackBuilder.addNextIntentWithParentStack(resultIntent)
                // Get the PendingIntent containing the entire back stack
                val resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "1")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setLargeIcon(icon_bitmap?.let { getCircleBitmap(it) })
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                builder.setContentIntent(resultPendingIntent)
                val notificationId = 1
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(1, builder.build())

                val i = Intent("chat_fragment")
                i.action = "refresh_chat"
                mContext.sendBroadcast(i)



                /*
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.burnab_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(getCircleBitmap(icon_bitmap))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        int notificationId = 1;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build()); */
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.default_notification_channel_id)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1", name, importance)
            channel.description = description
            channel.enableLights(true)
            channel.setShowBadge(true)
            channel.vibrationPattern = longArrayOf(0, 100)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getBitmapfromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            null
        }
    }

    fun getCircleBitmap(bitmap: Bitmap): Bitmap? {
        val output: Bitmap
        val srcRect: Rect
        val dstRect: Rect
        val r: Float
        val width = bitmap.width
        val height = bitmap.height
        if (width > height) {
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888)
            val left = (width - height) / 2
            val right = left + height
            srcRect = Rect(left, 0, right, height)
            dstRect = Rect(0, 0, height, height)
            r = (height / 2).toFloat()
        } else {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
            val top = (height - width) / 2
            val bottom = top + width
            srcRect = Rect(0, top, width, bottom)
            dstRect = Rect(0, 0, width, width)
            r = (width / 2).toFloat()
        }
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(r, r, r, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
        bitmap.recycle()
        return output
    }

    @SuppressLint("LongLogTag")
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.d(TAG, s)
    }


}