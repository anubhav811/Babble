package com.anubhav.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.anubhav.chatapp.R
import com.anubhav.chatapp.models.User
import com.anubhav.chatapp.adapters.UsersAdapter
import com.anubhav.chatapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databse:FirebaseDatabase
    private lateinit var usersList : ArrayList<User>
    private lateinit var adapter: UsersAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        databse = FirebaseDatabase.getInstance()
        usersList = ArrayList()
        adapter = UsersAdapter(this,usersList)
        binding.chatRv.adapter = adapter

        databse.reference.child("users").addValueEventListener(object :
            com.google.firebase.database.ValueEventListener {
            override fun onCancelled(p0: com.google.firebase.database.DatabaseError) {
                Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: com.google.firebase.database.DataSnapshot) {
                usersList.clear()
                for(userSnapshot in p0.children){
                    val user = userSnapshot.getValue(User::class.java)
                    usersList.add(user!!)
                }
                for(user in usersList){
                    Log.d("User",user.toString())
                }
                adapter.notifyDataSetChanged()
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show()
            }
            R.id.settings -> {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
            }
            R.id.groups -> {
                Toast.makeText(this, "Groups", Toast.LENGTH_SHORT).show()
            }
            R.id.invite -> {
                Toast.makeText(this, "Invite", Toast.LENGTH_SHORT).show()
            }
            R.id.logout -> {
                val alert : AlertDialog = AlertDialog.Builder(this).create()
                alert.setTitle("Logout")
                alert.setMessage("Are you sure you want to logout?")
                alert.setButton(AlertDialog.BUTTON_POSITIVE,"Yes"){
                    dialog, which ->
                    auth.signOut()
                    finish()
                }
                alert.setButton(AlertDialog.BUTTON_NEGATIVE,"No"){
                    dialog, which ->
                    dialog.dismiss()
                }
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}