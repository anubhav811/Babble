package com.anubhav.babble.adapters


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.babble.R
import com.anubhav.babble.activities.ChatActivity
import com.anubhav.babble.databinding.RecievedChatBinding
import com.anubhav.babble.databinding.SentChatBinding
import com.anubhav.babble.models.Message
import com.bumptech.glide.Glide
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.dsl.reactionConfig
import com.github.pgreze.reactions.dsl.reactions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.model.MyStory


class ConvoAdapter(
    private var activity: ChatActivity,
    private val messages: ArrayList<Message?>?,
    senderRoom: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENT = 1
    private val ITEM_RECEIVED = 2
    private val senderRoom = senderRoom
    var selected_position = 0 // You have to set this globally in the Adapter class


    val receiverId = senderRoom.slice(28..55)
    val receiverRoom = receiverId + FirebaseAuth.getInstance().currentUser!!.uid

    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding: SentChatBinding

        init {
            binding = SentChatBinding.bind(view)
        }
    }

    class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding: RecievedChatBinding


        init {
            binding = RecievedChatBinding.bind(view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(activity.applicationContext)
                .inflate(R.layout.sent_chat, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(activity.applicationContext)
                .inflate(R.layout.recieved_chat, parent, false)
            ReceivedViewHolder(view)
        }
    }


    override fun getItemViewType(position: Int): Int {
        val message = messages?.get(position)
        if (message != null) {
            if (FirebaseAuth.getInstance().uid == message.senderId) {
                return ITEM_SENT
            }
        }
        return ITEM_RECEIVED
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val message = messages?.get(position)
        val reaction = intArrayOf(
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry
        )
        val config = reactionConfig(activity.applicationContext) {
            reactions {
                reaction { R.drawable.ic_fb_like scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_love scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_laugh scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_wow scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_sad scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_angry scale ImageView.ScaleType.FIT_XY }
            }
        }


        // line 100 to 136 for adding rxns
        var viewHolder: RecyclerView.ViewHolder
        val popup = ReactionPopup(activity.applicationContext, config, { pos: Int? ->
            if (pos != null) {
                if (pos > -1) {
                    if (pos == message?.reaction) {
                        message.reaction = -1
                        if (holder.javaClass == SentViewHolder::class.java) {
                            viewHolder = holder as SentViewHolder
                            (viewHolder as SentViewHolder).binding.reaction.visibility = View.GONE
                        } else {
                            viewHolder = holder as ReceivedViewHolder
                            (viewHolder as ReceivedViewHolder).binding.reaction.visibility = View.GONE
                        }
                    } else if (pos != message?.reaction) {
                        message?.reaction = pos!!
                        if (holder.javaClass == SentViewHolder::class.java) {
                            viewHolder = holder as SentViewHolder
                            (viewHolder as SentViewHolder).binding.reaction.setImageResource(
                                reaction[pos]
                            )
                            (viewHolder as SentViewHolder).binding.reaction.visibility =
                                View.VISIBLE
                        } else {
                            viewHolder = holder as ReceivedViewHolder
                            (viewHolder as ReceivedViewHolder).binding.reaction.setImageResource(
                                reaction[pos]
                            )
                            (viewHolder as ReceivedViewHolder).binding.reaction.visibility =
                                View.VISIBLE
                        }
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                    FirebaseDatabase.getInstance().reference.child("chats")
                        .child(activity.senderRoom!!)
                        .child("messages").child(message!!.messageId).child("reaction")
                        .setValue(message.reaction)

                    FirebaseDatabase.getInstance().reference.child("chats")
                        .child(activity.receiverRoom!!)
                        .child("messages").child(message.messageId).child("reaction")
                        .setValue(message.reaction)
                }}}
            }
            true

        })

        // For adding messages to db
        if (holder.javaClass == SentViewHolder::class.java) {

            viewHolder = holder as SentViewHolder

            when (message?.status) {
                "Seen" -> {
                    (viewHolder as SentViewHolder).binding.tick1.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick2.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick1.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.received), android.graphics.PorterDuff.Mode.SRC_IN)
                    (viewHolder as SentViewHolder).binding.tick2.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.received), android.graphics.PorterDuff.Mode.SRC_IN)
                }
                "Delivered" -> {
                    (viewHolder as SentViewHolder).binding.tick1.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick2.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick1.setColorFilter(
                        ContextCompat.getColor(
                            activity.applicationContext,
                            R.color.white
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    );
                    (viewHolder as SentViewHolder).binding.tick2.setColorFilter(
                        ContextCompat.getColor(
                            activity.applicationContext,
                            R.color.white
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }
                "Sent" -> {
                    (viewHolder as SentViewHolder).binding.tick1.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick2.visibility = View.GONE
                    (viewHolder as SentViewHolder).binding.tick1.setColorFilter(
                        ContextCompat.getColor(
                            activity.applicationContext,
                            R.color.white
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    );

                }
            }


            (viewHolder as SentViewHolder).binding.message.text = message?.message
            if(message!!.message == "This message is removed"){
                (viewHolder as SentViewHolder).binding.message.setTypeface(null, android.graphics.Typeface.ITALIC)
            }
            (viewHolder as SentViewHolder).binding.time.text = message?.timestamp


            if (message != null) {
                if (message.message == "Photo" && message.imageUrl != "") {
                    (viewHolder as SentViewHolder).binding.sentImage.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.message.visibility = View.GONE

                    Glide.with(activity.applicationContext).load(message.imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into((viewHolder as SentViewHolder).binding.sentImage)


                    (viewHolder as SentViewHolder).binding.sentImage.setOnClickListener {
                        val myStories: ArrayList<MyStory> = ArrayList()
                        myStories.add(MyStory(message.imageUrl))
                        StoryView.Builder((activity).supportFragmentManager)
                            .setStoriesList(myStories)
                            .setStoryDuration(5000)
                            .build()
                            .show()
                    }
                } else if (message.imageUrl == "") {
                    (viewHolder as SentViewHolder).binding.sentImage.visibility = View.GONE
                    (viewHolder as SentViewHolder).binding.message.visibility = View.VISIBLE

                }
                if (message.reaction >= 0) {
                    (viewHolder as SentViewHolder).binding.reaction.setImageResource(reaction[message.reaction])
                    (viewHolder as SentViewHolder).binding.reaction.visibility = View.VISIBLE
                } else {
                    (viewHolder as SentViewHolder).binding.reaction.visibility = View.GONE
                }

            }

            (viewHolder as SentViewHolder).binding.message.setOnTouchListener(popup)
            (viewHolder as SentViewHolder).binding.time.setOnLongClickListener {
                val optionPopup = PopupMenu(activity.applicationContext, it)
                optionPopup.menuInflater.inflate(R.menu.menu_long_click, optionPopup.menu)
                optionPopup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.copy -> {
                            val clipboard =
                                activity.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = ClipData.newPlainText("text", messages?.get(position)!!.message)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(activity.applicationContext, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        R.id.deleteForMe -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    FirebaseDatabase.getInstance().reference.child("chats")
                                        .child(senderRoom)
                                        .child("messages").child(message!!.messageId).removeValue()
                                }
                            }
                            messages?.remove(message)
                            notifyDataSetChanged()

                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    // get last message node
                                    FirebaseDatabase.getInstance().reference.child("chats")
                                        .child(senderRoom)
                                        .child("messages").orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError) {
                                            }
                                            override fun onDataChange(p0: DataSnapshot) {
                                                if (p0.exists()) {
                                                    for (i in p0.children) {
                                                        val message = i.getValue(Message::class.java)
                                                        if (message != null) {
                                                            FirebaseDatabase.getInstance().reference.child("chats")
                                                                .child(senderRoom).child("lastMsg").setValue(message.message)
                                                            FirebaseDatabase.getInstance().reference.child("chats")
                                                                .child(senderRoom).child("lastMsgTime").setValue(message.timestamp)

                                                            notifyItemRemoved(position)

                                                        }
                                                    }
                                                }
                                            }
                                        })
                                }
                            }
                        }
                        R.id.deleteForAll -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    FirebaseDatabase.getInstance().reference.child("chats")
                                        .child(senderRoom)
                                        .child("messages").child(messages?.get(position)!!.messageId).child("message").setValue("This message is removed")
                                    FirebaseDatabase.getInstance().reference.child("chats")
                                        .child(receiverRoom)
                                        .child("messages").child(messages.get(position)!!.messageId).child("message").setValue("This message is removed")
                                }
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    // get last message node

                                    FirebaseDatabase.getInstance().reference.child("chats")
                                        .child(senderRoom)
                                        .child("messages").orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError) {
                                            }
                                            override fun onDataChange(p0: DataSnapshot) {
                                                if (p0.exists()) {
                                                    for (i in p0.children) {
                                                        val message = i.getValue(Message::class.java)
                                                        if (message != null) {
                                                            FirebaseDatabase.getInstance().reference.child("chats")
                                                                .child(senderRoom).child("lastMsg").setValue(message.message)
                                                            FirebaseDatabase.getInstance().reference.child("chats")
                                                                .child(senderRoom).child("lastMsgTime").setValue(message.timestamp)

                                                            FirebaseDatabase.getInstance().reference.child("chats")
                                                                .child(receiverRoom).child("lastMsg").setValue(message.message)
                                                            FirebaseDatabase.getInstance().reference.child("chats")
                                                                .child(receiverRoom).child("lastMsgTime").setValue(message.timestamp)
                                                            notifyItemRemoved(position)
                                                        }
                                                    }
                                                }
                                            }
                                        })

                                }
                            }
                            notifyDataSetChanged()
                        }
                    }
                    true
                }
                optionPopup.show()
                true
            }
        }
        else {
            FirebaseDatabase.getInstance().reference.child("chats")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(receiverRoom)) {
                            if (snapshot.child(receiverRoom).hasChild("messages")) {
                                if(snapshot.child(receiverRoom).child("messages").hasChild(message!!.messageId)){
                                FirebaseDatabase.getInstance().reference.child("chats")
                                    .child(receiverRoom)
                                    .child("messages").child(message.messageId).child("status")
                                    .setValue("Seen")
                                    .addOnSuccessListener {
                                        notifyDataSetChanged()
                                        FirebaseDatabase.getInstance().reference.child("chats")
                                            .child(senderRoom).child("unseen")
                                            .setValue(0)
                                        notifyDataSetChanged()
                                    }
                            }
                        }}
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }

                })

            viewHolder = holder as ReceivedViewHolder
            (viewHolder as ReceivedViewHolder).binding.message.text = message?.message
            if (message!!.message == "This message is removed") {
                (viewHolder as ReceivedViewHolder).binding.message.setTypeface(
                    null,
                    android.graphics.Typeface.ITALIC
                )
            }
            (viewHolder as ReceivedViewHolder).binding.time.text = message?.timestamp

            if (message.message == "Photo" && message.imageUrl != "") {
                (viewHolder as ReceivedViewHolder).binding.receivedImg.visibility = View.VISIBLE
                (viewHolder as ReceivedViewHolder).binding.message.visibility = View.GONE

                Glide.with(activity.applicationContext).load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into((viewHolder as ReceivedViewHolder).binding.receivedImg)


                (viewHolder as ReceivedViewHolder).binding.receivedImg.setOnClickListener {
                    val myStories: ArrayList<MyStory> = ArrayList()
                    myStories.add(MyStory(message.imageUrl))

                    StoryView.Builder((activity).supportFragmentManager)
                        .setStoriesList(myStories)
                        .setStoryDuration(5000)
                        .build()
                        .show()
                }
            }
            if (message.imageUrl == "") {
                (viewHolder as ReceivedViewHolder).binding.receivedImg.visibility = View.GONE
                (viewHolder as ReceivedViewHolder).binding.message.visibility = View.VISIBLE


            }
            if (message.reaction >= 0) {
                (viewHolder as ReceivedViewHolder).binding.reaction.setImageResource(reaction[message.reaction])
                (viewHolder as ReceivedViewHolder).binding.reaction.visibility = View.VISIBLE
            } else {
                (viewHolder as ReceivedViewHolder).binding.reaction.visibility = View.GONE
            }
            (viewHolder as ReceivedViewHolder).binding.message.setOnTouchListener(popup)


            (viewHolder as ReceivedViewHolder).binding.time.setOnLongClickListener {
                val optionPopup = PopupMenu(activity.applicationContext, it)
                optionPopup.menuInflater.inflate(R.menu.menu_long_click_receiver, optionPopup.menu)
                optionPopup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.copy -> {
                            val clipboard =
                                activity.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = ClipData.newPlainText("text", message?.message)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                activity.applicationContext,
                                "Copied to clipboard",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        R.id.deleteForMe -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    FirebaseDatabase.getInstance().reference.child("chats")
                                        .child(senderRoom)
                                        .child("messages").child(message!!.messageId).removeValue()
                                }
                            }
                            messages?.remove(message)
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    // get last message node
                                    FirebaseDatabase.getInstance().reference.child("chats")
                                        .child(senderRoom)
                                        .child("messages").orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError) {
                                            }
                                            override fun onDataChange(p0: DataSnapshot) {
                                                if (p0.exists()) {
                                                    for (i in p0.children) {
                                                        val message = i.getValue(Message::class.java)
                                                        if (message != null) {
                                                            FirebaseDatabase.getInstance().reference.child("chats")
                                                                .child(senderRoom).child("lastMsg").setValue(message.message)
                                                            FirebaseDatabase.getInstance().reference.child("chats")
                                                                .child(senderRoom).child("lastMsgTime").setValue(message.timestamp)


                                                            notifyDataSetChanged()

                                                        }
                                                    }
                                                }
                                            }
                                        })

                                }
                            }
                            notifyDataSetChanged()
                        }
                    }
                    true
                }
                optionPopup.show()
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return messages!!.size
    }



}