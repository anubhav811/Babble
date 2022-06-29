package com.anubhav.babble.listeners

interface UsersListener {
    fun initiateVideoCall(userId:String)
    fun initiateVoiceCall(userId:String)
}