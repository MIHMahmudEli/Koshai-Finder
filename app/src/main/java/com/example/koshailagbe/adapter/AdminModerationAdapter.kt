package com.example.koshailagbe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.koshailagbe.databinding.ItemAdminReportBinding
import com.example.koshailagbe.databinding.ItemAdminReviewBinding
import com.example.koshailagbe.model.Report
import com.example.koshailagbe.model.Review
import java.text.SimpleDateFormat
import java.util.*

class AdminModerationAdapter(
    private var list: List<Any>,
    private val onReviewAction: (Review, String) -> Unit, // "hide" or "delete"
    private val onReportAction: (Report) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_REVIEW = 0
        const val TYPE_REPORT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is Review) TYPE_REVIEW else TYPE_REPORT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_REVIEW) {
            ReviewViewHolder(ItemAdminReviewBinding.inflate(inflater, parent, false))
        } else {
            ReportViewHolder(ItemAdminReportBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        if (holder is ReviewViewHolder) bindReview(holder, item as Review)
        else if (holder is ReportViewHolder) bindReport(holder, item as Report)
    }

    private fun bindReview(holder: ReviewViewHolder, review: Review) {
        holder.binding.tvUserName.text = review.userName
        holder.binding.ratingBar.rating = review.rating
        holder.binding.tvComment.text = review.comment
        
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.binding.tvDate.text = sdf.format(review.timestamp.toDate())

        holder.binding.ivHidden.visibility = if (review.isHidden) View.VISIBLE else View.GONE
        holder.binding.btnHide.text = if (review.isHidden) "Show Review" else "Hide Review"

        holder.binding.btnHide.setOnClickListener { onReviewAction(review, "hide") }
        holder.binding.btnDelete.setOnClickListener { onReviewAction(review, "delete") }
    }

    private fun bindReport(holder: ReportViewHolder, report: Report) {
        holder.binding.tvReporterName.text = "By: ${report.reporterName}"
        holder.binding.tvTargetName.text = "Reported: ${report.reportedEntityName}"
        holder.binding.tvReason.text = report.reason
        holder.binding.tvDetails.text = report.details
        
        holder.binding.chipStatus.text = report.status.uppercase()
        if (report.status == "resolved") {
            holder.binding.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_dark)
            holder.binding.btnResolve.visibility = View.GONE
        } else {
            holder.binding.chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_dark)
            holder.binding.btnResolve.visibility = View.VISIBLE
        }

        holder.binding.btnResolve.setOnClickListener { onReportAction(report) }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Any>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ReviewViewHolder(val binding: ItemAdminReviewBinding) : RecyclerView.ViewHolder(binding.root)
    inner class ReportViewHolder(val binding: ItemAdminReportBinding) : RecyclerView.ViewHolder(binding.root)
}
