package com.anubhav.chatapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.anubhav.chatapp.models.User
import com.anubhav.chatapp.databinding.ActivityProfileSetupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileSetupActivity : AppCompatActivity() {

    private val TAG = "ProfileSetupActivity"
    private lateinit var binding: ActivityProfileSetupBinding

    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var database : FirebaseDatabase
    private var selectedImage : Uri = Uri.EMPTY
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        storage= FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creating Profile")
        progressDialog.setCancelable(false)

        binding.imgSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent,1)
            }

        binding.continueBtn.setOnClickListener {

            if(binding.nameEt.text.isEmpty()){
                binding.nameEt.error = "Name Required"
            }

            progressDialog.show()
            if(selectedImage != Uri.EMPTY){
                val reference : StorageReference = storage.reference.child("Profiles").child(auth.currentUser!!.uid)
                reference.putFile(selectedImage).addOnCompleteListener{
                    task ->
                    if(task.isSuccessful){
                        reference.downloadUrl.addOnSuccessListener {
                            uri ->
                            setupProfile(uri)
                        }
                    }
                }

            }
            else if(selectedImage == Uri.EMPTY){
                val dialog: AlertDialog = AlertDialog.Builder(this).create()
                dialog.setTitle("Profile")
                dialog.setMessage("You haven't selected an image . Are you sure you want to continue ?")
                dialog.setButton(AlertDialog.BUTTON_POSITIVE,"Yes"){
                    dialogInterface, i ->
                    setupProfile(Uri.EMPTY)
                }
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"No"){
                    dialogInterface, i ->
                    dialog.dismiss()

                }
                dialog.create()
                dialog.show()

            }
        }

    }

    fun setupProfile(uri : Uri){
        val imageUrl = uri.toString()
        val uid = auth.currentUser?.uid
        val phoneNumber = auth.currentUser!!.phoneNumber
        val name = binding.nameEt.text.toString()

        val user = User(uid!!,name,phoneNumber!!,imageUrl)

        database.reference.child("users").child(uid).setValue(user).addOnSuccessListener {
            Toast.makeText(this,"Profile Created",Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data!=null){
            if(data.data!=null){
                binding.imageView.setImageURI(data.data)
                selectedImage = data.data!!
            }
        }
    }
}