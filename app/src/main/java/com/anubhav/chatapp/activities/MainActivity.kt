package com.anubhav.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.anubhav.chatapp.R
import com.anubhav.chatapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private  lateinit var  navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        navController = binding.hostFragment.getFragment<NavHostFragment>().navController
        setupSmoothBottomMenu()
    }

    private fun setupSmoothBottomMenu(){
        val popupMenu = PopupMenu(this,null)
        popupMenu.inflate(R.menu.bottom_nav)
        val menu = popupMenu.menu
        binding.bottomNav.setupWithNavController(menu, binding.hostFragment.getFragment<NavHostFragment>().navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
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
            R.id.update -> {
                val intent = Intent(this,ProfileUpdateActivity::class.java)
                startActivity(intent)
            }
            R.id.logout -> {
                val alert: AlertDialog = AlertDialog.Builder(this).create()
                alert.setTitle("Logout")
                alert.setMessage("Are you sure you want to logout?")
                alert.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, _ ->
                    CoroutineScope(Dispatchers.IO).launch{
                        withContext(Dispatchers.Main) {
                            Firebase.auth.signOut()
                        }
                    }
                    val intent = Intent(this@MainActivity,PhoneAuthActivity::class.java)
                    dialog.dismiss()
                    startActivity(intent)
                }
                alert.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, _ ->
                    dialog.dismiss()
                }
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
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

