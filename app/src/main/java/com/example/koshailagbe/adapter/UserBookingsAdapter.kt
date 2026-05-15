package com.example.koshailagbe.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koshailagbe.databinding.ItemUserBookingBinding
import com.example.koshailagbe.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class UserBookingsAdapter(
    private var bookings: List<Booking>,
    private val onItemClick: (Booking) -> Unit,
    private val onReviewClick: (Booking) -> Unit
) : RecyclerView.Adapter<UserBookingsAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(val binding: ItemUserBookingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemUserBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        val binding = holder.binding

        binding.tvKoshaiName.text = booking.koshaiName
        
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.tvDateTime.text = "${sdf.format(booking.date.toDate())} | ${booking.slot}"
        
        binding.tvAddress.text = booking.address
        binding.tvPrice.text = "৳${booking.rateBreakdown["total"]?.toInt()}"
        
        val animals = mutableListOf<String>()
        booking.animalTypes["cow"]?.takeIf { it > 0 }?.let { animals.add("$it Cow") }
        booking.animalTypes["goat"]?.takeIf { it > 0 }?.let { animals.add("$it Goat") }
        binding.tvAnimals.text = animals.joinToString(", ")

        // Status styling
        binding.tvStatus.text = booking.status.uppercase()
        val color = when (booking.status) {
            "pending" -> "#FBC02D"
            "confirmed" -> "#2E7D32"
            "en_route", "arrived" -> "#1976D2"
            "completed" -> "#455A64"
            else -> "#D32F2F" // cancelled/declined
        }
        binding.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))

        // Review button visibility
        if (booking.status == "completed" && !booking.isReviewed) {
            binding.btnReview.visibility = android.view.View.VISIBLE
        } else {
            binding.btnReview.visibility = android.view.View.GONE
        }

        binding.btnReview.setOnClickListener { onReviewClick(booking) }
        binding.root.setOnClickListener { onItemClick(booking) }
    }

    override fun getItemCount() = bookings.size

    fun updateList(newList: List<Booking>) {
        bookings = newList
        notifyDataSetChanged()
    }
}
