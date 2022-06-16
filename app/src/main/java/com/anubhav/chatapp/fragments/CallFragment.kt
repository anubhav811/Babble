package com.anubhav.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anubhav.chatapp.R
import com.anubhav.chatapp.databinding.FragmentCallBinding
import com.anubhav.chatapp.databinding.FragmentChatBinding

class CallFragment : Fragment() {

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }


}