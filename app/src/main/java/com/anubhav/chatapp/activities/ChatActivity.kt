package com.anubhav.chatapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.anubhav.chatapp.adapters.ChatAdapter
import com.anubhav.chatapp.databinding.ActivityChatBinding
import com.anubhav.chatapp.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var messages: ArrayList<Message>
    var senderRoom: String? = null
    var receiverRoom: String ? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        messages = ArrayList()

        receiverUid = intent.getStringExtra("uid").toString()
        senderUid = FirebaseAuth.getInstance().uid.toString()

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        adapter = ChatAdapter(this, messages)
        binding.messagesRv.layoutManager = LinearLayoutManager(this)
        binding.messagesRv.adapter = adapter
        database.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (snapshot1 in snapshot.children) {
                        val message = snapshot1.getValue(Message::class.java) as Message
                            message.messageId = snapshot1.key.toString()
                            messages.add(message)

                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        binding.sendBtn.setOnClickListener{
                val messageTxt: String = binding.message.text.toString()
                val date = Date()
                val message = Message(messageTxt, senderUid, date.time)
                binding.message.setText("")
                var randomKey = database.reference.push().key
                database.reference.child("chats")
                    .child(senderRoom!!)
                    .child("messages")
                    .child(randomKey.toString())
                    .setValue(message).addOnSuccessListener {
                        database.reference.child("chats")
                            .child(receiverRoom!!)
                            .child("messages")
                            .child(randomKey.toString())
                            .setValue(message)
            }
            }

    }


}
