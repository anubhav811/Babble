package com.anubhav.babble.models

import java.io.Serializable

data class Call (
    var callTime:String,
    var callType :String,
    var id : String,
    var name:String,
): Serializable {
constructor(): this("","","","")
}