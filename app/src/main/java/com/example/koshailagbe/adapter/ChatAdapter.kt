package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.koshailagbe.databinding.ItemChatReceivedBinding
import com.example.koshailagbe.databinding.ItemChatSentBinding
import com.example.koshailagbe.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val currentUserId: String,
    private var messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    abstract class ChatViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(message: ChatMessage)
    }

    class SentViewHolder(val binding: ItemChatSentBinding) : ChatViewHolder(binding) {
        override fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.message
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.tvTime.text = sdf.format(message.timestamp.toDate())
        }
    }

    class ReceivedViewHolder(val binding: ItemChatReceivedBinding) : ChatViewHolder(binding) {
        override fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.message
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.tvTime.text = sdf.format(message.timestamp.toDate())
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return if (viewType == TYPE_SENT) {
            SentViewHolder(ItemChatSentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            ReceivedViewHolder(ItemChatReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}
