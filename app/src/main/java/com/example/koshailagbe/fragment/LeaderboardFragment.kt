package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.R
import com.example.koshailagbe.adapter.LeaderboardAdapter
import com.example.koshailagbe.databinding.FragmentLeaderboardBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadLeaderboard()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter(emptyList()) { koshai ->
            val bundle = Bundle().apply {
                putString("koshaiId", koshai.id)
            }
            findNavController().navigate(R.id.userKoshaiDetailFragment, bundle)
        }
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = adapter
    }

    private fun loadLeaderboard() {
        binding.shimmerLoading.visibility = View.VISIBLE
        binding.shimmerLoading.startShimmer()
        
        db.collection("koshais")
            .whereEqualTo("isVerified", true)
            .whereEqualTo("isBanned", false)
            .addSnapshotListener { snapshot, e ->
                if (!isAdded) return@addSnapshotListener
                binding.shimmerLoading.stopShimmer()
                binding.shimmerLoading.visibility = View.GONE
                
                if (e != null) {
                    android.util.Log.e("Leaderboard", "Error loading leaderboard", e)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(KoshaiProfile::class.java)?.apply { id = doc.id }
                }?.sortedWith(compareByDescending<KoshaiProfile> { it.rating }.thenByDescending { it.totalJobs }) 
                 ?.take(50) ?: emptyList()
                
                adapter.updateList(list)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
