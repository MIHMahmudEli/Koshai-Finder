package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koshailagbe.databinding.ItemEarningBinding
import com.example.koshailagbe.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class EarningAdapter(
    private var bookings: List<Booking>
) : RecyclerView.Adapter<EarningAdapter.EarningViewHolder>() {

    inner class EarningViewHolder(val binding: ItemEarningBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarningViewHolder {
        val binding = ItemEarningBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EarningViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EarningViewHolder, position: Int) {
        val booking = bookings[position]
        val binding = holder.binding

        binding.tvDescription.text = "Booking #${booking.id.takeLast(6).uppercase()}"
        
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvDate.text = sdf.format(booking.date.toDate())
        
        val total = booking.rateBreakdown["total"] ?: 0.0
        binding.tvAmount.text = "+৳${String.format("%.0f", total)}"
    }

    override fun getItemCount() = bookings.size

    fun updateList(newList: List<Booking>) {
        bookings = newList
        notifyDataSetChanged()
    }
}
