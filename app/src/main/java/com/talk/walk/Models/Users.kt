package com.talk.walk.Models

class Users(var user_id: String, var username: String, var profile_image: String, var gender: String) {

    constructor(): this("", "", "", "")

    var default_value = "default_value"

    get() = field
    set(value) {field = value}
}