package com.anubhav.babble.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.anubhav.babble.adapters.InviteAdapter
import com.anubhav.babble.databinding.ActivityInviteBinding
import com.anubhav.babble.db.RoomDb
import com.anubhav.babble.models.Invite
import com.anubhav.babble.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class InviteActivity : AppCompatActivity() {
    private lateinit var binding : ActivityInviteBinding
    private lateinit var inviteList: ArrayList<Invite>
    private lateinit var currUsersList: ArrayList<Invite>
    private lateinit var adapter: InviteAdapter
    var contacts = ArrayList<ContactData>()
    var userList = ArrayList<Invite>()
    var db : RoomDb? = null
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInviteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog.setMessage("Fetching your contacts...")
        dialog.setCancelable(false)

        inviteList = ArrayList()
        currUsersList = ArrayList()
        db = RoomDb.invoke(this)

        adapter = InviteAdapter(this, inviteList)
        binding.inviteRv.adapter = adapter

        binding.backArrow.setOnClickListener {
            finish()
        }


        binding.searchBtn.setOnSearchClickListener(View.OnClickListener {
            binding.name.visibility = View.GONE
            binding.backArrow.visibility = View.GONE
        })
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


    private fun loadContacts() {
        contacts = retrieveAllContacts() as ArrayList<ContactData>
        // add contacts to to inviteList
        for (contact in contacts) {
            // for loop for size of phonenumber list in contact
            for (i in 0 until contact.phoneNumber.size) {
                inviteList.add(Invite(contact.name, contact.phoneNumber[i]))
            }
        }
        getUniqueUsers()
    }

    private fun getUsers() {
        Firebase.database.reference.child("users").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }
            override fun onDataChange(p0: DataSnapshot) {
                currUsersList.clear()
                for (userSnapshot in p0.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user?.uid != Firebase.auth.currentUser!!.uid) {
                        val invite = Invite(user!!.name, user.phoneNumber)
                        currUsersList.add(invite)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
    private fun getUniqueUsers() {
        inviteList.removeAll(currUsersList)
        adapter.notifyDataSetChanged()
        dialog.dismiss()
    }


    override fun onStart() {
        super.onStart()
        dialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            getUsers()
            loadContacts()
        }
    }



}