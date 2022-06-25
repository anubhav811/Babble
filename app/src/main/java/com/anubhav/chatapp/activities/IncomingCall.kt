package com.anubhav.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import com.anubhav.chatapp.R
import com.anubhav.chatapp.databinding.ActivityIncomingCallBinding
import com.bumptech.glide.Glide

class IncomingCall : AppCompatActivity() {
    private lateinit var binding : ActivityIncomingCallBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val phone = intent.getStringExtra("phone")
        val callType= intent.getStringExtra("callType")
        val image = intent.getStringExtra("image")

        binding.callerName.text = name
        binding.callerNo.text = phone
        binding.incomingTv.text = "Incoming $callType"
        Glide.with(this@IncomingCall).load(image!!.toUri()).placeholder(R.drawable.avatar).into(binding.callerImg)

        binding.declineBtn.setOnClickListener {
            onBackPressed()
        }



    }
}