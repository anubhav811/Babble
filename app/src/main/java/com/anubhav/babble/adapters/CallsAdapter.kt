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
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.babble.R
import com.anubhav.babble.activities.ChatActivity
import com.anubhav.babble.activities.OutgoingCall
import com.anubhav.babble.databinding.RowCallBinding
import com.anubhav.babble.databinding.RowConversationBinding
import com.anubhav.babble.models.Call
import com.anubhav.babble.models.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CallsAdapter(var context: Context, private var callsList: ArrayList<Call>) :
    RecyclerView.Adapter<CallsAdapter.CallsViewHolder>(),
    Filterable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_call, parent, false)
        return CallsViewHolder(view)
    }

    private val callsListCopy: ArrayList<Call> = callsList



    override fun onBindViewHolder(holder: CallsViewHolder, position: Int) {
        val call = callsList[position]

        val sender = getUserData()
        var receiver = User()
        Firebase.database.reference.child("users").child(call.id)
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        receiver = snapshot.getValue(User::class.java)!!
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                }
            )
        holder.binding.username.text = call.name
        holder.binding.lastCallTime.text = call.callTime
        Glide.with(context).load(receiver.profileImage.toUri())
            .placeholder(R.drawable.avatar)
            .into(holder.binding.profileImg)
        holder.binding.type.setImageResource(
            when (call.callType) {
                "Voice" -> R.drawable.ic_call
                "Video" -> R.drawable.ic_video
                else -> R.drawable.ic_call
            }
        )
        holder.binding.type.setOnClickListener {
            if (isNetworkAvailable()) {
                val dialog = AlertDialog.Builder(context)
                dialog.setTitle("Make a ${call.callType} Call")
                dialog.setMessage("Are you sure you want to make a ${call.callType} call to ${receiver.name} ? ")
                dialog.setPositiveButton("Yes") { dialog, which ->
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            Firebase.database!!.reference.child("users").child(receiver.uid)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val user = snapshot.getValue(User::class.java)
                                            if (user!!.token.isEmpty()) {
                                                Toast.makeText(
                                                    context,
                                                    user.name + " is not available right now",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                val intent =
                                                    Intent(context, OutgoingCall::class.java)
                                                intent.putExtra("sender", sender)
                                                intent.putExtra("receiver", user)
                                                intent.putExtra("type", call.callType)
                                                context.startActivity(intent)
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }

                                })
                        }
                    }
                }
                dialog.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                dialog.show()
            } else {
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }

        }
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fall_down))

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }


    override fun getItemCount(): Int {
        return callsList.size
    }

    class CallsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: RowCallBinding

        init {
            binding = RowCallBinding.bind(itemView)
        }
    }

    override fun getFilter(): Filter {
        return callFilter
    }

    val callFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()

            if (TextUtils.isEmpty(constraint)) {
                results.count = callsListCopy.size
                results.values = callsListCopy
            } else {
                val filteredList: ArrayList<Call> = ArrayList()
                val filterPattern =
                    constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                for (item in callsListCopy) {
                    if (item.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
                results.count = filteredList.size
                results.values = filteredList;

            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            callsList = results.values as ArrayList<Call>
            notifyDataSetChanged()
        }
    }

    private fun getUserData(): User {
        var sender = User()
        Firebase.database.reference.child("users").child(Firebase.auth.currentUser!!.uid)
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        sender = snapshot.getValue(User::class.java)!!
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                }
            )
        return sender
    }

    private fun getReceiverData(call: Call): User {
        var receiver = User()

        // get user data
        Firebase.database.reference.child("users").child(call.id)
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        receiver = snapshot.getValue(User::class.java)!!
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                }
            )
        return receiver
    }
}