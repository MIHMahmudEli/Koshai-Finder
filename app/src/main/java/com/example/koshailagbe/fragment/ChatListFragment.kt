package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.R
import com.example.koshailagbe.adapter.ChatListAdapter
import com.example.koshailagbe.databinding.FragmentChatListBinding
import com.example.koshailagbe.model.ChatRoom
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchChatRooms()

        return binding.root
    }

    private fun setupRecyclerView() {
        val currentUserId = auth.currentUser?.uid ?: ""
        adapter = ChatListAdapter(currentUserId, emptyList()) { room ->
            val otherUserId = room.participants.firstOrNull { it != currentUserId } ?: ""
            val bundle = Bundle().apply {
                putString("receiverId", otherUserId)
                putString("receiverName", room.userNames[otherUserId])
                putString("receiverPhoto", room.userPhotos[otherUserId])
            }
            findNavController().navigate(R.id.action_chatListFragment_to_chatFragment, bundle)
        }
        binding.rvChatRooms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatRooms.adapter = adapter
    }

    private fun fetchChatRooms() {
        val userId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        db.collection("chatRooms")
            .whereArrayContains("participants", userId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (!isAdded) return@addSnapshotListener
                binding.progressBar.visibility = View.GONE

                if (e != null) {
                    showSnackBar("Failed to load chats: ${e.message}")
                    return@addSnapshotListener
                }

                val rooms = snapshots?.toObjects(ChatRoom::class.java) ?: emptyList()
                adapter.updateList(rooms)
                binding.emptyState.visibility = if (rooms.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
