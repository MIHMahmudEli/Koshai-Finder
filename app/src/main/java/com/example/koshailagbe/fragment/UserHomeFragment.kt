package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.R
import com.example.koshailagbe.adapter.KoshaiDiscoveryAdapter
import com.example.koshailagbe.databinding.FragmentUserHomeBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.example.koshailagbe.utils.SharedPrefsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private lateinit var topRatedAdapter: KoshaiDiscoveryAdapter
    private lateinit var nearbyAdapter: KoshaiDiscoveryAdapter
    
    private var allKoshais = listOf<KoshaiProfile>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerViews()
        setupSearch()
        setupLogout()
        fetchKoshais()

        return binding.root
    }

    private fun setupRecyclerViews() {
        // Top Rated (Horizontal)
        topRatedAdapter = KoshaiDiscoveryAdapter(emptyList(), true) { koshai ->
            navigateToDetail(koshai)
        }
        binding.rvTopRated.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTopRated.adapter = topRatedAdapter

        // Nearby (Vertical)
        nearbyAdapter = KoshaiDiscoveryAdapter(emptyList(), false) { koshai ->
            navigateToDetail(koshai)
        }
        binding.rvNearby.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNearby.adapter = nearbyAdapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterKoshais(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterKoshais(newText)
                return true
            }
        })
    }

    private fun filterKoshais(query: String?) {
        if (query.isNullOrEmpty()) {
            topRatedAdapter.updateList(allKoshais.sortedByDescending { it.rating }.take(5))
            nearbyAdapter.updateList(allKoshais)
            return
        }

        val filtered = allKoshais.filter {
            it.district.contains(query, ignoreCase = true) || 
            it.upazila.contains(query, ignoreCase = true) ||
            it.name.contains(query, ignoreCase = true)
        }
        
        nearbyAdapter.updateList(filtered)
    }

    private fun fetchKoshais() {
        db.collection("koshais")
            .get()
            .addOnSuccessListener { snapshots ->
                if (!isAdded) return@addOnSuccessListener
                
                allKoshais = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(KoshaiProfile::class.java)?.apply { id = doc.id }
                }
                
                // Top Rated: Sorted by rating
                val topRated = allKoshais.sortedByDescending { it.rating }.take(5)
                topRatedAdapter.updateList(topRated)
                
                // Available: Sorted by status (Online first)
                val sorted = allKoshais.sortedWith(compareByDescending<KoshaiProfile> { it.status == "online" }.thenByDescending { it.rating })
                nearbyAdapter.updateList(sorted)
            }
    }

    private fun navigateToDetail(koshai: KoshaiProfile) {
        val bundle = Bundle().apply { putString("koshaiId", koshai.id) }
        findNavController().navigate(R.id.action_userHomeFragment_to_userKoshaiDetailFragment, bundle)
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            SharedPrefsHelper.clearUserRole(requireContext())
            auth.signOut()
            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.userHomeFragment, true)
                    .build()
            )
        }

        binding.ibChatList.setOnClickListener {
            findNavController().navigate(R.id.chatListFragment)
        }

        binding.fabBookings.setOnClickListener {
            findNavController().navigate(R.id.action_userHomeFragment_to_userBookingsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}