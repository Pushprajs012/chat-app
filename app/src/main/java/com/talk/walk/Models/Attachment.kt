package com.talk.walk.Models

import android.graphics.drawable.Drawable

class Attachment(var attachment_name: String, var drawable: Drawable?) {

    constructor(): this("", null)

    var default_name = "default_name"
    get() = field
    set(value) {field = value}

}