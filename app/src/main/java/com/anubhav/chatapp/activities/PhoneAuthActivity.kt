package com.anubhav.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.anubhav.chatapp.databinding.ActivityPhoneAuthBinding
import com.google.firebase.auth.FirebaseAuth

class PhoneAuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneAuthBinding
    private lateinit var auth :FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        supportActionBar?.hide()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            binding.phoneEt.requestFocus()
            binding.continueBtn.setOnClickListener {
                val intent = Intent(this, OTPActivity::class.java)
                intent.putExtra("phoneNo", binding.phoneEt.text.toString())
                startActivity(intent)
            }
        }
    }
}