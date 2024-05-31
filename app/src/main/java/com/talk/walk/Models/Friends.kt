package com.talk.walk.Models

class Friends(var user_id: String, var met_user_id: String, var hasUserRead: Boolean, var timestamp: Long) {

    constructor(): this("", "", false, 0)

    var default_value = "default_value"

    get() = field
    set(value) {field = value}
}