package com.anubhav.chatapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.anubhav.chatapp.databinding.ActivityOtpactivityBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOtpactivityBinding
    private lateinit var auth:FirebaseAuth
    lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth=FirebaseAuth.getInstance()
        val phoneNo = intent.getStringExtra("phoneNo")
        binding.phoneLabelTv.text = "Verify $phoneNo"

        val dialog = ProgressDialog(this)
        dialog.setMessage("Sending OTP")
        dialog.setCancelable(false)
        dialog.show()


        val options: PhoneAuthOptions =
            PhoneAuthOptions.Builder(auth)
                .setPhoneNumber(phoneNo.toString())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object :PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    }
                    override fun onVerificationFailed(p0: FirebaseException) {
                    }

                    override fun onCodeSent(verifyId: String, forceSendingToken: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(verifyId, forceSendingToken)
                        verificationId = verifyId
                        dialog.dismiss()
                        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                        binding.otpView.requestFocus()
                    }
                }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)


        binding.otpView.setOtpCompletionListener { it ->

            val credential :PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, it.toString())
            
            auth.signInWithCredential(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileSetupActivity::class.java))
                    finishAffinity()
                }
                else{
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }

        }

        binding.continueBtn.setOnClickListener {
            val credential :PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, it.toString())

            auth.signInWithCredential(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileSetupActivity::class.java))
                    finishAffinity()
                }
                else{
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}