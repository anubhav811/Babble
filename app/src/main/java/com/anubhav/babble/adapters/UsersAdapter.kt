package com.anubhav.babble.adapters

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
import java.util.*


class UsersAdapter(var context: Context, private var usersList: ArrayList<User>) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() , Filterable{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false)
        return UsersViewHolder(view)
    }

    private val usersListCopy: ArrayList<User> = usersList
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

    override fun getFilter(): Filter {
        return userFilter
    }
    val userFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()

            if (TextUtils.isEmpty(constraint)) {
                results.count = usersListCopy.size
                results.values = usersListCopy
            } else {
                val filteredList: ArrayList<User> = ArrayList()
                val filterPattern =
                    constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                for (item in usersListCopy) {
                    if (item.name.toLowerCase().contains(filterPattern) || item.phoneNumber.toLowerCase().contains(filterPattern)){
                        filteredList.add(item)
                    }
                }
                results.count = filteredList.size
                results.values = filteredList;

            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
                usersList = results.values as ArrayList<User>
                notifyDataSetChanged()
        }
    }


}