package com.anubhav.chatapp.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.anubhav.chatapp.adapters.ChatsAdapter
import com.anubhav.chatapp.adapters.UsersAdapter
import com.anubhav.chatapp.databinding.ActivityUsersBinding
import com.anubhav.chatapp.models.User
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class UsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsersBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var usersList: ArrayList<User>
    private lateinit var userShimmerFl: ShimmerFrameLayout
    private lateinit var adapter: UsersAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog.setMessage("Syncing your contacts...")
        dialog.setCancelable(false)
        dialog.show()

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        userShimmerFl = binding.userShimmer

        usersList = ArrayList()

        userShimmerFl.startShimmer()

        binding.backArrow.setOnClickListener { finish() }

        adapter = UsersAdapter(applicationContext, usersList)
        binding.userRv.adapter = adapter

                val contacts = retrieveAllContacts() as ArrayList<ContactData>
                Log.d("Contacts SiZe", contacts.size.toString())
                if (contacts.size == 0) {
                    Toast.makeText(
                        applicationContext,
                        "None of your contacts are on Chat App :( ",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                    userShimmerFl.stopShimmer()
                    userShimmerFl.visibility = View.GONE
                } else {
                    val phoneNos = ArrayList<String>()

                            for (contact in contacts) {
                                if (contact.phoneNumber.isNotEmpty()) {
                                    phoneNos.add(
                                        contact.phoneNumber[0]
                                            .replace("\\s".toRegex(), "")
                                            .takeLast(10)
                                    )

                        }
                    }
                    database.reference.child("users").addValueEventListener(object :
                        ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            usersList.clear()
                                for (userSnapshot in p0.children) {
                                    val user = userSnapshot.getValue(User::class.java)
                                    if (user?.uid != auth.currentUser!!.uid) {
                                        val contactNoDb = user!!.phoneNumber.replace("\\s".toRegex(), "").takeLast(10)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            withContext(Dispatchers.Main)
                                            {
                                                println(contactNoDb)
                                                if(phoneNos.contains(contactNoDb)){
                                                    usersList.add(user)
                                                    adapter.notifyItemInserted(usersList.size - 1)
                                                }
                                    } }}
                            }
                            dialog.dismiss()
                            userShimmerFl.stopShimmer()
                            userShimmerFl.visibility = View.GONE
                            binding.userRv.visibility = View.VISIBLE
                            adapter.notifyDataSetChanged()

                        }
                    })
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("activity").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("activity").child(currentId!!).setValue("Offline")
    }

}