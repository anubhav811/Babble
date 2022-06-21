package com.anubhav.chatapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anubhav.chatapp.R
import com.anubhav.chatapp.adapters.ConvoAdapter
import com.anubhav.chatapp.databinding.ActivityChatBinding
import com.anubhav.chatapp.databinding.RowConversationBinding
import com.anubhav.chatapp.models.Message
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    var adapter: ConvoAdapter? = null
    var messages: ArrayList<Message?>? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var receiverUid: String? = null
    var name: String? = null
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()

        database!!.reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("name")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        name = snapshot.getValue(String::class.java)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        val profile = intent.getStringExtra("profileImage")
        val token = intent.getStringExtra("token")
        val receiverName = intent.getStringExtra("name")

        binding.name.text = receiverName
        Glide.with(this).load(profile)
            .placeholder(R.drawable.avatar)
            .into(binding.profileImg)
        binding.backArrow.setOnClickListener(View.OnClickListener { finish() })
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid


        CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) {
            database!!.reference.child("activity").child(receiverUid!!)
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

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                val list = messages!!
                database!!.reference.child("chats")
                    .child(senderRoom!!)
                    .child("messages")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val count = messages!!.size
                            messages!!.clear()
                            for (snapshot1 in snapshot.children) {
                                val message = snapshot1.getValue(Message::class.java)
                                if (message != null) {
                                    message.messageId = snapshot1.key.toString()
                                    messages!!.add(message)
                                }
                            }
                            adapter!!.notifyDataSetChanged()
                            if(messages!!.size>count) {
                                binding.messagesRv.scrollToPosition(binding.messagesRv.adapter!!.itemCount-1)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }}

        adapter = ConvoAdapter(this, messages, senderRoom!!)
        binding.messagesRv.layoutManager = LinearLayoutManager(this)
        binding.messagesRv.adapter = adapter
        binding.messagesRv.addOnLayoutChangeListener(OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                val lastVisiblePosition = (binding.messagesRv.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                val lastItem = binding.messagesRv.adapter!!.itemCount - 1
                Log.d("lastVisiblePosition", lastVisiblePosition.toString())
                Log.d("lastItem", lastItem.toString())
                binding.messagesRv.postDelayed({
                    if(lastVisiblePosition+5==lastItem || lastVisiblePosition+6==lastItem) binding.messagesRv.scrollToPosition(lastItem)
                    else binding.messagesRv.scrollToPosition(lastVisiblePosition)

                }, 100)
            }
        })

        binding.sendBtn.setOnClickListener {
            if(binding.message.text.toString().isNotEmpty()){
            adapter!!.notifyDataSetChanged()
            val messageTxt: String = binding.message.getText().toString()
            val dateFormat: DateFormat = SimpleDateFormat("hh.mm aa")
            val time: String = dateFormat.format(Date()).toString()
            var status : String = "Sent"
            val message = Message(messageTxt, senderUid!!, time)
            binding.message.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj: HashMap<String, Any> = HashMap()
            lastMsgObj["lastMsg"] = message.message
            lastMsgObj["lastMsgTime"] = time
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
            database!!.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)
            database!!.reference.child("chats")
                .child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    adapter!!.notifyItemInserted(messages!!.size)
                    database!!.reference.child("chats").child(senderRoom!!).child("messages").child(randomKey).child("status").setValue("Sent")
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener {
                            database!!.reference.child("chats").child(receiverRoom!!).child("unseen").setValue(ServerValue.increment(1))
                            database!!.reference.child("chats").child(senderRoom!!).child("messages").child(randomKey).child("status").setValue("Delivered")
//                            sendNotification(
//                                name,
//                                message.message,
//                                token
//                            )
                            adapter!!.notifyItemInserted(messages!!.size)
                            binding.messagesRv.scrollToPosition(binding.messagesRv.adapter!!.itemCount-1)
                        }

                }}}
                }
        }
//        CoroutineScope(Dispatchers.IO).launch {
//            withContext(Dispatchers.Main) {
//                database!!.reference.child("chats")
//                    .child(senderRoom!!)
//                    .child("messages").addValueEventListener(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            if (snapshot.exists()) {
//                                for (snapshot1 in snapshot.children) {
//                                    val message = snapshot1.getValue(Message::class.java)
//                                    if (message != null) {
//                                        if(message.status=="Delivered"){
//                                        }
//                                    }
//                                }
//                                adapter!!.notifyDataSetChanged()
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                        }
//
//                    })
//            }}
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                val calendar = Calendar.getInstance()
                val reference = storage!!.reference.child("chats")
                    .child(calendar.timeInMillis.toString() + "")
                dialog!!.show()
                reference.putFile(uri).addOnCompleteListener { task ->
                    dialog!!.dismiss()
                    if (task.isSuccessful) {
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            val filePath = uri.toString()
                            val messageTxt: String =
                                binding.message.text.toString()
                            val dateFormat: DateFormat = SimpleDateFormat("hh.mm aa")
                            val time: String = dateFormat.format(Date()).toString()

                            val message = Message(
                                messageTxt,
                                senderUid!!, time
                            )
                            message.message = ("Photo")
                            message.imageUrl = (filePath)
                            binding.message.setText("")
                            val randomKey = database!!.reference.push().key
                            val lastMsgObj: HashMap<String, Any> = HashMap()
                            lastMsgObj["lastMsg"] = message.message
                            lastMsgObj["lastMsgTime"] = time
                            database!!.reference.child("chats").child(senderRoom!!)
                                .updateChildren(lastMsgObj)
                            database!!.reference.child("chats").child(receiverRoom!!)
                                .updateChildren(lastMsgObj)
                            database!!.reference.child("chats")
                                .child(senderRoom!!)
                                .child("messages")
                                .child(randomKey!!)
                                .setValue(message).addOnSuccessListener {
                                    database!!.reference.child("chats")
                                        .child(receiverRoom!!)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener {
                                            adapter!!.notifyItemInserted(messages!!.size)
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
                database!!.reference.child("activity").child(senderUid!!).setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)}}
            }

            var userStoppedTyping =
                Runnable {
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                    database!!.reference.child("activity").child(senderUid!!).setValue("Online")
                }}}
        })
        supportActionBar!!.setDisplayShowTitleEnabled(false)



    }




    fun sendNotification(name: String?, message: String?, token: String?) {
        try {
            val queue = Volley.newRequestQueue(this)
            val url = "https://fcm.googleapis.com/fcm/send"
            val data = JSONObject()
            data.put("title", name)
            data.put("body", message)
            val notificationData = JSONObject()
            notificationData.put("notification", data)
            notificationData.put("to", token)
            val request: JsonObjectRequest =
                object : JsonObjectRequest(url, notificationData, Response.Listener {
                    // Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
                },
                    Response.ErrorListener { error ->
                        Toast.makeText(
                            this@ChatActivity,
                            error.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val map: HashMap<String, String> = HashMap()
                        val key = "Key=AAAAWzdDAf0:APA91bFr2CU-YWpb3ay0a0DCLOvu7oJBUbB9ld5MNK4qlnPC1Il1HWuh4HgJmxOu4Qvgo0Zag6o8-s8rVg6a8KrVLB-BuzfTp09KgGE_VlmjTaeKNyzud3TT6QJhu7_9QMzzLwD7fYOM"
                        map["Content-Type"] = "application/json"
                        map["Authorization"] = key
                        return map
                    }
                }
            queue.add(request)
        } catch (ex: Exception) {
        }
    }




//    @SuppressLint("NotifyDataSetChanged")
//    override fun onResume() {
//        super.onResume()
//        adapter!!.notifyDataSetChanged()
//        binding.messagesRv.ScrollToPosition(binding.messagesRv.adapter!!.itemCount)
//        val currentId = FirebaseAuth.getInstance().uid
//        database!!.reference.child("activity").child(currentId!!).setValue("Online")
//        database!!.reference.child("chats")
//            .child(receiverRoom!!)
//            .child("messages")
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (snapshot1 in snapshot.children) {
//                        val message = snapshot1.getValue(Message::class.java)
//                        if (message != null) {
//                            message.messageId = snapshot1.key.toString()
//                            database!!.reference.child("chats")
//                                .child(receiverRoom!!)
//                                .child("messages").child(message.messageId).child("status").setValue("Seen").addOnSuccessListener {
//                                    adapter!!.notifyDataSetChanged()
//                                }
//
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {}
//            })
//        super.onStart()
//    }


    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("activity").child(currentId!!).setValue("Offline")
    }
    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("activity").child(currentId!!).setValue("Online")

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }


}