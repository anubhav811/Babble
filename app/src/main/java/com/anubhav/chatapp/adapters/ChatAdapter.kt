package com.anubhav.chatapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.chatapp.R
import com.anubhav.chatapp.databinding.RecievedChatBinding
import com.anubhav.chatapp.databinding.SentChatBinding
import com.anubhav.chatapp.models.Message
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter   (var context: Context, private val messages: ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private final val ITEM_SENT = 1
    private final val ITEM_RECEIVED = 2

    class SentViewHolder(view: View):RecyclerView.ViewHolder(view){
        var binding : SentChatBinding
        init {
            binding = SentChatBinding.bind(view)
        }
    }
    class ReceivedViewHolder(view: View):RecyclerView.ViewHolder(view){
        var binding : RecievedChatBinding
        init {
            binding = RecievedChatBinding.bind(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        if(FirebaseAuth.getInstance().uid == message.senderId){
            return ITEM_SENT
        }
        return ITEM_RECEIVED
        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == ITEM_SENT){
            val view = LayoutInflater.from(context).inflate(R.layout.sent_chat,parent,false)
            SentViewHolder(view)
        } else{
            val view = LayoutInflater.from(context).inflate(R.layout.recieved_chat,parent,false)
            ReceivedViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if(holder.javaClass == SentViewHolder::class.java){
            val sentViewHolder = holder as SentViewHolder
            sentViewHolder.binding.message.text = message.message
        } else{
            val receivedViewHolder = holder as ReceivedViewHolder
            receivedViewHolder.binding.message.text = message.message
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}