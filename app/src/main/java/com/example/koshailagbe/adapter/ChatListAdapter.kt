package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.ItemChatRoomBinding
import com.example.koshailagbe.model.ChatRoom
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(
    private val currentUserId: String,
    private var chatRooms: List<ChatRoom>,
    private val onItemClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder>() {

    inner class ChatRoomViewHolder(val binding: ItemChatRoomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val binding = ItemChatRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatRoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val room = chatRooms[position]
        val binding = holder.binding

        // Get the other participant's ID
        val otherUserId = room.participants.firstOrNull { it != currentUserId } ?: ""
        
        binding.tvName.text = room.userNames[otherUserId] ?: "User"
        binding.tvLastMessage.text = room.lastMessage.ifEmpty { "No messages yet" }
        
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.tvTime.text = sdf.format(room.lastTimestamp.toDate())

        Glide.with(holder.itemView.context)
            .load(room.userPhotos[otherUserId])
            .placeholder(R.drawable.bg_dashboard_header)
            .into(binding.ivProfile)

        binding.root.setOnClickListener { onItemClick(room) }
    }

    override fun getItemCount() = chatRooms.size

    fun updateList(newList: List<ChatRoom>) {
        chatRooms = newList
        notifyDataSetChanged()
    }
}
