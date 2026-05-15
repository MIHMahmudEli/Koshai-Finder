package com.example.koshailagbe.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.ItemKoshaiHorizontalBinding
import com.example.koshailagbe.databinding.ItemKoshaiVerticalBinding
import com.example.koshailagbe.model.KoshaiProfile

class KoshaiDiscoveryAdapter(
    private var koshais: List<KoshaiProfile>,
    private val isHorizontal: Boolean,
    private val onKoshaiClick: (KoshaiProfile) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_HORIZONTAL = 0
        private const val VIEW_VERTICAL = 1
    }

    inner class HorizontalViewHolder(val binding: ItemKoshaiHorizontalBinding) : RecyclerView.ViewHolder(binding.root)
    inner class VerticalViewHolder(val binding: ItemKoshaiVerticalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int) = if (isHorizontal) VIEW_HORIZONTAL else VIEW_VERTICAL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_HORIZONTAL) {
            HorizontalViewHolder(ItemKoshaiHorizontalBinding.inflate(inflater, parent, false))
        } else {
            VerticalViewHolder(ItemKoshaiVerticalBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val koshai = koshais[position]
        if (holder is HorizontalViewHolder) {
            holder.binding.tvName.text = koshai.name
            holder.binding.ivVerified.visibility = if (koshai.isVerified) View.VISIBLE else View.GONE
            holder.binding.tvRating.text = String.format("%.1f", koshai.rating)
            holder.binding.tvJobs.text = "(${koshai.totalJobs})"
            holder.binding.tvStatus.text = koshai.status.replaceFirstChar { it.uppercase() }
            
            val statusColor = when(koshai.status) {
                "online" -> "#2E7D32"
                "busy" -> "#F57C00"
                else -> "#757575"
            }
            holder.binding.tvStatus.setTextColor(Color.parseColor(statusColor))

            Glide.with(holder.itemView.context)
                .load(koshai.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(holder.binding.ivKoshai)
            
            holder.itemView.setOnClickListener { onKoshaiClick(koshai) }
        } else if (holder is VerticalViewHolder) {
            holder.binding.tvName.text = koshai.name
            holder.binding.ivVerified.visibility = if (koshai.isVerified) View.VISIBLE else View.GONE
            holder.binding.tvLocation.text = "${koshai.upazila}, ${koshai.district}"
            holder.binding.tvRating.text = String.format("%.1f", koshai.rating)
            holder.binding.tvJobs.text = "(${koshai.totalJobs})"
            holder.binding.tvPrice.text = "৳${String.format("%.0f", koshai.ratePerCow)}"

            Glide.with(holder.itemView.context)
                .load(koshai.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(holder.binding.ivKoshai)
            
            holder.itemView.setOnClickListener { onKoshaiClick(koshai) }
            holder.binding.btnBook.setOnClickListener { onKoshaiClick(koshai) }
        }
    }

    override fun getItemCount() = koshais.size

    fun updateList(newList: List<KoshaiProfile>) {
        koshais = newList
        notifyDataSetChanged()
    }
}
