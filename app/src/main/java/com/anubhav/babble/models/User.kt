package com.anubhav.babble.models

import java.io.Serializable

data class User (
     var uid: String,
     var name: String,
     var phoneNumber : String,
    var profileImage: String,
     var token : String,

    ): Serializable{
    constructor(): this("","","","","")
    constructor(uid: String, name: String, phoneNumber: String,profileImage: String) : this(){
        this.uid=uid
        this.name = name
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
    }
}