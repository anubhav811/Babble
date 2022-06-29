package com.anubhav.babble.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.anubhav.babble.R
import com.anubhav.babble.databinding.ActivityOutgoingCallBinding
import com.anubhav.babble.models.User
import com.anubhav.babble.network.ApiClient
import com.anubhav.babble.network.ApiService
import com.anubhav.babble.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class OutgoingCall : AppCompatActivity() {
    private lateinit var binding : ActivityOutgoingCallBinding
    private lateinit var receiver: User
    private lateinit var sender: User
    private lateinit var callerToken :String
    private lateinit var meetingRoom :String
    private lateinit var callType :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutgoingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiver = intent.getSerializableExtra("receiver") as User
        sender = intent.getSerializableExtra("sender") as User
        val name = receiver.name
        val phoneNumber = receiver.phoneNumber
        val uid = receiver.uid
        val profileImage = receiver.profileImage

        callType = intent.getStringExtra("type")!!

        meetingRoom = sender.uid +  UUID.randomUUID().toString().substring(0,5)

        binding.callType.text = "$callType Calling"
        binding.receiverName.text  = name
        binding.receiverNo.text = phoneNumber
        Glide.with(this).load(profileImage.toUri())
            .placeholder(R.drawable.avatar)
            .into(binding.receiverImg)

        binding.cancelBtn.setOnClickListener {
            cancelCall(receiver.token)
            finish()
         }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(it.isSuccessful){
                callerToken = it.result.toString()
                if(callType!=null){
                    initiateCall(callType,receiver.token)
                }
            }
        }


    }
    private fun cancelCall(receiverToken :String){
        try{
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put("type","callResponse")
            data.put("callResponse","cancelled")

            body.put("data",data)
            body.put("registration_ids",tokens)

            sendRemoteMessage(body.toString(),"invitationResponse")

        }catch (e:Exception){
            Toast.makeText(this@OutgoingCall,e.message,Toast.LENGTH_LONG).show()
        }
    }
    private fun sendRemoteMessage(remoteMessageBody:String,type:String){

            ApiClient.getClient().create(ApiService::class.java).sendRemoteMessage(
                Constants.getRemoteMessageHeader(),remoteMessageBody
            ).enqueue( object : Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful){
                        if(type == "call"){
                            Toast.makeText(this@OutgoingCall,"Call Successful",Toast.LENGTH_SHORT).show()
                        }else if(type == "cancelled"){
                            Toast.makeText(this@OutgoingCall,"Call Cancelled",Toast.LENGTH_SHORT).show()
                            finish()
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
        private fun initiateCall(callType : String, receiverToken : String){
            try{
                val tokens  = JSONArray()
                tokens.put(receiverToken)
                val body = JSONObject()
                val data = JSONObject()

                data.put("type","call")
                data.put("callType",callType)
                data.put("Name",sender.name)
                data.put("Phone",sender.phoneNumber)
                data.put("Image",sender.profileImage)
                data.put("callerToken",callerToken)

                data.put("meetingRoom",meetingRoom)
                body.put("data",data)
                body.put("registration_ids",tokens)

                sendRemoteMessage(body.toString(),"call")

            }catch (e : Exception){
                Toast.makeText(this@OutgoingCall,e.message,Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    private val callResponseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val response = intent!!.getStringExtra("callResponse")
                if(response == "accepted"){
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
                    JitsiMeetActivity.launch(this@OutgoingCall,builder.build())
                    finish()
                    Toast.makeText(context,"Call Connected",Toast.LENGTH_SHORT).show()
                    // get date in day, time , month, year format
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm aa")
                    val time: String = dateFormat.format(Date()).toString()

                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main){
                            val randomKey = Firebase.database.reference.push().key

                            Firebase.database.reference.child("calls").child(sender.uid).child(randomKey!!).child("callTime").setValue(time)
                            Firebase.database.reference.child("calls").child(sender.uid).child(randomKey).child("callType").setValue(callType)
                            Firebase.database.reference.child("calls").child(sender.uid).child(randomKey).child("id").setValue(receiver.uid)
                            Firebase.database.reference.child("calls").child(sender.uid).child(randomKey).child("name").setValue(receiver.name)

                            Firebase.database.reference.child("calls").child(receiver.uid).child(randomKey).child("callTime").setValue(time)
                            Firebase.database.reference.child("calls").child(receiver.uid).child(randomKey).child("callType").setValue(callType)
                            Firebase.database.reference.child("calls").child(receiver.uid).child(randomKey).child("id").setValue(sender.uid)
                            Firebase.database.reference.child("calls").child(receiver.uid).child(randomKey).child("name").setValue(sender.name)

                        }

                   }
                }
                else if(response == "declined"){
                    Toast.makeText(context,"Call Declined",Toast.LENGTH_SHORT).show()
                }
                finish()
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