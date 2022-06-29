package com.anubhav.babble.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.anubhav.babble.R
import com.anubhav.babble.activities.OutgoingCall
import com.anubhav.babble.adapters.CallsAdapter
import com.anubhav.babble.databinding.FragmentCallBinding
import com.anubhav.babble.models.Call
import com.anubhav.babble.models.Message
import com.anubhav.babble.models.User
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
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
import okhttp3.internal.wait
import java.util.ArrayList

class CallFragment : Fragment() {

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!
    private lateinit var callsList: ArrayList<Call>
    private lateinit var sender : User
    private lateinit var receiver : User
    private lateinit var adapter : CallsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        callsList = ArrayList()

        adapter = CallsAdapter(requireContext(), callsList)
        binding.callShimmer.startShimmer()
        binding.callsRv.adapter = adapter


        Firebase.database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    callsList.clear()
                    for (userSnapshot in p0.children) {
                        val user = userSnapshot.getValue(User::class.java)

                        val senderUid = Firebase.auth.currentUser!!.uid


                        Firebase.database.reference.child("calls")
                            .addValueEventListener(object : ValueEventListener {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.hasChild(senderUid)) {
                                        Firebase.database.reference.child("calls").child(senderUid)
                                            .addValueEventListener(object : ValueEventListener {
                                                @SuppressLint("NotifyDataSetChanged")
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    callsList!!.clear()
                                                    for (snapshot1 in snapshot.children) {
                                                        val call = snapshot1.getValue(Call::class.java)
                                                        if (call != null) {
                                                            if (!callsList.contains(call)) {
                                                                callsList.add(call)
                                                                adapter.notifyDataSetChanged()
                                                            }
                                                        }

                                                    }
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    }
                }
            }
        })
    }


                        override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(view?.findViewById(R.id.toolbar))
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}