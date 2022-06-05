package com.anubhav.chatapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.chatapp.R
import com.anubhav.chatapp.activities.ChatActivity
import com.anubhav.chatapp.databinding.RecievedChatBinding
import com.anubhav.chatapp.databinding.SentChatBinding
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.dsl.reactionConfig
import com.github.pgreze.reactions.dsl.reactions
import com.google.firebase.auth.FirebaseAuth
import com.anubhav.chatapp.models.Message
import com.google.firebase.database.FirebaseDatabase


class ChatAdapter(var activity: ChatActivity, private val messages: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private final val ITEM_SENT = 1
    private final val ITEM_RECEIVED = 2

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

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        if (FirebaseAuth.getInstance().uid == message.senderId) {
            return ITEM_SENT
        }
        return ITEM_RECEIVED
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


    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
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


        val popup = ReactionPopup(activity.applicationContext, config, { pos: Int? ->

            if (pos == message.reaction) {
                message.reaction = -1
                if (holder.javaClass == SentViewHolder::class.java) {
                    val sentViewHolder = holder as SentViewHolder
                    sentViewHolder.binding.reaction.visibility = View.GONE
                } else {
                    val receivedViewHolder = holder as ReceivedViewHolder
                    receivedViewHolder.binding.reaction.visibility = View.GONE
                }
            } else {
                message.reaction = pos!!
                if (holder.javaClass == SentViewHolder::class.java) {
                    val sentViewHolder = holder as SentViewHolder
                    sentViewHolder.binding.reaction.setImageResource(reaction[pos])
                    sentViewHolder.binding.reaction.visibility = View.VISIBLE
                } else {
                    val receivedViewHolder = holder as ReceivedViewHolder
                    receivedViewHolder.binding.reaction.setImageResource(reaction[pos])
                    receivedViewHolder.binding.reaction.visibility = View.VISIBLE
                }
            }


            FirebaseDatabase.getInstance().reference.child("chats").child(activity.senderRoom!!)
                .child("messages").child(message.messageId).child("reaction").setValue(message.reaction)
            FirebaseDatabase.getInstance().reference.child("chats").child(activity.receiverRoom!!)
                .child("messages").child(message.messageId).child("reaction").setValue(message.reaction)

            true // true is closing popup, false is requesting a new selection
        })

        if (holder.javaClass == SentViewHolder::class.java) {
            val sentViewHolder = holder as SentViewHolder
            sentViewHolder.binding.message.text = message.message

            if (message.reaction >= 0) {
                sentViewHolder.binding.reaction.setImageResource(reaction[message.reaction])
                sentViewHolder.binding.reaction.visibility = View.VISIBLE
            } else {
                sentViewHolder.binding.reaction.visibility = View.GONE
            }
            sentViewHolder.binding.message.setOnTouchListener(popup)

        } else {
            val receivedViewHolder = holder as ReceivedViewHolder
            receivedViewHolder.binding.message.text = message.message
            if (message.reaction >= 0) {
                receivedViewHolder.binding.reaction.setImageResource(reaction[message.reaction])
                receivedViewHolder.binding.reaction.visibility = View.VISIBLE
            } else {
                receivedViewHolder.binding.reaction.visibility = View.GONE
            }
            receivedViewHolder.binding.message.setOnTouchListener(popup)

        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}