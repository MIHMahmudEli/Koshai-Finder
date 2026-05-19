package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.ItemLeaderboardBinding
import com.example.koshailagbe.model.KoshaiProfile

class LeaderboardAdapter(
    private var list: List<KoshaiProfile>,
    private val onItemClick: (KoshaiProfile) -> Unit
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val koshai = list[position]
        val rank = position + 1
        
        with(holder.binding) {
            val context = root.context
            tvRank.text = rank.toString()
            tvName.text = koshai.name
            tvLocation.text = context.getString(R.string.leaderboard_location_format, koshai.upazila, koshai.district)
            tvRating.text = context.getString(R.string.leaderboard_rating_format, koshai.rating)
            tvJobsDone.text = context.getString(R.string.leaderboard_jobs_format, koshai.totalJobs)

            // Highlight top 3
            when (rank) {
                1 -> {
                    tvRank.setTextColor(android.graphics.Color.parseColor("#FFD700")) // Gold
                    tvRank.textSize = 24f
                }
                2 -> {
                    tvRank.setTextColor(android.graphics.Color.parseColor("#C0C0C0")) // Silver
                    tvRank.textSize = 20f
                }
                3 -> {
                    tvRank.setTextColor(android.graphics.Color.parseColor("#CD7F32")) // Bronze
                    tvRank.textSize = 18f
                }
                else -> {
                    tvRank.setTextColor(android.graphics.Color.parseColor("#1A237E"))
                    tvRank.textSize = 18f
                }
            }

            Glide.with(ivProfile.context)
                .load(koshai.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)

            root.setOnClickListener { onItemClick(koshai) }
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<KoshaiProfile>) {
        list = newList
        notifyDataSetChanged()
    }
}
