package com.anubhav.chatapp.models

import java.io.Serializable

data class User (
     val uid: String,
     val name: String,
     val phoneNumber : String,
    val profileImage: String
    ): Serializable{
    constructor(): this("","","","")
    constructor(uid: String, name: String, phoneNumber: String): this(uid, name, phoneNumber, "")
    }