@file:Suppress("DEPRECATION")

package com.anubhav.chatapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.chatapp.R
import com.anubhav.chatapp.activities.UsersActivity
import com.anubhav.chatapp.adapters.ChatsAdapter
import com.anubhav.chatapp.adapters.StatusAdapter
import com.anubhav.chatapp.databinding.FragmentChatBinding
import com.anubhav.chatapp.models.Status
import com.anubhav.chatapp.models.User
import com.anubhav.chatapp.models.UserStatus
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.drjacky.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.util.*

@Suppress("DEPRECATION")
class ChatsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var chatsList: ArrayList<User>
    private lateinit var adapter: ChatsAdapter
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var statusList: ArrayList<UserStatus>
    private lateinit var dialog: ProgressDialog
    private lateinit var user: User
    private lateinit var chatShimmerFl: ShimmerFrameLayout
    private lateinit var statusShimmerFl: ShimmerFrameLayout
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            val map: HashMap<String, Any> = HashMap()
            map["token"] = it
            database.reference.child("users").child(auth.currentUser!!.uid).updateChildren(map)
        }
    
        dialog = ProgressDialog(requireContext())
        dialog.setMessage("Uploading...")
        dialog.setCancelable(false)

        chatsList = ArrayList()
        statusList = ArrayList()
        
        adapter = ChatsAdapter(requireContext(), chatsList)
        statusAdapter = StatusAdapter(requireContext(), statusList)

        chatShimmerFl = binding.chatShimmer
        statusShimmerFl = binding.statusShimmer
        chatShimmerFl.startShimmer()
        statusShimmerFl.startShimmer()

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding.statusRv.layoutManager = layoutManager
        binding.statusRv.adapter = statusAdapter
        binding.chatRv.adapter = adapter

        database.getReference("users").child(auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", error.message)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User::class.java)!!
                    }
                }
            })

        binding.newMessage.setOnClickListener {
            val intent = Intent(requireContext(), UsersActivity::class.java)
            startActivity(intent)
        }
        
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data!!
                dialog.show()
                val storage = FirebaseStorage.getInstance()
                val date = Date()
                val reference = storage.reference.child("status").child(date.time.toString())
                reference.putFile(uri).addOnSuccessListener {
                    reference.downloadUrl.addOnSuccessListener {
                        val status = UserStatus(user.name, user.profileImage, date.time)
                        val obj: HashMap<String, Any> = HashMap()
                        obj["name"] = status.name
                        obj["profileImg"] = status.profileImg
                        obj["lastUpdated"] = status.lastUpdated

                        val story = Status(it.toString(), status.lastUpdated)

                        database.reference.child("stories")
                            .child(FirebaseAuth.getInstance().uid!!)
                            .updateChildren(obj)

                        database.reference.child("stories")
                            .child(FirebaseAuth.getInstance().uid!!)
                            .child("statuses").push().setValue(story)
                        dialog.dismiss()
                    }
                }
            } else if (result.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(
                    requireContext(),
                    ImagePicker.Companion.getError(result.data),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        binding.addStatus.setOnClickListener {
            ImagePicker.with(requireActivity())
                .crop()
                .createIntentFromDialog {
                    launcher.launch(it)
                }

        }
        
        database.reference.child("stories").addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        statusList.clear()
                        for (storySnapshot in snapshot.children) {
                            val status = UserStatus()
                            status.name =
                                storySnapshot.child("name").getValue(String::class.java)!!
                            status.profileImg =
                                storySnapshot.child("profileImg").getValue(String::class.java)!!

                            status.lastUpdated =
                                storySnapshot.child("lastUpdated").getValue(Long::class.java)!!
                            val statuses: ArrayList<Status> = ArrayList()
                            for (statusSnapshot in storySnapshot.child("statuses").children) {
                                val sampleStatus = statusSnapshot.getValue(Status::class.java)
                                if (sampleStatus != null) {
                                    statuses.add(sampleStatus)
                                }
                            }
                            status.statuses = statuses
                            statusList.add(status)
                        }
                        binding.statusRv.visibility = View.VISIBLE
                        statusAdapter.notifyDataSetChanged()
                    }

                    statusShimmerFl.stopShimmer()
                    statusShimmerFl.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        database.reference.child("users").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        chatsList.clear()
                        for (userSnapshot in p0.children) {
                            val user = userSnapshot.getValue(User::class.java)

                            val senderUid = auth.currentUser!!.uid
                            val receiverUid = user?.uid
                            val senderRoom = senderUid + receiverUid
                            val receiverRoom = receiverUid + senderUid

                            database.reference.child("chats").child(senderRoom)
                                .addValueEventListener(object : ValueEventListener {
                                    @SuppressLint("NotifyDataSetChanged")
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            if (user != null) {
                                                if (!chatsList.contains(user)) {
                                                    chatsList.add(user)
                                                    adapter.notifyDataSetChanged()
                                                }
                                            }

                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }

                                })
                            database.reference.child("chats").child(receiverRoom)
                                .addValueEventListener(object : ValueEventListener {
                                    @SuppressLint("NotifyDataSetChanged")
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            if (user != null) {
                                                if (!chatsList.contains(user)) {
                                                    chatsList.add(user)
                                                    adapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                        }
                        adapter.notifyDataSetChanged()

                        chatShimmerFl.stopShimmer()
                        chatShimmerFl.visibility = View.GONE
                        binding.chatRv.visibility = View.VISIBLE
                    }
                }
            })

    }
    
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

}


