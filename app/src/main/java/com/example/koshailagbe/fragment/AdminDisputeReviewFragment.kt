package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.adapter.AdminModerationAdapter
import com.example.koshailagbe.databinding.FragmentAdminDisputeReviewBinding
import com.example.koshailagbe.model.Report
import com.example.koshailagbe.model.Review
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminDisputeReviewFragment : Fragment() {

    private var _binding: FragmentAdminDisputeReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdminModerationAdapter

    private var allReviews = listOf<Review>()
    private var allReports = listOf<Report>()
    private var currentTab = 0 // 0 for Reviews, 1 for Reports

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDisputeReviewBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupTabs()
        loadData()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = AdminModerationAdapter(
            emptyList(),
            onReviewAction = { review, action -> handleReviewAction(review, action) },
            onReportAction = { report -> resolveReport(report) }
        )
        binding.rvModeration.layoutManager = LinearLayoutManager(requireContext())
        binding.rvModeration.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                displayCurrentTab()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        
        // 1. Load All Reviews (Collection Group Query)
        db.collectionGroup("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (!isAdded) return@addSnapshotListener
                
                if (e != null) {
                    Toast.makeText(requireContext(), "Review Load Error: ${e.message}", Toast.LENGTH_LONG).show()
                    android.util.Log.e("AdminModeration", "Error fetching reviews", e)
                    return@addSnapshotListener
                }

                allReviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Review::class.java)?.apply { 
                        id = doc.id
                        koshaiId = doc.reference.parent.parent?.id ?: ""
                    }
                } ?: emptyList()
                if (currentTab == 0) displayCurrentTab()
            }

        // 2. Load All Reports
        db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (!isAdded) return@addSnapshotListener
                binding.progressBar.visibility = View.GONE
                allReports = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Report::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                if (currentTab == 1) displayCurrentTab()
            }
    }

    private fun displayCurrentTab() {
        if (currentTab == 0) adapter.updateList(allReviews)
        else adapter.updateList(allReports)
    }

    private fun handleReviewAction(review: Review, action: String) {
        if (review.koshaiId.isEmpty()) return
        
        val docRef = db.collection("koshais").document(review.koshaiId)
            .collection("reviews").document(review.id)
            
        if (action == "hide") {
            val newHidden = !review.isHidden
            docRef.update("isHidden", newHidden).addOnSuccessListener {
                Toast.makeText(requireContext(), if (newHidden) "Review Hidden" else "Review Restored", Toast.LENGTH_SHORT).show()
            }
        } else if (action == "delete") {
            docRef.delete().addOnSuccessListener {
                Toast.makeText(requireContext(), "Review Deleted Permanently", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resolveReport(report: Report) {
        db.collection("reports").document(report.id)
            .update("status", "resolved")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Report Marked as Resolved", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
