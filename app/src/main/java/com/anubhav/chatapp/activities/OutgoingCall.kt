package com.anubhav.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.anubhav.chatapp.R
import com.anubhav.chatapp.databinding.ActivityOutgoingCallBinding
import com.anubhav.chatapp.models.User
import com.anubhav.chatapp.network.ApiClient
import com.anubhav.chatapp.network.ApiService
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import  com.anubhav.chatapp.utils.Constants
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OutgoingCall : AppCompatActivity() {
    private lateinit var binding : ActivityOutgoingCallBinding
    private lateinit var preferenceManager : PreferenceManager
    private lateinit var user: User
    private lateinit var sender: User
    private lateinit var senderToken :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutgoingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        senderToken = FirebaseMessaging.getInstance().token.toString()
        val type = intent.getStringExtra("type")
        user = intent.getSerializableExtra("receiver") as User
        sender = intent.getSerializableExtra("sender") as User
        val name = user.name
        val phoneNumber = user.phoneNumber
        val uid = user.uid
        val receiverToken = user.token
        val profileImage = user.profileImage


        binding.callType.text = "$type Call"
        binding.receiverName.text  = name
        binding.receiverNo.text = phoneNumber
        Glide.with(this).load(profileImage.toUri())
            .placeholder(R.drawable.avatar)
            .into(binding.receiverImg)

        binding.declineBtn.setOnClickListener {
            onBackPressed()
        }
        if(type!=null){
            initiateCall(type,receiverToken)
            }
    }

    private fun sendRemoteMessage(remoteMessageBody:String,type:String){

            ApiClient.getClient().create(ApiService::class.java).sendRemoteMessage(
                Constants.getRemoteMessageHeader(),remoteMessageBody
            ).enqueue( object : Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful){
                        if(type == Constants.REMOTE_MSG_CALL){
                            Toast.makeText(this@OutgoingCall,"Call Successful",Toast.LENGTH_SHORT).show()

                        }
                    }else{
                        Toast.makeText(this@OutgoingCall,response.message(),Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@OutgoingCall,t.message,Toast.LENGTH_SHORT).show()

                }

            })
        }
        private fun initiateCall(meetingType : String, receiverToken : String){
            try{
                val tokens  = JSONArray()
                tokens.put(receiverToken)
                val body = JSONObject()
                val data = JSONObject()

                data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_CALL)
                data.put(Constants.REMOTE_MSG_CALL_TYPE,meetingType)
                data.put("Name",sender.name)
                data.put("Phone",sender.phoneNumber)
                data.put("Image",sender.profileImage)
                data.put(Constants.REMOTE_MSG_CALLER_TOKEN,senderToken)

                body.put(Constants.REMOTE_MSG_DATA,data)
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens)

                sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_CALL)
            }catch (e : Exception){
                Toast.makeText(this@OutgoingCall,e.message,Toast.LENGTH_SHORT).show()
                finish()
            }
        }


}