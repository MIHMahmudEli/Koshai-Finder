package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.koshailagbe.R
import com.example.koshailagbe.adapter.ChatAdapter
import com.example.koshailagbe.databinding.FragmentChatBinding
import com.example.koshailagbe.model.ChatMessage
import com.example.koshailagbe.model.ChatRoom
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
    
    private var chatRoomId: String? = null
    private var receiverId: String? = null
    private var receiverName: String? = null
    private var receiverPhoto: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        receiverId = arguments?.getString("receiverId")
        receiverName = arguments?.getString("receiverName")
        receiverPhoto = arguments?.getString("receiverPhoto")

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || receiverId == null) {
            showSnackBar("Error: Invalid chat session")
            findNavController().popBackStack()
            return binding.root
        }

        // Generate a consistent ChatRoom ID for these two participants
        chatRoomId = if (currentUserId < receiverId!!) "${currentUserId}_${receiverId}" else "${receiverId}_${currentUserId}"

        setupUI()
        setupRecyclerView()
        listenForMessages()
        setupSendButton()
        createOrUpdateChatRoom()

        return binding.root
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.tvChatPartnerName.text = receiverName ?: "Koshai"
        
        Glide.with(this)
            .load(receiverPhoto)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .into(binding.ivPartnerProfile)
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
        val roomId = chatRoomId ?: return

        val message = ChatMessage(
            senderId = currentUserId,
            text = text,
            timestamp = Timestamp.now()
        )

        binding.etMessage.setText("")

        // Add to sub-collection
        db.collection("chatRooms").document(roomId)
            .collection("messages").add(message)
            .addOnSuccessListener {
                // Update room's last message
                db.collection("chatRooms").document(roomId)
                    .update(
                        "lastMessage", text,
                        "lastTimestamp", Timestamp.now()
                    )
            }
            .addOnFailureListener {
                if (isAdded) showSnackBar("Failed to send: ${it.message}")
            }
    }

    private fun createOrUpdateChatRoom() {
        val currentUserId = auth.currentUser?.uid ?: return
        val roomId = chatRoomId ?: return
        val rId = receiverId ?: return

        val role = com.example.koshailagbe.utils.SharedPrefsHelper.getUserRole(requireContext())
        val myCollection = if (role == com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI) "koshais" else "users"
        val otherCollection = if (role == com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI) "users" else "koshais"

        db.collection(myCollection).document(currentUserId).get().addOnSuccessListener { myDoc ->
            val myName = myDoc.getString("name") ?: auth.currentUser?.displayName ?: "User"

            db.collection(otherCollection).document(rId).get().addOnSuccessListener { otherDoc ->
                val otherName = otherDoc.getString("name") ?: receiverName ?: "User"
                
                if (isAdded) {
                    binding.tvChatPartnerName.text = otherName
                }

                val roomRef = db.collection("chatRooms").document(roomId)
                roomRef.get().addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        val room = ChatRoom(
                            id = roomId,
                            participants = listOf(currentUserId, rId).sorted(),
                            userNames = mapOf(currentUserId to myName, rId to otherName),
                            userPhotos = mapOf(currentUserId to "", rId to (receiverPhoto ?: "")),
                            lastTimestamp = Timestamp.now()
                        )
                        roomRef.set(room)
                    } else {
                        val updates = mapOf(
                            "userNames.$currentUserId" to myName,
                            "userNames.$rId" to otherName
                        )
                        roomRef.update(updates)
                    }
                }
            }
        }
    }

    private fun listenForMessages() {
        val roomId = chatRoomId ?: return
        db.collection("chatRooms").document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (!isAdded) return@addSnapshotListener
                if (e != null) return@addSnapshotListener

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
