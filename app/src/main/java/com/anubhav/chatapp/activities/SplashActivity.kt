package com.anubhav.chatapp.activities

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.anubhav.chatapp.databinding.ActivitySplashBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }
            override fun onAnimationEnd(p0: Animator?) {
                if (Firebase.auth.currentUser != null) {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    val intent = Intent(applicationContext, PhoneAuthActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onAnimationCancel(p0: Animator?) {
            }
            override fun onAnimationRepeat(p0: Animator?) {
            }

        })
    }
}