package com.anubhav.chatapp.activities

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
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestContactPermission(arrayOf(android.Manifest.permission.READ_CONTACTS), 1)
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
                            database.reference.child("users").child(Firebase.auth.currentUser!!.uid).child("token").setValue("")
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

    fun requestContactPermission(arrayOf: Array<String>, i: Int) {
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
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("activity").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("activity").child(currentId!!).setValue("Offline")
    }
}

