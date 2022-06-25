package com.anubhav.chatapp.utils

class Constants {
    companion object {
        private const val REMOTE_MSG_AUTHORIZATION = "Authorization"
        private const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"
        const val REMOTE_MSG_TYPE = "type"
        const val REMOTE_MSG_CALL = "call"
        const val REMOTE_MSG_CALL_TYPE = "callType"
        const val REMOTE_MSG_CALLER_TOKEN = "callerToken"
        const val REMOTE_MSG_DATA = "data"
        const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"


        fun getRemoteMessageHeader() : HashMap<String,String>{
            val headers = HashMap<String,String>()
            headers[REMOTE_MSG_AUTHORIZATION] = "key=AAAAWzdDAf0:APA91bFr2CU-YWpb3ay0a0DCLOvu7oJBUbB9ld5MNK4qlnPC1Il1HWuh4HgJmxOu4Qvgo0Zag6o8-s8rVg6a8KrVLB-BuzfTp09KgGE_VlmjTaeKNyzud3TT6QJhu7_9QMzzLwD7fYOM"
            headers[REMOTE_MSG_CONTENT_TYPE] = "application/json"

            return headers

        }
    }
}