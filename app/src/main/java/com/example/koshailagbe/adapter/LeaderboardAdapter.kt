package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koshailagbe.databinding.ItemLeaderboardBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.bumptech.glide.Glide

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
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivAvatar)
        
        val earnings = if (koshai.earnings >= 1000) "৳${String.format("%.1fk", koshai.earnings / 1000.0)}" else "৳${koshai.earnings}"
        binding.tvEarnings.text = earnings
    }

    override fun getItemCount() = koshais.size

    fun updateList(newList: List<KoshaiProfile>) {
        koshais = newList
        notifyDataSetChanged()
    }
}
