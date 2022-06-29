package com.anubhav.babble.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.anubhav.babble.R
import com.anubhav.babble.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private  lateinit var  navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = Firebase.database
        navController = binding.hostFragment.getFragment<NavHostFragment>().navController
        setupSmoothBottomMenu()
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestContactPermission(arrayOf(android.Manifest.permission.READ_CONTACTS))
        }
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

    override  fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.invite -> {
                if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, InviteActivity::class.java)
                    startActivity(intent) }
                else {
                    Toast.makeText(this, "Contact permission not granted", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.update -> {
                val intent = Intent(this, ProfileUpdateActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.logout -> {
                val alert: AlertDialog = AlertDialog.Builder(this).create()
                alert.setTitle("Logout")
                alert.setMessage("Are you sure you want to logout?")
                alert.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, _ ->

                val currUser = Firebase.auth.currentUser!!.uid
                database.reference.child("users").child(currUser)
                    .child("token").setValue("").addOnSuccessListener {
                        Firebase.auth.signOut()
                    }

                val intent = Intent(this, PhoneAuthActivity::class.java)
                dialog.dismiss()
                startActivity(intent)
                    finish()
                }
                alert.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, _ ->
                    dialog.dismiss()
                }
                alert.show()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun requestContactPermission(arrayOf: Array<String>) {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_CONTACTS))
        {
            android.app.AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("Allow permission for getting your contacts info")
                .setPositiveButton("ok") { dialog, which ->
                    ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_CONTACTS),1)
                }
                .setNegativeButton("cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }else{
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_CONTACTS),1)
        }}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        database.reference.child("activity").child(Firebase.auth.currentUser!!.uid).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        database.reference.child("activity").child(Firebase.auth.currentUser!!.uid).setValue("Offline")
    }
}

