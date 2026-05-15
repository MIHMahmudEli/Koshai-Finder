package com.example.koshailagbe.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koshailagbe.databinding.ItemBookingBinding
import com.example.koshailagbe.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onAccept: (Booking) -> Unit,
    private val onDecline: (Booking) -> Unit,
    private val onUpdateStatus: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(val binding: ItemBookingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        val binding = holder.binding

        binding.tvUserName.text = "Customer (ID: ${booking.userId.takeLast(6)})" // Placeholder name
        
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateStr = sdf.format(booking.date.toDate())
        binding.tvDateSlot.text = "$dateStr | ${booking.slot}"
        
        binding.tvAddress.text = booking.address
        
        val animals = mutableListOf<String>()
        booking.animalTypes["cow"]?.takeIf { it > 0 }?.let { animals.add("$it Cow") }
        booking.animalTypes["goat"]?.takeIf { it > 0 }?.let { animals.add("$it Goat") }
        booking.animalTypes["sheep"]?.takeIf { it > 0 }?.let { animals.add("$it Sheep") }
        binding.tvAnimals.text = animals.joinToString(", ")

        // Status styling
        binding.tvStatusLabel.text = booking.status.uppercase()
        when (booking.status) {
            "pending" -> {
                binding.tvStatusLabel.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FBC02D"))
                binding.btnAccept.visibility = View.VISIBLE
                binding.btnDecline.visibility = View.VISIBLE
                binding.btnUpdateStatus.visibility = View.GONE
            }
            "confirmed" -> {
                binding.tvStatusLabel.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32"))
                binding.btnAccept.visibility = View.GONE
                binding.btnDecline.visibility = View.GONE
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.text = "Start Job"
            }
            "en_route" -> {
                binding.tvStatusLabel.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))
                binding.btnAccept.visibility = View.GONE
                binding.btnDecline.visibility = View.GONE
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.text = "Arrived"
            }
            "arrived" -> {
                binding.tvStatusLabel.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2"))
                binding.btnAccept.visibility = View.GONE
                binding.btnDecline.visibility = View.GONE
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.text = "Complete"
            }
            "completed" -> {
                binding.tvStatusLabel.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#455A64"))
                binding.btnAccept.visibility = View.GONE
                binding.btnDecline.visibility = View.GONE
                binding.btnUpdateStatus.visibility = View.GONE
            }
            "cancelled" -> {
                binding.tvStatusLabel.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                binding.btnAccept.visibility = View.GONE
                binding.btnDecline.visibility = View.GONE
                binding.btnUpdateStatus.visibility = View.GONE
            }
        }

        binding.btnAccept.setOnClickListener { onAccept(booking) }
        binding.btnDecline.setOnClickListener { onDecline(booking) }
        binding.btnUpdateStatus.setOnClickListener { onUpdateStatus(booking) }
    }

    override fun getItemCount() = bookings.size

    fun updateList(newList: List<Booking>) {
        bookings = newList
        notifyDataSetChanged()
    }
}
