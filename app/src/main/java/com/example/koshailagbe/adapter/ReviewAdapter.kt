package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koshailagbe.databinding.ItemReviewBinding
import com.example.koshailagbe.model.Review
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(private var reviews: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        val binding = holder.binding

        binding.tvUserName.text = review.userName
        binding.ratingBar.rating = review.rating
        binding.tvComment.text = review.comment
        
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.tvDate.text = sdf.format(review.timestamp.toDate())
    }

    override fun getItemCount() = reviews.size

    fun updateList(newList: List<Review>) {
        reviews = newList
        notifyDataSetChanged()
    }
}
