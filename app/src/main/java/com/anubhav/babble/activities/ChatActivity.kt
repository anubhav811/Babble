@file:Suppress("DEPRECATION")

package com.anubhav.babble.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.anubhav.babble.R
import com.anubhav.babble.adapters.ConvoAdapter
import com.anubhav.babble.databinding.ActivityChatBinding
import com.anubhav.babble.listeners.UsersListener
import com.anubhav.babble.models.Message
import com.anubhav.babble.models.User
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() , UsersListener{

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ConvoAdapter
    private lateinit var messages: ArrayList<Message?>
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var dialog: ProgressDialog
    lateinit var senderUid: String
    lateinit var receiverUid: String
    lateinit var receiverName: String
    lateinit var senderRoom: String
    lateinit var receiverRoom: String
    lateinit var sender: User
    private lateinit var name: String

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database
        storage = Firebase.storage
        dialog = ProgressDialog(this)
        dialog.setMessage("Uploading image...")
        dialog.setCancelable(false)
        messages = ArrayList()



        database.reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("name")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        name = snapshot.getValue(String::class.java).toString()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        val profileImg = intent.getStringExtra("profileImage")
        receiverName = intent.getStringExtra("name").toString()

        binding.name.text = receiverName
        Glide.with(this).load(profileImg)
            .placeholder(R.drawable.avatar)
            .into(binding.profileImg)

        binding.backArrow.setOnClickListener { finish() }
        receiverUid = intent.getStringExtra("uid").toString()


        senderUid = Firebase.auth.currentUser!!.uid

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                database.reference.child("users").child(senderUid).addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            sender = snapshot.getValue(User::class.java)!!
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}

                })
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                database.reference.child("activity").child(receiverUid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val status = snapshot.getValue(String::class.java)
                                if (status!!.isNotEmpty()) {
                                    if (status == "Offline") {
                                        binding.status.visibility = View.GONE
                                    } else {
                                        binding.status.text = status
                                        binding.status.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}

                    })
            }
        }
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        Log.d("senderRoom", senderRoom)
        Log.d("receiverRoom", receiverRoom)
        Log.d("senderUid", senderUid)
        Log.d("receiverUid", receiverUid)


        getMessages()

        adapter = ConvoAdapter(this, messages,senderRoom,receiverRoom)
        binding.messagesRv.layoutManager = LinearLayoutManager(this)
        binding.messagesRv.adapter = adapter

        binding.messagesRv.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                val lastVisiblePosition =
                    (binding.messagesRv.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                val lastItem = binding.messagesRv.adapter!!.itemCount - 1
                Log.d("lastVisiblePosition", lastVisiblePosition.toString())
                Log.d("lastItem", lastItem.toString())
                binding.messagesRv.postDelayed({
                    if (lastVisiblePosition + 5 == lastItem || lastVisiblePosition + 6 == lastItem) binding.messagesRv.scrollToPosition(
                        lastItem
                    )
                    else binding.messagesRv.scrollToPosition(lastVisiblePosition)
                }, 100)
            }
        }

        binding.sendBtn.setOnClickListener {
            if(binding.message.text.toString().isNotEmpty()){
                val messageTxt: String = binding.message.text.toString()
                binding.message.setText("")

                val dateFormat: DateFormat = SimpleDateFormat("hh.mm aa")
                val time: String = dateFormat.format(Date()).toString()

                val message = Message(messageTxt, senderUid, time)
                val randomKey = database.reference.push().key
                val lastMsgObj: HashMap<String, Any> = HashMap()
                lastMsgObj["lastMsg"] = message.message
                lastMsgObj["lastMsgTime"] = time

                database.reference.child("chats").child(senderRoom).updateChildren(lastMsgObj)
                database.reference.child("chats").child(receiverRoom).updateChildren(lastMsgObj)
                database.reference.child("chats")
                .child(senderRoom)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                        database.reference.child("chats").child(receiverRoom).child("messages")
                            .child(randomKey).setValue(message)
                        database.reference.child("chats").child(senderRoom).child("messages")
                            .child(randomKey).child("status").setValue("Sent")
                        adapter.notifyItemInserted(messages.size)
                        binding.messagesRv.scrollToPosition(binding.messagesRv.adapter!!.itemCount - 1)
                        database.reference.child("chats").child(senderRoom).child("messages")
                            .child(randomKey).child("status").setValue("Delivered")
//                        database.reference.child("chats").child(receiverRoom).child("unseen")
//                            .setValue(ServerValue.increment(1))
                        adapter.notifyDataSetChanged()

                    }
                }}



        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                val calendar = Calendar.getInstance()
                val reference = storage.reference.child("chats")
                    .child(calendar.timeInMillis.toString() + "")
                dialog.show()
                reference.putFile(uri).addOnCompleteListener { task ->
                    dialog.dismiss()
                    if (task.isSuccessful) {
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            val filePath = uri.toString()
                            val messageTxt: String =
                                binding.message.text.toString()
                            val dateFormat: DateFormat = SimpleDateFormat("hh.mm aa")
                            val time: String = dateFormat.format(Date()).toString()

                            val message = Message(messageTxt, senderUid, time)

                            message.message = ("Photo")
                            message.imageUrl = (filePath)
                            binding.message.setText("")
                            val randomKey = database.reference.push().key
                            val lastMsgObj: HashMap<String, Any> = HashMap()
                            lastMsgObj["lastMsg"] = message.message
                            lastMsgObj["lastMsgTime"] = time
                            database.reference.child("chats").child(senderRoom)
                                .updateChildren(lastMsgObj)
                            database.reference.child("chats").child(receiverRoom)
                                .updateChildren(lastMsgObj)
                            database.reference.child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(randomKey!!)
                                .setValue(message).addOnSuccessListener {
                                    database.reference.child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener {
                                            adapter.notifyItemInserted(messages.size)
                                            binding.messagesRv.scrollToPosition(binding.messagesRv.adapter!!.itemCount)
                                        }
                                }
                        }
                    }}}
                }
            }
            else if (it.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this,ImagePicker.Companion.getError(it.data),Toast.LENGTH_SHORT).show()
            }
        }
        binding.attachment.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .createIntentFromDialog { launcher.launch(it) }
        }
        val handler = Handler()
        binding.message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                database.reference.child("activity").child(senderUid).setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)}}
            }
            var userStoppedTyping =
                Runnable {
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                    database.reference.child("activity").child(senderUid).setValue("Online")
                }}}
        })

        binding.voiceCallBtn.setOnClickListener {
            if (isNetworkAvailable()) {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Make a Video Call")
                dialog.setMessage("Are you sure you want to make a video call to $receiverName ? ")
                dialog.setPositiveButton("Yes") { _, _ ->
                    initiateVideoCall(receiverUid)
                }
                dialog.setNegativeButton("No") { d, _ ->
                    d.dismiss()
                }
                dialog.show()
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }

        binding.videoCallBtn.setOnClickListener {
            if (isNetworkAvailable()) {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Make a Voice Call")
                dialog.setMessage("Are you sure you want to make a voice call to $receiverName ? ")
                dialog.setPositiveButton("Yes") { _, _ ->
                    initiateVoiceCall(receiverUid)
                }
                dialog.setNegativeButton("No") { d, _ ->
                    d.dismiss()
                }
                dialog.show()
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                database.reference.child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val count = messages.size
                            messages.clear()
                            for (snapshot1 in snapshot.children) {
                                val message = snapshot1.getValue(Message::class.java)
                                if (message != null) {
                                    message.messageId = snapshot1.key.toString()
                                    messages.add(message)
                                }
                            }
                            adapter.notifyDataSetChanged()
                            if(messages.size>count) {
                                binding.messagesRv.scrollToPosition(binding.messagesRv.adapter!!.itemCount-1)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        }

    }


    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("activity").child(currentId!!).setValue("Offline")
    }
    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("activity").child(currentId!!).setValue("Online")

    }

    override fun initiateVideoCall(userId: String) {
        CoroutineScope(Dispatchers.IO).launch{
            withContext(Dispatchers.Main){
                database.reference.child("users").child(userId).addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)
                            if(user!!.token.isEmpty()){
                                Toast.makeText(this@ChatActivity,user.name + " is not available right now",Toast.LENGTH_SHORT).show()
                            }
                            else{
                                val intent  = Intent(this@ChatActivity,OutgoingCall::class.java)
                                intent.putExtra("sender",sender)
                                intent.putExtra("receiver",user)
                                intent.putExtra("type","Video")
                                startActivity(intent)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }
        }
    }
    override fun initiateVoiceCall(userId: String) {
        CoroutineScope(Dispatchers.IO).launch{
            withContext(Dispatchers.Main){
                database.reference.child("users").child(userId).addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)
                            if(user!!.token.isEmpty()){
                                Toast.makeText(this@ChatActivity,user.name + " is not available right now",Toast.LENGTH_SHORT).show()
                            }
                            else{
                                val intent  = Intent(this@ChatActivity,OutgoingCall::class.java)
                                intent.putExtra("sender",sender)
                                intent.putExtra("receiver",user)
                                intent.putExtra("type","Voice")
                                startActivity(intent)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }
        }
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}