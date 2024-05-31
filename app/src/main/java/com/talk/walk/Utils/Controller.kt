package com.talk.walk.Utils

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.LifecycleObserver
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.IOException
import java.io.InputStream

import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.talk.walk.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


open class Controller : Application(), LifecycleObserver {

    private val TAG: String? = Controller::class.java.name


    companion object {
        var canSendFriendRequest = false
        var isPaid = false
        var const_receiver_user_id = ""
        var const_sender_user_id = ""
        var isChatUserBlocked = false
        var blocked_met_user_id = ""
        var blocked_user_id = ""
        lateinit var mainActivity: Activity
        var isTwoButtonDialogShown = false
        var shouldClearInboxList = false
        var BASE_URL = ""

        var ADMOB_BANNER_ID = ""
        var ADMOB_INTERSTITAL_ID = ""

        @Throws(IOException::class)
        open fun  handleSamplingAndRotationBitmap(context: Context, selectedImage: Uri?): Bitmap? {
            val MAX_HEIGHT = 1024
            val MAX_WIDTH = 1024

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            var imageStream: InputStream? = selectedImage?.let {
                context.getContentResolver().openInputStream(
                    it
                )
            }
            BitmapFactory.decodeStream(imageStream, null, options)
            imageStream?.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            imageStream = selectedImage?.let { context.contentResolver.openInputStream(it) }
            var img = BitmapFactory.decodeStream(imageStream, null, options)
            img = rotateImageIfRequired(context, img, selectedImage)
            return img
        }

        open fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int, reqHeight: Int
        ): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

                // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
                // with both dimensions larger than or equal to the requested height and width.
                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

                // This offers some additional logic in case the image has a strange
                // aspect ratio. For example, a panorama may have a much larger
                // width than height. In these cases the total pixels might still
                // end up being too large to fit comfortably in memory, so we should
                // be more aggressive with sample down the image (=larger inSampleSize).
                val totalPixels = (width * height).toFloat()

                // Anything more than 2x the requested pixels we'll sample down further
                val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
                while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                    inSampleSize++
                }
            }
            return inSampleSize
        }

        @Throws(IOException::class)
        open fun rotateImageIfRequired(
            context: Context,
            img: Bitmap?,
            selectedImage: Uri?
        ): Bitmap? {
            val input = context.contentResolver.openInputStream(selectedImage!!)
            val ei: ExifInterface = if (Build.VERSION.SDK_INT > 23) input?.let { ExifInterface(it) }!! else selectedImage.path?.let { ExifInterface(it) }!!
            return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img!!, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img!!, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img!!, 270)
                else -> img
            }
        }

        open fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
            img.recycle()
            return rotatedImg
        }

        fun getRandomColor(): Int {
            val rnd = Random()
            return Color.argb(170, rnd.nextInt(200), rnd.nextInt(200), rnd.nextInt(200))
        }

        fun isDarkTheme(activity: Activity): Boolean {
            return activity.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }

        fun showTwoButtonDialog(mContext: Context, title: String, message: String, isHideSecondButton: Boolean) {
            val dialog = Dialog(mContext)
            dialog.setContentView(R.layout.dialog_delete_account_layout)
            dialog.window!!.setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()

            val tvDialogTwoButtonMessage: TextView = dialog.findViewById(R.id.tvDialogTwoButtonMessage)
            val tvDialogTwoButtonHeader: TextView = dialog.findViewById(R.id.tvDialogTwoButtonHeader)
            val tvDeleteCancel: TextView = dialog.findViewById(R.id.tvDeleteCancel)
            val tvDeleteOkay: TextView = dialog.findViewById(R.id.tvDeleteOkay)

            tvDialogTwoButtonHeader.text = title
            tvDialogTwoButtonMessage.text = message

            if (isHideSecondButton) {
                tvDeleteCancel.visibility = View.INVISIBLE
            } else {
                tvDeleteCancel.visibility = View.VISIBLE
            }

            tvDeleteCancel.setOnClickListener {
                dialog.dismiss()
            }

            tvDeleteOkay.setOnClickListener {
                dialog.dismiss()
                (mContext as Activity).finish()
            }
        }

        /**
         * Return true if this [Context] is available.
         * Availability is defined as the following:
         * + [Context] is not null
         * + [Context] is not destroyed (tested with [FragmentActivity.isDestroyed] or [Activity.isDestroyed])
         */
        open fun isValidContextForGlide(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            if (context is Activity) {
                val activity = context
                if (activity.isDestroyed || activity.isFinishing) {
                    return false
                }
            }
            return true
        }

        fun showKeyboard(activity: Activity) {
            (activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?)?.toggleSoftInput(
                InputMethodManager.SHOW_IMPLICIT,
                0
            )
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun getDate(time : Long) : String {
            val cal = Calendar.getInstance();
            val tz = cal.getTimeZone();//get your local time zone.
            var sdf = SimpleDateFormat("hh:mm a");
            sdf.setTimeZone(tz);//set time zone.
            val localTime = sdf.format(Date(time))
            var date = Date();
            try {
                date = sdf.parse(localTime);//get local date
            } catch (e : ParseException) {
                e.printStackTrace();
            }
            val time = sdf.format(date)
            return time;
        }
    }


}