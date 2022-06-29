package com.anubhav.babble.models

import java.io.Serializable

data class Invite (
    var name: String,
    var phoneNumber : String,
    ): Serializable{
    constructor(): this("","")
    }
