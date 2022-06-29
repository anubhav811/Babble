
package com.anubhav.babble.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.anubhav.babble.R
import com.anubhav.babble.databinding.RowConversationBinding
import com.anubhav.babble.models.Invite
import com.bumptech.glide.Glide
import java.util.*


class InviteAdapter(var context: Context, private var inviteList: ArrayList<Invite>) : RecyclerView.Adapter<InviteAdapter.InviteViewHolder>(),
    Filterable {

    private val inviteListCopy: ArrayList<Invite> = inviteList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false)
        return InviteViewHolder(view)
    }


    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val user = inviteList[position]
        holder.binding.username.text = user.name
        holder.binding.msgTime.text =""
        holder.binding.lastMsg.text = user.phoneNumber
        Glide.with(context).load(R.drawable.avatar)
            .placeholder(R.drawable.avatar)
            .into(holder.binding.profileImg)

        holder.itemView.setOnClickListener {
            val sms_uri = Uri.parse("smsto:" + user.phoneNumber)
            val sms_intent = Intent(Intent.ACTION_SENDTO, sms_uri)
            sms_intent.putExtra("sms_body", "Hey, I am using Babble app. Download it now from https://github.com/anubhav811/Babble");
            startActivity(this.context, sms_intent, null)

//            val smsIntent = Intent(Intent.ACTION_VIEW)
//            smsIntent.type = "vnd.android-dir/mms-sms"
//            smsIntent.putExtra("address", user.phoneNumber)
//            smsIntent.putExtra("sms_body", "Hey, I am using Babble app. Download it now from https://github.com/anubhav811/Babble");
//            startActivity(this.context, smsIntent, null)

        }
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context,R.anim.fall_down))

    }


    override fun getItemCount(): Int {
        return inviteList.size
    }
    class InviteViewHolder(itemView: View) : ViewHolder(itemView) {
        var binding: RowConversationBinding
        init {
            binding = RowConversationBinding.bind(itemView)
        }
    }
    override fun getFilter(): Filter {
        return inviteFilter
    }
        val inviteFilter: Filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()

                if (TextUtils.isEmpty(constraint)) {
                    results.count = inviteListCopy.size
                    results.values = inviteListCopy
                } else {
                    val filteredList: ArrayList<Invite> = ArrayList()
                    val filterPattern =
                        constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                    for (item in inviteListCopy) {
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

                    inviteList = results.values as ArrayList<Invite>
                    notifyDataSetChanged()


            }
        }

}