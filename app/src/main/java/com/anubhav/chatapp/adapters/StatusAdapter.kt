package com.anubhav.chatapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.chatapp.activities.MainActivity
import com.anubhav.chatapp.databinding.ItemStatusBinding
import com.anubhav.chatapp.models.Status
import com.anubhav.chatapp.models.UserStatus
import com.bumptech.glide.Glide
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory


class StatusAdapter(var context: Context, private val statusList: ArrayList<UserStatus>) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {


    class StatusViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var binding : ItemStatusBinding

        init {
            binding = ItemStatusBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(context).inflate(com.anubhav.chatapp.R.layout.item_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val status = statusList[position]
        val lastStatus = status.statuses[status.statuses.size - 1]

        Glide.with(context).load(lastStatus.imageUrl).into(holder.binding.statusImg)
        holder.binding.circularStatusView.setPortionsCount(status.statuses.size)

        holder.binding.circularStatusView.setOnClickListener {
            val myStories: ArrayList<MyStory> = ArrayList()
            for (story:Status in status.statuses){
                myStories.add(MyStory(story.imageUrl))
            }
            StoryView.Builder((context as MainActivity).supportFragmentManager)
                .setStoriesList(myStories) // Required
                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                .setTitleText(status.name) // Default is Hidden
                .setSubtitleText("") // Default is Hidden
                .setTitleLogoUrl(status.profileImg) // Default is Hidden
                .setStoryClickListeners(object : StoryClickListeners {
                    override fun onDescriptionClickListener(position: Int) {
                        //your action
                    }

                    override fun onTitleIconClickListener(position: Int) {
                        //your action
                    }
                }) // Optional Listeners
                .build() // Must be called before calling show method
                .show()


        }
    }


    override fun getItemCount(): Int {
        return statusList.size
    }
}