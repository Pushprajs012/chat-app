package com.talk.walk.Models

class BlockedFriends(var user_id: String, var met_user_id: String, var timestamp: Long) {

    constructor(): this("", "", 0)

    var default_value:String = "Hey"
    get() = field
    set(value) {field = value}
}

