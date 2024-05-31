package com.talk.walk.Models

data class Chat(
    var user_key: String,
    var delete: Boolean,
    var sender_user_id: String,
    var receiver_user_id: String,
    var message: String,
    var chat_id: String,
    var media_type: String,
    var video_thumbnail: String,
    var media_url: String,
    var is_read: Boolean,
    var type: String,
    var is_uploading: Boolean,
    var upload_progress: Int,
    var timestamp: Long
) {

    constructor() : this("", false, "", "", "", "", "", "", "", false, "", false, 0,  System.currentTimeMillis())

    var isAudioPlaying: Boolean = false
        get() = field
        set(value) {
            field = value
        }
}