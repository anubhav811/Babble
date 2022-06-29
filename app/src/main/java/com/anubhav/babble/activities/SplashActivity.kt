package com.anubhav.babble.activities

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anubhav.babble.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser


        binding.animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }
            override fun onAnimationEnd(p0: Animator?) {
                if (user != null) {
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