package com.anubhav.chatapp.adapters


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.chatapp.R
import com.anubhav.chatapp.activities.ChatActivity
import com.anubhav.chatapp.databinding.DeleteDialogBinding
import com.anubhav.chatapp.databinding.RecievedChatBinding
import com.anubhav.chatapp.databinding.SentChatBinding
import com.anubhav.chatapp.models.Message
import com.bumptech.glide.Glide
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.dsl.reactionConfig
import com.github.pgreze.reactions.dsl.reactions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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
                if (pos > 0) {
                    if (pos == message?.reaction) {
                        message?.reaction = -1
                        if (holder.javaClass == SentViewHolder::class.java) {
                            viewHolder = holder as SentViewHolder
                            (viewHolder as SentViewHolder).binding.reaction.visibility = View.GONE
                        } else {
                            viewHolder = holder as ReceivedViewHolder
                            (viewHolder as ReceivedViewHolder).binding.reaction.visibility =
                                View.GONE
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


                    FirebaseDatabase.getInstance().reference.child("chats")
                        .child(activity.senderRoom!!)
                        .child("messages").child(message!!.messageId).child("reaction")
                        .setValue(message.reaction)

                    FirebaseDatabase.getInstance().reference.child("chats")
                        .child(activity.receiverRoom!!)
                        .child("messages").child(message.messageId).child("reaction")
                        .setValue(message.reaction)
                }
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
                    (viewHolder as SentViewHolder).binding.tick1.setColorFilter(
                        ContextCompat.getColor(
                            activity.applicationContext,
                            R.color.received
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    );
                    (viewHolder as SentViewHolder).binding.tick2.setColorFilter(
                        ContextCompat.getColor(
                            activity.applicationContext,
                            R.color.received
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    );

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
            (viewHolder as SentViewHolder).binding.linearLayout.setOnTouchListener(popup)
        } else {

            FirebaseDatabase.getInstance().reference.child("chats")
                .child(senderRoom)
                .child("messages").child(message!!.messageId).child("status").setValue("Seen")
                .addOnSuccessListener {
                    notifyDataSetChanged()
                    FirebaseDatabase.getInstance().reference.child("chats")
                        .child(receiverRoom)
                        .child("messages").child(message!!.messageId).child("status")
                        .setValue("Seen").addOnSuccessListener {
                            notifyDataSetChanged()
                        }
                }


            viewHolder = holder as ReceivedViewHolder
            (viewHolder as ReceivedViewHolder).binding.message.text = message?.message
            (viewHolder as ReceivedViewHolder).binding.time.text = message?.timestamp

            if (message != null) {
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
            }
            (viewHolder as ReceivedViewHolder).binding.linearLayout2.setOnTouchListener(popup)
        }
//      viewHolder.itemView.setOnClickListener {
//            val view: View = LayoutInflater.from(activity.applicationContext).inflate(R.layout.delete_dialog,null)
//            val binding: DeleteDialogBinding = DeleteDialogBinding.bind(view)
//            val dialog: android.app.AlertDialog? = android.app.AlertDialog.Builder(activity.applicationContext)
//                .setTitle("Delete Message")
//                .setView(binding.root)
//                .create()
//
//            binding.everyone.setOnClickListener(View.OnClickListener {
//                message!!.message = "This message is removed."
//                message.reaction = -1
////                FirebaseDatabase.getInstance().reference
////                    .child("public")
////                    .child(message!!.messageId).setValue(message)
//                dialog?.dismiss()
//            })
//            binding.delete.setOnClickListener(View.OnClickListener {
////                FirebaseDatabase.getInstance().reference
////                    .child("public")
////                    .child(message!!.messageId).setValue(null)
//                dialog?.dismiss()
//            })
//            binding.cancel.setOnClickListener(View.OnClickListener { dialog?.dismiss() })
//            dialog?.show()
//false        }
//


    }


    override fun getItemCount(): Int {
        return messages!!.size
    }


}