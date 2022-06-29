package com.anubhav.babble.activities

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.anubhav.babble.R
import com.anubhav.babble.databinding.ActivityIncomingCallBinding
import com.anubhav.babble.network.ApiClient
import com.anubhav.babble.network.ApiService
import com.anubhav.babble.utils.Constants
import com.bumptech.glide.Glide
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL


class IncomingCall : AppCompatActivity() {
    private lateinit var binding : ActivityIncomingCallBinding
    private lateinit var callType : String
    private lateinit var meetingRoom : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val phone = intent.getStringExtra("phone")
        callType= intent.getStringExtra("callType")!!
        val image = intent.getStringExtra("image")

        meetingRoom = intent.getStringExtra("meetingRoom")!!

        binding.callerName.text = name
        binding.callerNo.text = phone
        binding.incomingTv.text = "Incoming $callType call"

        Glide.with(this@IncomingCall).load(image!!.toUri()).placeholder(R.drawable.avatar).into(binding.callerImg)

        binding.acceptBtn.setOnClickListener {
            sendCallResponse("accepted", intent.getStringExtra("callerToken")!!)
            Toast.makeText(this@IncomingCall, "Call Connected", Toast.LENGTH_SHORT).show()
            finish()


        }
        binding.declineBtn.setOnClickListener {
            sendCallResponse("declined", intent.getStringExtra("callerToken")!!)
            Toast.makeText(this@IncomingCall, "Call Declined", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendCallResponse(response:String, receiverToken :String){
        try{
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put("type","callResponse")
            data.put("callResponse",response)

            body.put("data",data)
            body.put("registration_ids",tokens)

            sendRemoteMessage(body.toString(),response)

        }catch (e:Exception){
            Toast.makeText(this@IncomingCall,e.message,Toast.LENGTH_LONG).show()
        }
    }
    private fun sendRemoteMessage(remoteMessageBody:String,type:String){
        ApiClient.getClient().create(ApiService::class.java).sendRemoteMessage(
            Constants.getRemoteMessageHeader(),remoteMessageBody
        ).enqueue( object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful){
                    if (type=="accepted") {
                        val builder : JitsiMeetConferenceOptions.Builder = JitsiMeetConferenceOptions.Builder()
                        builder.setServerURL(URL("https://meet.jit.si"))
                        builder.setRoom(meetingRoom)
                        builder.setAudioMuted(false)
                        builder.setFeatureFlag("calendar.enabled",false)
                        builder.setFeatureFlag("chat.enabled",true)
                        builder.setFeatureFlag("filmstrip.enabled",true)
                        builder.setFeatureFlag("live-streaming.enabled",false)
                        builder.setFeatureFlag("meeting-name.enabled",false)
                        builder.setFeatureFlag("meeting-password.enabled",false)
                        builder.setFeatureFlag("overflow-menu.enabled",false)
                        builder.setFeatureFlag("raise-hand.enabled",false)
                        builder.setFeatureFlag("toolbox.alwaysVisible",true)
                        builder.setFeatureFlag("welcomepage.enabled",false)

                        if(callType == "Voice") {
                            builder.setVideoMuted(true)
                            builder.setAudioOnly(true)
                        }
                        else {
                            builder.setVideoMuted(false)
                            builder.setAudioOnly(false)
                        }
                        JitsiMeetActivity.launch(this@IncomingCall,builder.build())
                        finish()


                    }
                    else if (type == "declined"){
                        Toast.makeText(this@IncomingCall,"Call Declined",Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this@IncomingCall,response.message(), Toast.LENGTH_SHORT).show()
                    finish()
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@IncomingCall,t.message, Toast.LENGTH_SHORT).show()
                finish()
            }

        })
    }
    private val callResponseReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            val type = intent!!.getStringExtra("callResponse")
            if (type != null) {
                Log.d("IncomingCall",type)
            }
            if(type!=null){
                if(type == "cancelled"){
                    Toast.makeText(context,"Call Cancelled",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(callResponseReceiver,
            IntentFilter("callResponse")
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(callResponseReceiver)

    }


}