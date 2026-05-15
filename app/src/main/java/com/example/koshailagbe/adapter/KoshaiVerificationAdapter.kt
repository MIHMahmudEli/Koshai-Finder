package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.ItemKoshaiVerificationBinding
import com.example.koshailagbe.model.KoshaiProfile

class KoshaiVerificationAdapter(
    private var list: List<KoshaiProfile>,
    private val onApprove: (KoshaiProfile) -> Unit,
    private val onReject: (KoshaiProfile) -> Unit
) : RecyclerView.Adapter<KoshaiVerificationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemKoshaiVerificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKoshaiVerificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val koshai = list[position]
        holder.binding.tvName.text = koshai.name
        holder.binding.tvLocation.text = "${koshai.upazila}, ${koshai.district}"
        holder.binding.tvPhone.text = koshai.phone

        Glide.with(holder.itemView.context)
            .load(koshai.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(holder.binding.ivKoshai)

        holder.binding.btnApprove.setOnClickListener { onApprove(koshai) }
        holder.binding.btnReject.setOnClickListener { onReject(koshai) }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<KoshaiProfile>) {
        list = newList
        notifyDataSetChanged()
    }
}
