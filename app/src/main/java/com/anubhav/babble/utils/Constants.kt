package com.anubhav.babble.utils

class Constants {
    companion object {
        private const val REMOTE_MSG_AUTHORIZATION = "Authorization"
        private const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"

        fun getRemoteMessageHeader() : HashMap<String,String>{
            val headers = HashMap<String,String>()
            headers[REMOTE_MSG_AUTHORIZATION] = "key=AAAAHsSUUn0:APA91bFLZWwUt8Cco0OkMq5HnH73f5rrbbR4K28mnUx30jWD475s4jcXQk1aK_gUm5lihU3Kij4pxMwYVbo3P8lKNZIffPdDlLOoHUbBUn5hHH3VYGUs2OQuQzykWHZY4B_ymVm9MHpM"
            headers[REMOTE_MSG_CONTENT_TYPE] = "application/json"

            return headers

        }
    }
}