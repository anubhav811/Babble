package com.anubhav.babble.activities

import android.R.menu
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.anubhav.babble.R
import com.anubhav.babble.adapters.InviteActivity
import com.anubhav.babble.adapters.UsersAdapter
import com.anubhav.babble.databinding.ActivityUsersBinding
import com.anubhav.babble.db.RoomDb
import com.anubhav.babble.db.UserEntity
import com.anubhav.babble.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class UsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsersBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var usersList: ArrayList<User>
    private lateinit var adapter: UsersAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: ProgressDialog
    var contacts = ArrayList<ContactData>()
    var phoneNos = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog.setMessage("Syncing your contacts...")
        dialog.setCancelable(false)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()


        usersList = ArrayList()

        adapter = UsersAdapter(this, usersList)

        binding.inviteBtn.setOnClickListener {
            val intent = Intent(this, InviteActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.backArrow.setOnClickListener { finish() }

        adapter = UsersAdapter(applicationContext, usersList)
        binding.userRv.adapter = adapter



    }

    override fun onStart() {
        super.onStart()
        dialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            loadContacts()
            loadPhoneNos()
            if (phoneNos.size != 0) getUsers()
        }
    }

    private fun loadPhoneNos() {
        if (contacts.size == 0) {
            Toast.makeText(applicationContext, "None of your contacts are on Chat App :( ", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            binding.inviteBtn.visibility = View.VISIBLE
        }
        else {
            binding.inviteBtn.visibility = View.GONE
            phoneNos = ArrayList()
            for (contact in contacts) {
                if (contact.phoneNumber.isNotEmpty()) {
                    phoneNos.add(contact.phoneNumber[0].replace("\\s".toRegex(), "").takeLast(10))
                }
            }
        }
    }

    private fun loadContacts() {
        contacts = retrieveAllContacts() as ArrayList<ContactData>
    }

    private fun getUsers(){
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
                                if(phoneNos.contains(contactNoDb)){
                                    usersList.add(user)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                }
                if(usersList.size == 0) {
                    binding.inviteBtn.visibility = View.VISIBLE
                    Toast.makeText(
                        applicationContext,
                        "None of your contacts are on Chat App :( ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else{
                    binding.userRv.visibility = View.VISIBLE
                    binding.inviteBtn.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
                dialog.dismiss()

            }
        })

        binding.searchBtn.setOnSearchClickListener(View.OnClickListener {
            binding.name.visibility = View.GONE
            binding.backArrow.visibility = View.GONE

        })
        // Detect SearchView close
        // Detect SearchView close
        binding.searchBtn.setOnCloseListener {
            binding.name.visibility = View.VISIBLE
            binding.backArrow.visibility = View.VISIBLE
            false
        }

        binding.searchBtn.queryHint = "Type here to search"
        binding.searchBtn.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
               adapter.filter.filter(query)
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

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