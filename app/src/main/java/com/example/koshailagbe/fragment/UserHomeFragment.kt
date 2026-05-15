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
import com.bumptech.glide.Glide
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
    private var userDistrict: String? = null
    private var userUpazila: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerViews()
        setupSearch()
        setupChips()
        setupLogout()
        fetchKoshais()
        loadUserProfile()

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

    private fun setupChips() {
        binding.chipAll.setOnClickListener { filterKoshais(null) }
        binding.chipCattleExpert.setOnClickListener { filterByCategory("cattle") }
        binding.chipGoatSheep.setOnClickListener { filterByCategory("goat") }
        binding.chipNearby.setOnClickListener { filterNearby() }
    }

    private fun filterByCategory(category: String) {
        val filtered = when (category) {
            "cattle" -> allKoshais.filter { it.ratePerCow > 0 }
            "goat" -> allKoshais.filter { it.ratePerGoat > 0 || it.ratePerSheep > 0 }
            else -> allKoshais
        }
        updateDiscoveryLists(filtered)
    }

    private fun filterNearby() {
        if (userDistrict == null) {
            updateDiscoveryLists(allKoshais)
            return
        }
        val filtered = allKoshais.filter {
            it.district.contains(userDistrict!!, ignoreCase = true) ||
            it.upazila.contains(userUpazila ?: "", ignoreCase = true)
        }
        updateDiscoveryLists(filtered)
    }

    private fun updateDiscoveryLists(list: List<KoshaiProfile>) {
        nearbyAdapter.updateList(list)
        
        // Toggle visibility of empty state
        if (list.isEmpty()) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.rvNearby.visibility = View.GONE
            binding.sectionTopRated.visibility = View.GONE
            binding.rvTopRated.visibility = View.GONE
            binding.lblNearby.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.rvNearby.visibility = View.VISIBLE
            binding.sectionTopRated.visibility = View.VISIBLE
            binding.rvTopRated.visibility = View.VISIBLE
            binding.lblNearby.visibility = View.VISIBLE
            
            // Only update top rated if it's the "All" view or if results are plenty
            val topRated = list.sortedByDescending { it.rating }.take(5)
            topRatedAdapter.updateList(topRated)
        }
    }

    private fun filterKoshais(query: String?) {
        if (query.isNullOrEmpty()) {
            updateDiscoveryLists(allKoshais)
            return
        }

        val filtered = allKoshais.filter {
            it.district.contains(query, ignoreCase = true) || 
            it.upazila.contains(query, ignoreCase = true) ||
            it.name.contains(query, ignoreCase = true)
        }
        
        updateDiscoveryLists(filtered)
    }

    private fun fetchKoshais() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("koshais").addSnapshotListener { snapshots, e ->
            if (!isAdded) return@addSnapshotListener
            binding.progressBar.visibility = View.GONE
            
            if (e != null || snapshots == null) return@addSnapshotListener
            
            allKoshais = snapshots.documents.mapNotNull { doc ->
                doc.toObject(KoshaiProfile::class.java)?.apply { id = doc.id }
            }
            
            updateDiscoveryLists(allKoshais)
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

        binding.ibProfile.setOnClickListener {
            findNavController().navigate(R.id.action_userHomeFragment_to_userProfileFragment)
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                
                userDistrict = doc.getString("district")
                userUpazila = doc.getString("upazila")
                
                val photoUrl = doc.getString("photoUrl")
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ibProfile)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}