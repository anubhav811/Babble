package com.anubhav.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.anubhav.chatapp.databinding.ActivityPhoneAuthBinding

class PhoneAuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.phoneEt.requestFocus()
        binding.continueBtn.setOnClickListener {
            val intent = Intent(this, OTPActivity::class.java)
            val code = binding.ccp.selectedCountryCode
            val phoneNo = "+" + code + binding.phoneEt.text.toString()
            intent.putExtra("phoneNo", phoneNo)
            startActivity(intent)
        }
    }

}