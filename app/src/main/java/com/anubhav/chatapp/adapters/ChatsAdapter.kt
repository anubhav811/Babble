package com.anubhav.chatapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.anubhav.chatapp.R
import com.anubhav.chatapp.activities.ChatActivity
import com.anubhav.chatapp.databinding.RowConversationBinding
import com.anubhav.chatapp.models.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChatsAdapter(var context: Context, private val chatsList: ArrayList<User>) : RecyclerView.Adapter<ChatsAdapter.UsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false)
        return UsersViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = chatsList[position]
        val senderId = FirebaseAuth.getInstance().uid
        val senderRoom  = senderId.plus(user.uid)
        val receiverRoom = user.uid.plus(senderId)
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                FirebaseDatabase.getInstance().reference.child("chats").child(senderRoom)
                    .addValueEventListener(
                        object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val lastMsg = dataSnapshot.child("lastMsg").value.toString()
                                    val time = dataSnapshot.child("lastMsgTime").value.toString()
                                    val unseen = dataSnapshot.child("unseen").value.toString()
                                    holder.binding.lastMsg.text = lastMsg
                                    holder.binding.msgTime.text = time

                                    if (unseen == "0" || unseen.isNullOrBlank()) {
                                        holder.binding.unseen.visibility = View.GONE
                                        holder.binding.lastMsg.setTypeface(null, android.graphics.Typeface.NORMAL)
                                    } else {
                                        holder.binding.lastMsg.setTypeface(null, android.graphics.Typeface.BOLD)
                                        holder.binding.unseen.visibility = View.VISIBLE
                                        holder.binding.unseen.text = unseen
                                    }
                                } else {
                                    holder.binding.lastMsg.text = "Tap to Chat"
                                    holder.binding.msgTime.text = ""
                                }


                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        }


                    )

            }}
                holder.binding.username.text = user.name
                Glide.with(context).load(user.profileImage.toUri())
                    .placeholder(R.drawable.avatar)
                    .into(holder.binding.profileImg)
                holder.itemView.setOnClickListener {
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("name", user.name)
                    intent.putExtra("profileImage", user.profileImage)
                    intent.putExtra("uid", user.uid)
                    intent.putExtra("token", user.token)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }

                holder.itemView.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.fall_down
                    )
                )
    }
    override fun getItemCount(): Int {
        return chatsList.size
        }
    class UsersViewHolder(itemView: View) : ViewHolder(itemView) {
        var binding: RowConversationBinding

        init {
            binding = RowConversationBinding.bind(itemView)
        }
    }
}