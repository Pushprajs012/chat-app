package com.talk.walk.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Models.Chat
import com.talk.walk.R
import com.talk.walk.Utils.Constants

import android.media.MediaPlayer
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import com.downloader.*
import com.talk.walk.Activities.ImageViewerActivity

import java.lang.Exception


import com.bumptech.glide.request.RequestOptions

import com.talk.walk.Activities.VideoViewerActivity

import android.os.*
import android.widget.*

import com.squareup.picasso.Picasso
import xyz.belvi.blurhash.BlurHash
import xyz.belvi.blurhash.blurPlaceHolder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

import com.google.firebase.storage.FirebaseStorage

import com.talk.walk.Utils.Controller
import jp.wasabeef.glide.transformations.BlurTransformation


class ChatAdapter(var mContext: Context, var chatList: MutableList<Chat>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String? = ChatAdapter::class.java.name
    private val SENDER_VIEW: Int = 1
    private val RECERIVER_VIEW: Int = 2
    private val SENDER_IMAGE_VIDEO_VIEW: Int = 3
    private val RECERIVER_IMAGE_VIDEO_VIEW: Int = 4
    private val SENDER_AUDIO_VIEW: Int = 5
    private val RECERIVER_AUDIO_VIEW: Int = 6
    private val EMPTY_VIEW: Int = 6
    private val CHAT_CONNECTED_INDICATOR_VIEW: Int = 7
    private val SENDER_UPLOAD_PROGRESS_VIEW: Int = 8
    private val RECEIVER_UPLOAD_PROGRESS_VIEW: Int = 9

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()
    private var pause: Boolean = false
    private var startedPlaying: Boolean = false
    private var currentAudioPosition: Int = 0
    private var mp: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1
    private var playingAudioViewHolder: AudioViewHolder? = null
    private lateinit var path: String

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvChat: CardView = itemView.findViewById(R.id.cvChat)
        val tvChatMessage: TextView = itemView.findViewById(R.id.tvChatMessage)
        val tvChatTime: TextView = itemView.findViewById(R.id.tvChatTime)
    }

    private inner class ImageVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvAudioVideo: CardView = itemView.findViewById(R.id.cvAudioVideo)
        val ivAudioVideo: ImageView = itemView.findViewById(R.id.ivAudioVideo)
        val tvImageVideoError: TextView = itemView.findViewById(R.id.tvImageVideoError)
        val tvChatTime: TextView = itemView.findViewById(R.id.tvChatTime)
        val ivVideoPlayIndicator: ImageView = itemView.findViewById(R.id.ivVideoPlayIndicator)

    }

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvChatAudioIndicator: TextView = itemView.findViewById(R.id.tvChatAudioIndicator)
        val tvChatTime: TextView = itemView.findViewById(R.id.tvChatTime)
        val cvChatPlayPauseAudio: CardView = itemView.findViewById(R.id.cvChatPlayPauseAudio)
        val cvChatAudioItem: CardView = itemView.findViewById(R.id.cvChatAudioItem)
        val ivChatAudioPlayPauseIcon: ImageView =
            itemView.findViewById(R.id.ivChatAudioPlayPauseIcon)
    }

    inner class UploadProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvChatUploadProgress: CardView = itemView.findViewById(R.id.cvChatUploadProgress)
        val ivChaUploadProgress: ImageView = itemView.findViewById(R.id.ivChaUploadProgress)
        val pvChatUploadProgress: ProgressBar = itemView.findViewById(R.id.pvChatUploadProgress)
    }

    inner class ChatConnectedIndicatorHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    @JvmName("setChatList1")
    fun setChatList(chatList: MutableList<Chat>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SENDER_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_sender_chat_layout, parent, false)
            return ViewHolder(view)
        } else if (viewType == RECERIVER_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_receiver_chat_layout, parent, false)
            return ViewHolder(view)
        } else if (viewType == SENDER_IMAGE_VIDEO_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_sender_image_video_media_chat_layout, parent, false)
            return ImageVideoViewHolder(view)
        } else if (viewType == RECERIVER_IMAGE_VIDEO_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_receiver_image_video_media_chat_layout, parent, false)
            return ImageVideoViewHolder(view)
        } else if (viewType == SENDER_AUDIO_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_sender_audio_chat_layout, parent, false)
            return AudioViewHolder(view)
        } else if (viewType == RECERIVER_AUDIO_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_receiver_audio_chat_layout, parent, false)
            return AudioViewHolder(view)
        }
        else if (viewType == CHAT_CONNECTED_INDICATOR_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_first_chat_layout, parent, false)
            return ChatConnectedIndicatorHolder(view)
        } else if (viewType == SENDER_UPLOAD_PROGRESS_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_sender_upload_progress, parent, false)
            return UploadProgressViewHolder(view)
        } else if (viewType == RECEIVER_UPLOAD_PROGRESS_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_receiver_upload_progress, parent, false)
            return UploadProgressViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.empty_view, parent, false)
            return UploadProgressViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var chat: Chat = chatList[position]
        auth = Firebase.auth
        currentUser = auth.currentUser!!
        if (position > 0) {
            val viewType: Int = getItemViewType(position)

//            if (Math.abs(chat.timestamp - System.currentTimeMillis()) >= 3600000) {
            if (Math.abs(chat.timestamp - System.currentTimeMillis()) >= 60000) {
                deleteChatMediaAfterTimeout(holder, chat, position, chatList)
            }

            if (viewType == SENDER_VIEW || viewType == RECERIVER_VIEW) {
                setNormalMessage(holder, chat, position, chatList)
            } else if (viewType == SENDER_IMAGE_VIDEO_VIEW || viewType == RECERIVER_IMAGE_VIDEO_VIEW) {
                setImageVideoMessage(holder, chat, position, chatList)
            }  else if (viewType == SENDER_UPLOAD_PROGRESS_VIEW || viewType == RECEIVER_UPLOAD_PROGRESS_VIEW) {
                val holder: UploadProgressViewHolder = (holder as UploadProgressViewHolder)
                holder.pvChatUploadProgress.isIndeterminate = false
                holder.pvChatUploadProgress.progress = chat.upload_progress
                try {
                    Picasso.get().load(chat.video_thumbnail).into(holder.ivChaUploadProgress)
//                    Glide.with(mContext).load(chat.video_thumbnail).into(holder.ivChaUploadProgress)
                } catch (e: Exception) {
                    Log.e(TAG, "onBindViewHolder: ", e)
                }
            } else if (viewType == SENDER_AUDIO_VIEW || viewType == RECERIVER_AUDIO_VIEW)  {
                setAudioMessage(holder, chat, position, chatList)
            }

//            if (chat.receiver_user_id == currentUser.uid && !chat.is_read) {
//                databseReference.child("chats").child(chat.sender_user_id)
//                    .child(chat.receiver_user_id).child(chat.chat_id).child("is_read")
//                    .setValue(true)
//                databseReference.child("chats").child(chat.receiver_user_id).child(chat.sender_user_id).child(chat.chat_id).child("is_read").setValue(true)
//            }
        }

    }

    private fun deleteChatMediaAfterTimeout(
        holder: RecyclerView.ViewHolder,
        chat: Chat,
        position: Int,
        chatList: MutableList<Chat>
    ) {
        if (chat.media_url.isNotEmpty()) {
            databseReference.child("chats").child(chat.receiver_user_id)
                .child(chat.sender_user_id)
                .child(chat.chat_id).child("delete").setValue(true)
            databseReference.child("chats").child(chat.sender_user_id)
                .child(chat.receiver_user_id)
                .child(chat.chat_id).child("delete").setValue(true)
            if (chat.type == Constants.Keys.VIDEO) {
                val storageReference =
                    FirebaseStorage.getInstance().getReferenceFromUrl(chat.media_url)
                storageReference.delete().addOnSuccessListener { // File deleted successfully
                    Log.e("firebasestorage", "onSuccess: deleted file")
                }.addOnFailureListener { // Uh-oh, an error occurred!
                    Log.e("firebasestorage", "onFailure: did not delete file")
                }
            } else {
//                val storageReference =
//                    FirebaseStorage.getInstance().getReferenceFromUrl(chat.media_url)
//                storageReference.delete().addOnSuccessListener { // File deleted successfully
//                    Log.e("firebasestorage", "onSuccess: deleted file")
//                }.addOnFailureListener { // Uh-oh, an error occurred!
//                    Log.e("firebasestorage", "onFailure: did not delete file")
//                }
            }

        }
    }

    private fun setAudioMessage(
        holder: RecyclerView.ViewHolder,
        chat: Chat,
        position: Int,
        chatList: MutableList<Chat>
    ) {
        val holder: AudioViewHolder = (holder as AudioViewHolder)
        holder.cvChatAudioItem.visibility = View.VISIBLE
        holder.tvChatTime.text = Controller.getDate(chat.timestamp)
        if (chat.delete) {
//            Handler(Looper.getMainLooper()).postDelayed({
//                chatList.removeAt(holder.adapterPosition)
//                notifyItemRemoved(holder.adapterPosition)
//                notifyItemRangeRemoved(
//                    holder.adapterPosition,
//                    chatList.size
//                )
//            }, 1500)
            holder.tvChatAudioIndicator.text = "Audio not available"
        } else {
            mp = MediaPlayer()
            holder.cvChatPlayPauseAudio.setOnClickListener {
                if (!chat.delete) {
                    if (startedPlaying) {
                        if (position == currentPlayingPosition) {
                            if (mp?.isPlaying == true) {
                                mp?.pause()
                                playingAudioViewHolder?.ivChatAudioPlayPauseIcon?.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                                playingAudioViewHolder?.tvChatAudioIndicator?.text = "Paused"
                            } else {
                                mp?.start()
                                playingAudioViewHolder?.ivChatAudioPlayPauseIcon?.setImageResource(R.drawable.ic_baseline_pause_24)
                                playingAudioViewHolder?.tvChatAudioIndicator?.text = "Playing"
                            }
                        } else {
                            currentPlayingPosition = position
                            if (mp != null) {
                                playingAudioViewHolder?.let { updateNonPlayingView(it) }
                            }
                            mp?.reset()
                            downloadAudio(holder, chat, position)
                        }
//                    if (startedPlaying && mp.isPlaying) {
//                        holder.ivChatAudioPlayPauseIcon.setImageDrawable(mContext.resources.getDrawable(R.drawable.ic_baseline_play_arrow_24))
//                        holder.tvChatAudioIndicator.text = "Paused"
//                        currentAudioPosition = mp.currentPosition
//                        mp.stop()
//                        pause = true
//                    } else {
//                        holder.ivChatAudioPlayPauseIcon.setImageDrawable(mContext.resources.getDrawable(R.drawable.ic_baseline_pause_24))
//                        holder.tvChatAudioIndicator.text = "Playing"
//                        mp.seekTo(currentAudioPosition)
//                        mp.start()
//                        pause = false
//                    }

                    } else {
                        downloadAudio(holder, chat, position)
                    }
                }

                holder.ivChatAudioPlayPauseIcon.setOnClickListener {
                    if (chat.delete) {
                        Toast.makeText(mContext, "Media is not available", Toast.LENGTH_LONG).show()
                    }
                }


                holder.cvChatAudioItem.setOnLongClickListener {
                    if (currentUser.uid == chat.sender_user_id) {
                        val builder = AlertDialog.Builder(mContext)
                            .setTitle("Delete")
                            .setMessage("Are you sure you want to delete this message? By deleting you cannot restore it.")
                            .setPositiveButton("Yes") { p0, p1 ->
                                databseReference.child("chats").child(chat.sender_user_id)
                                    .child(chat.receiver_user_id).child(chat.chat_id)
                                    .setValue(null).addOnSuccessListener {
                                        databseReference.child("chats").child(chat.receiver_user_id)
                                            .child(chat.sender_user_id).child(chat.chat_id)
                                            .setValue(null)
                                        val storageReference = FirebaseStorage.getInstance()
                                            .getReferenceFromUrl(chat.media_url)
                                        storageReference.delete()
                                            .addOnSuccessListener { // File deleted successfully
                                                Log.e("firebasestorage", "onSuccess: deleted file")
                                            }.addOnFailureListener { // Uh-oh, an error occurred!
                                                Log.e(
                                                    "firebasestorage",
                                                    "onFailure: did not delete file"
                                                )
                                            }
                                        Toast.makeText(mContext, "Chat deleted", Toast.LENGTH_LONG)
                                            .show()
                                        p0?.dismiss()
                                        chatList.removeAt(holder.adapterPosition)
                                        notifyItemRemoved(holder.adapterPosition)
                                        notifyItemRangeRemoved(
                                            holder.adapterPosition,
                                            chatList.size
                                        )
                                    }
                            }.setNegativeButton("No") { p0, p1 -> p0?.dismiss() }
                        builder.show()
                    }
                    false
                }
            }
        }

    }

    private fun setImageVideoMessage(
        holder: RecyclerView.ViewHolder,
        chat: Chat,
        position: Int,
        chatList: MutableList<Chat>
    ) {
        val blurHash: BlurHash = BlurHash(mContext, lruSize = 20, punch = 1F)
        val holder: ImageVideoViewHolder = (holder as ImageVideoViewHolder)
        holder.tvImageVideoError.visibility = View.GONE
        holder.tvChatTime.text = Controller.getDate(chat.timestamp)
        if (chat.media_type == Constants.Values.VIDEO) {
            holder.ivVideoPlayIndicator.visibility = View.VISIBLE
        } else {
            holder.ivVideoPlayIndicator.visibility = View.GONE
        }

        if (chat.media_type == "Gallery" || chat.media_type == "Camera") {
            if (chat.delete) {
                Glide.with(mContext).load(chat.media_url).apply(
                    RequestOptions.bitmapTransform(
                        BlurTransformation(16, 6)
                    )).into(holder.ivAudioVideo)
            } else {
                if (Controller.isValidContextForGlide(mContext)) {
                    Picasso.get().load(chat.media_url)
                        .blurPlaceHolder("LHA-Vc_4s9ad4oMwt8t7RhXTNGRj", holder.ivAudioVideo, blurHash)
                        {
                                request ->
                            request.into(holder.ivAudioVideo)
                        }
                }
            }

        } else {
            if (chat.delete) {
                Glide.with(mContext).load(chat.video_thumbnail).apply(
                    RequestOptions.bitmapTransform(
                        BlurTransformation(16, 6)
                    )).into(holder.ivAudioVideo)
            } else {
                if (chat.video_thumbnail.isNotEmpty()) {
                    Picasso.get().load(chat.video_thumbnail).blurPlaceHolder(
                        "LHA-Vc_4s9ad4oMwt8t7RhXTNGRj",
                        holder.ivAudioVideo,
                        blurHash
                    )
                    { request ->
                        request.into(holder.ivAudioVideo)
                    }
                }
            }

        }

        holder.cvAudioVideo.setOnClickListener {
            if (chat.delete) {
                Toast.makeText(mContext, "Media is not available", Toast.LENGTH_LONG).show()
            } else {
                if (chat.media_type == Constants.Values.VIDEO) {
                    val videoViewIntent = Intent(mContext, VideoViewerActivity::class.java);
                    videoViewIntent.putExtra("media_url", chat.media_url)
                    mContext.startActivity(videoViewIntent)
                } else {
                    val imageViewIntent = Intent(mContext, ImageViewerActivity::class.java);
                    imageViewIntent.putExtra("chat_media_url", chat.media_url)
                    mContext.startActivity(imageViewIntent)
                }
            }
        }
        holder.cvAudioVideo.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(p0: View?): Boolean {
                deleteAudioMessage(holder, chat, chatList, position)
                return false
            }

        })
    }

    private fun deleteAudioMessage(
        holder: ImageVideoViewHolder,
        chat: Chat,
        chatList: MutableList<Chat>,
        position: Int
    ) {
        if (currentUser.uid == chat.sender_user_id) {
            val builder = AlertDialog.Builder(mContext)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this message? By deleting you cannot restore it.")
                .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        databseReference.child("chats").child(chat.sender_user_id)
                            .child(chat.receiver_user_id).child(chat.chat_id)
                            .setValue(null).addOnSuccessListener {
                                databseReference.child("chats")
                                    .child(chat.receiver_user_id)
                                    .child(chat.sender_user_id).child(chat.chat_id)
                                    .setValue(null)
                                Toast.makeText(
                                    mContext,
                                    "Chat deleted",
                                    Toast.LENGTH_LONG
                                ).show()
                                p0?.dismiss()
                                chatList.remove(chat)
                                notifyItemRemoved(holder.adapterPosition)
                                notifyItemRangeChanged(
                                    holder.adapterPosition,
                                    chatList.size
                                )
                                notifyDataSetChanged()
                            }
                    }
                })
                .setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        p0?.dismiss()
                    }

                })
            builder.show()
        }
    }

    private fun setNormalMessage(
        holder: RecyclerView.ViewHolder,
        chat: Chat,
        position: Int,
        chatList: MutableList<Chat>
    ) {
        val holder: ViewHolder = (holder as ViewHolder)
        if (chat.message.isNotEmpty()) {
            holder.tvChatMessage.text = chat.message
            holder.tvChatTime.text = Controller.getDate(chat.timestamp)
        } else {
            holder.cvChat.visibility = View.GONE
            holder.tvChatTime.visibility = View.GONE

        }
    }



    fun md5(s: String): String? {
        try {
            // Create MD5 Hash
            val digest: MessageDigest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest: ByteArray = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(
                Integer.toHexString(
                    0xFF and messageDigest[i]
                        .toInt()
                )
            )
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun downloadAudio(holder: AudioViewHolder, chat: Chat, position: Int) {
        holder.tvChatAudioIndicator.text = "Loading"
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        PRDownloader.initialize(mContext);
        val output = mContext.externalCacheDir!!.absolutePath + "/"
        val downloadId = PRDownloader.download(chat.media_url, output, "${currentUser.uid}.mp3")
            .build()
            .setOnStartOrResumeListener { }
            .setOnPauseListener { }
            .setOnCancelListener(object : OnCancelListener {
                override fun onCancel() {}
            })
            .setOnProgressListener(object : OnProgressListener {
                override fun onProgress(progress: Progress?) {
                    Log.d(TAG, "onProgress: $progress")
                    holder.tvChatAudioIndicator.text = "Loading"
                }
            })
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
//                                Toast.makeText(mContext, "Downloaded", Toast.LENGTH_LONG).show()
                    path = output + "${currentUser.uid}.mp3"
                    audioPlayer(holder, position)
                }

                override fun onError(error: com.downloader.Error?) {
                    if (error != null) {
                        Log.e(TAG, "onError: ${error.serverErrorMessage}")
                    }
                }

            })
    }

    fun audioPlayer(holder: AudioViewHolder, position: Int) {
        try {
            playingAudioViewHolder = holder
            updatePlayingView()
            mp?.setOnCompletionListener(MediaPlayer.OnCompletionListener { releaseMediaPlayer() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateNonPlayingView(holder: AudioViewHolder) {
        holder.ivChatAudioPlayPauseIcon.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        holder.tvChatAudioIndicator.text = "Stopped"
        startedPlaying = false
    }

    private fun updatePlayingView() {
        mp?.setDataSource(path)
        mp?.prepare()
        mp?.start()
        playingAudioViewHolder?.ivChatAudioPlayPauseIcon?.setImageResource(R.drawable.ic_baseline_pause_24)
        playingAudioViewHolder?.tvChatAudioIndicator?.text = "Playing"
        startedPlaying = true
        pause = false
    }

    private fun releaseMediaPlayer() {
        playingAudioViewHolder?.let { updateNonPlayingView(it) }
        mp?.reset()
        currentPlayingPosition = -1
        playingAudioViewHolder?.ivChatAudioPlayPauseIcon?.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        playingAudioViewHolder?.tvChatAudioIndicator?.text = "Voice Message"
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (currentPlayingPosition == holder.adapterPosition) {
            playingAudioViewHolder = null
            startedPlaying = false
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        auth = Firebase.auth
        currentUser = auth.currentUser!!
        val chat: Chat = chatList[position]
        return if (chat.media_type.isEmpty() && chat.sender_user_id == currentUser.uid) {
            SENDER_VIEW
        } else (if (chat.media_type.isEmpty() && !chat.is_uploading && chat.receiver_user_id == currentUser.uid) {
            RECERIVER_VIEW
        } else if (chat.media_url.isNotEmpty() && !chat.is_uploading && (chat.media_type == Constants.Keys.VIDEO || chat.media_type == Constants.Keys.GALLERY || chat.media_type == Constants.Keys.CAMERA) && chat.sender_user_id == currentUser.uid) {
            SENDER_IMAGE_VIDEO_VIEW
        } else if (chat.media_url.isNotEmpty() && !chat.is_uploading && (chat.media_type == Constants.Keys.VIDEO || chat.media_type == Constants.Keys.GALLERY || chat.media_type == Constants.Keys.CAMERA) && chat.receiver_user_id == currentUser.uid) {
            RECERIVER_IMAGE_VIDEO_VIEW
        }
        else if (chat.media_url.isNotEmpty() && !chat.is_uploading && chat.media_type == Constants.Keys.VOICE && chat.sender_user_id == currentUser.uid) {
            SENDER_AUDIO_VIEW
        } else if (chat.media_url.isNotEmpty() && !chat.is_uploading && chat.media_type == Constants.Keys.VOICE && chat.receiver_user_id == currentUser.uid) {
            RECERIVER_AUDIO_VIEW
        }
        else if (chat.message == "chat_connected_indicator") {
            CHAT_CONNECTED_INDICATOR_VIEW
        } else if (chat.media_url.isEmpty() && !chat.is_uploading && (chat.media_type == Constants.Keys.VIDEO || chat.media_type == Constants.Keys.GALLERY || chat.media_type == Constants.Keys.CAMERA) && chat.sender_user_id == currentUser.uid) {
            SENDER_UPLOAD_PROGRESS_VIEW
        }
        else if (chat.media_url.isEmpty() && !chat.is_uploading && (chat.media_type == Constants.Keys.VIDEO || chat.media_type == Constants.Keys.GALLERY || chat.media_type == Constants.Keys.CAMERA) && chat.receiver_user_id == currentUser.uid) {
            RECEIVER_UPLOAD_PROGRESS_VIEW
        }
        else {
            EMPTY_VIEW
        })
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}