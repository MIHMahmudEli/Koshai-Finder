package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.ItemAdminManagementBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.example.koshailagbe.model.User

class AdminManagementAdapter(
    private var list: List<Any>,
    private val onBanToggle: (Any) -> Unit
) : RecyclerView.Adapter<AdminManagementAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAdminManagementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminManagementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        when (item) {
            is User -> bindUser(holder, item)
            is KoshaiProfile -> bindKoshai(holder, item)
        }
    }

    private fun bindUser(holder: ViewHolder, user: User) {
        holder.binding.tvName.text = user.name
        holder.binding.tvDetail.text = "${user.phone} • ${user.upazila}"
        
        Glide.with(holder.itemView.context)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(holder.binding.ivAvatar)

        updateStatus(holder, user.isBanned)
        holder.binding.btnBan.setOnClickListener { onBanToggle(user) }
    }

    private fun bindKoshai(holder: ViewHolder, koshai: KoshaiProfile) {
        holder.binding.tvName.text = koshai.name
        holder.binding.tvDetail.text = "${koshai.phone} • ${koshai.district}"
        
        Glide.with(holder.itemView.context)
            .load(koshai.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(holder.binding.ivAvatar)

        updateStatus(holder, koshai.isBanned)
        holder.binding.btnBan.setOnClickListener { onBanToggle(koshai) }
    }

    private fun updateStatus(holder: ViewHolder, isBanned: Boolean) {
        if (isBanned) {
            holder.binding.chipStatus.text = "Banned"
            holder.binding.chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_dark)
            holder.binding.btnBan.text = "Unban"
        } else {
            holder.binding.chipStatus.text = "Active"
            holder.binding.chipStatus.setChipBackgroundColorResource(R.color.primary_green)
            holder.binding.btnBan.text = "Ban Account"
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Any>) {
        list = newList
        notifyDataSetChanged()
    }
}
