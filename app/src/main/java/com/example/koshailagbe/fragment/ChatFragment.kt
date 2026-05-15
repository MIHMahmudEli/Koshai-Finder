package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.adapter.ChatAdapter
import com.example.koshailagbe.databinding.FragmentChatBinding
import com.example.koshailagbe.model.ChatMessage
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ChatAdapter
    
    private var bookingId: String? = null
    private var receiverId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        bookingId = arguments?.getString("bookingId")
        receiverId = arguments?.getString("receiverId")

        if (bookingId == null || receiverId == null) {
            showSnackBar("Error: Invalid chat session", isError = true)
            findNavController().popBackStack()
        } else {
            setupRecyclerView()
            setupToolbar()
            listenForMessages()
            setupSendButton()
        }

        return binding.root
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(auth.currentUser?.uid ?: "", emptyList())
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun sendMessage(text: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val message = ChatMessage(
            senderId = currentUserId,
            receiverId = receiverId!!,
            message = text,
            timestamp = Timestamp.now(),
            bookingId = bookingId!!
        )

        binding.etMessage.setText("")
        db.collection("chats").add(message)
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                showSnackBar("Failed to send: ${it.message}", isError = true)
            }
    }

    private fun listenForMessages() {
        db.collection("chats")
            .whereEqualTo("bookingId", bookingId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (!isAdded) return@addSnapshotListener
                if (e != null) {
                    showSnackBar("Chat error: ${e.message}", isError = true)
                    return@addSnapshotListener
                }

                val messages = snapshots?.toObjects(ChatMessage::class.java) ?: emptyList()
                adapter.updateMessages(messages)
                if (messages.isNotEmpty()) {
                    binding.rvMessages.smoothScrollToPosition(messages.size - 1)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
