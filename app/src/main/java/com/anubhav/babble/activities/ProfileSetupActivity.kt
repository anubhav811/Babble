package com.anubhav.babble.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.anubhav.babble.R
import com.anubhav.babble.databinding.ActivityProfileSetupBinding
import com.anubhav.babble.models.User
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding

    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var database : FirebaseDatabase
    private var selectedImage : Uri = Uri.EMPTY
    private lateinit var progressDialog : ProgressDialog

    private var name = ""
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creating Profile")
        progressDialog.setCancelable(false)

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                database.reference.child("users").child(auth.currentUser!!.uid)
                    .addValueEventListener(
                        (object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    user = snapshot.getValue(User::class.java)!!
                                    name = user!!.name
                                    binding.nameEt.setText(name)
                                    Glide.with(applicationContext).load(user!!.profileImage)
                                        .placeholder(R.drawable.avatar).into(binding.imageView)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })
                    )
            }
        }
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data!!
                    binding.imageView.setImageURI(uri)
                    selectedImage = uri
                }
                }


        binding.imgSelect.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .createIntentFromDialog {
                    launcher.launch(it)
                }
        }

        binding.continueBtn.setOnClickListener {

            if (binding.nameEt.text.isEmpty()) {
                binding.nameEt.error = "Name Required"
            } else {
                progressDialog.show()
                if (selectedImage != Uri.EMPTY) {
                    val reference: StorageReference =
                        storage.reference.child("Profiles").child(auth.currentUser!!.uid)
                    reference.putFile(selectedImage).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { uri ->
                                setupProfile(uri)
                            }
                        }
                    }

                } else {
                    setupProfile(user?.profileImage!!.toUri())
                }
            }
        }
    }

    private fun setupProfile(uri : Uri){
        val imageUrl = uri.toString()
        val uid = auth.currentUser?.uid
        val phoneNumber = auth.currentUser!!.phoneNumber
        val name = binding.nameEt.text.toString()
        val user = User(uid!!,name,phoneNumber!!,imageUrl)

                database.reference.child("users").child(uid).setValue(user).addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@ProfileSetupActivity, "Profile Created", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ProfileSetupActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
    }

}