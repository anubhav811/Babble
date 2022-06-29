package com.anubhav.babble.models

import java.io.Serializable

class Message(

    var messageId: String,
    var message: String,
    var senderId: String,
    var timestamp: String,
    var reaction: Int = -1,
    var imageUrl :String,
    var status : String

 ) : Serializable{
    constructor() : this("","","","",-1,"","Sent")
    constructor(message: String, senderId: String, timestamp: String) : this() {
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
    }
}