package com.talk.walk.Models

class SelectPeople(var name: String, var drawable: Int, var isSelected: Boolean, var points: Int) {

    constructor(): this("", 0, false, 0)

    var name2: String = "defaultvalue"
        get() = field                     // getter
        set(value) { field = value }      // setter

}