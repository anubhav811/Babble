package com.anubhav.chatapp.listeners

import com.anubhav.chatapp.models.User

interface UsersListener {
    fun initiateVideoCall(userId:String)
    fun initiateVoiceCall(userId:String)
}