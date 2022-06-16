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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UsersAdapter(var context: Context, private val usersList: ArrayList<User>) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false)
        return UsersViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = usersList[position]
        holder.binding.username.text = user.name
        holder.binding.msgTime.text =""
        Glide.with(context).load(user.profileImage.toUri())
            .placeholder(R.drawable.avatar)
            .into(holder.binding.profileImg)


        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("profileImage", user.profileImage)
            intent.putExtra("uid", user.uid)
            intent.putExtra("token",user.token)
            intent.flags  = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

        }
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context,R.anim.fall_down))

    }


    override fun getItemCount(): Int {
        return usersList.size
        }
    class UsersViewHolder(itemView: View) : ViewHolder(itemView) {
        var binding: RowConversationBinding

        init {
            binding = RowConversationBinding.bind(itemView)
        }
    }
}