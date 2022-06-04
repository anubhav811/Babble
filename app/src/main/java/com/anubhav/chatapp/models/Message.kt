package com.anubhav.chatapp.models

import java.io.Serializable

class Message(
    var messageId: String,
    var message: String,
    var senderId: String,
    var timestamp: Long,
    var reaction: Int
) : Serializable{
    constructor() : this("","","",0,0)
    constructor(message: String, senderId: String, timestamp: Long) : this() {
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
    }
}