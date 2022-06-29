package com.anubhav.babble.models

import java.io.Serializable

class Status(
    var imageUrl: String,
    var timeStamp : Long
):Serializable{
    constructor() : this("", -1)
    constructor(imageUrl: String, timeStamp: Long, userId: String):this(imageUrl,timeStamp)
}