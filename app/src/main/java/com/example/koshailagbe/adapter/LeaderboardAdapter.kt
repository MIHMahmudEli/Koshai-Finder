package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koshailagbe.databinding.ItemLeaderboardBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.bumptech.glide.Glide
import com.example.koshailagbe.R

class LeaderboardAdapter(
    private var koshais: List<KoshaiProfile>
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    inner class LeaderboardViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val koshai = koshais[position]
        val binding = holder.binding

        binding.tvRank.text = (position + 4).toString()
        binding.tvName.text = koshai.name
        binding.tvStats.text = "${String.format("%.1f", koshai.rating)} ★ | ${koshai.totalJobs} Jobs"
        
        Glide.with(holder.itemView.context)
            .load(koshai.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .into(binding.ivAvatar)
        
        val earningsFormatted = when {
            koshai.earnings >= 1_000_000 -> "৳${String.format("%.1fM", koshai.earnings / 1_000_000.0)}"
            koshai.earnings >= 1_000     -> {
                val s = String.format("%.1fk", koshai.earnings / 1_000.0)
                "৳${if (s.endsWith(".0k")) s.replace(".0k", "k") else s}"
            }
            else -> "৳${koshai.earnings.toInt()}"
        }
        binding.tvEarnings.text = earningsFormatted
    }

    override fun getItemCount() = koshais.size

    fun updateList(newList: List<KoshaiProfile>) {
        koshais = newList
        notifyDataSetChanged()
    }
}
