package com.anubhav.babble.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.anubhav.babble.R
import com.anubhav.babble.activities.ChatActivity
import com.anubhav.babble.databinding.RowConversationBinding
import com.anubhav.babble.models.Invite
import com.anubhav.babble.models.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList


class ChatsAdapter(var context: Context, private var chatsList: ArrayList<User> ) : RecyclerView.Adapter<ChatsAdapter.UsersViewHolder>() , Filterable{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false)
        return UsersViewHolder(view)
    }
    private val chatsListCopy: java.util.ArrayList<User> = chatsList

    override fun onBindViewHolder(holder: UsersViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val user = chatsList[position]
        val senderId = FirebaseAuth.getInstance().uid
        val senderRoom  = senderId.plus(user.uid)
                FirebaseDatabase.getInstance().reference.child("chats").addValueEventListener( object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if(dataSnapshot.hasChild(senderRoom)) {
                                        val snapshot = dataSnapshot.child(senderRoom)
                                        if(snapshot.hasChild("lastMsg")) {
                                            val lastMessage = snapshot.child("lastMsg").value.toString()
                                            holder.binding.lastMsg.text = lastMessage
                                        }
                                        if(snapshot.hasChild("lastMsgTime")) {
                                            val timestamp = snapshot.child("lastMsgTime").value.toString()
                                            holder.binding.msgTime.text = timestamp
                                        }
                                        if (snapshot.hasChild("unseen") && snapshot.child("unseen").value.toString() != "0") {
                                            holder.binding.lastMsg.setTypeface(null, android.graphics.Typeface.BOLD)
                                            holder.binding.unseen.visibility = View.VISIBLE
                                            holder.binding.unseen.text = snapshot.child("unseen").value.toString()
                                        } else {
                                            holder.binding.lastMsg.setTypeface(null,android.graphics.Typeface.NORMAL)
                                            holder.binding.unseen.visibility = View.GONE
                                        }
                                    }
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        }

                    )


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
        holder.itemView.setOnLongClickListener {
            // create alert dialog for deleting chat

            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            builder.setTitle("Delete Chat")
            builder.setMessage("Are you sure you want to delete chat with ${user.name} ?")
            builder.setPositiveButton("Yes") { dialog, which ->
                FirebaseDatabase.getInstance().reference.child("chats").child(senderRoom).removeValue().addOnSuccessListener {
                    chatsList.removeAt(position)
                    notifyItemRemoved(position)
                }
                }
            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()
            true
        }
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

    override fun getFilter(): Filter {
        return chatFilter
    }
    private val chatFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()

            if (TextUtils.isEmpty(constraint)) {
                results.count = chatsListCopy.size
                results.values = chatsListCopy
            } else {
                val filteredList: java.util.ArrayList<User> = java.util.ArrayList()
                val filterPattern =
                    constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                for (item in chatsListCopy) {
                    if (item.name.toLowerCase()
                            .contains(filterPattern) || item.phoneNumber.toLowerCase()
                            .contains(filterPattern)
                    ) {
                        filteredList.add(item)
                    }
                }
                results.count = filteredList.size
                results.values = filteredList;

            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {

            chatsList = results.values as java.util.ArrayList<User>
            notifyDataSetChanged()
        }
    }
}