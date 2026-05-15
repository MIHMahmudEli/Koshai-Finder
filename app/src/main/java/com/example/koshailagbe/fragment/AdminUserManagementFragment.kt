package com.example.koshailagbe.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.R
import com.example.koshailagbe.adapter.AdminManagementAdapter
import com.example.koshailagbe.databinding.FragmentAdminUserManagementBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.example.koshailagbe.model.User
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore

class AdminUserManagementFragment : Fragment() {

    private var _binding: FragmentAdminUserManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdminManagementAdapter

    private var allUsers = listOf<User>()
    private var allKoshais = listOf<KoshaiProfile>()
    private var currentTab = 0 // 0 for Users, 1 for Koshais

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUserManagementBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupTabs()
        setupSearch()
        loadData()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = AdminManagementAdapter(emptyList()) { item ->
            toggleBanStatus(item)
        }
        binding.rvManagement.layoutManager = LinearLayoutManager(requireContext())
        binding.rvManagement.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                filterAndDisplay()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterAndDisplay() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadData() {
        binding.shimmerLoading.visibility = View.VISIBLE
        binding.shimmerLoading.startShimmer()
        
        // Listen to Users
        db.collection("users").addSnapshotListener { snapshot, _ ->
            if (!isAdded) return@addSnapshotListener
            binding.shimmerLoading.stopShimmer()
            binding.shimmerLoading.visibility = View.GONE

            allUsers = snapshot?.documents?.mapNotNull { doc ->
                val user = doc.toObject(User::class.java)
                user?.apply { 
                    id = doc.id 
                    isBanned = doc.getBoolean("isBanned") ?: false
                }
            } ?: emptyList()
            if (currentTab == 0) filterAndDisplay()
        }

        // Listen to Koshais
        db.collection("koshais").addSnapshotListener { snapshot, _ ->
            if (!isAdded) return@addSnapshotListener
            binding.shimmerLoading.stopShimmer()
            binding.shimmerLoading.visibility = View.GONE

            allKoshais = snapshot?.documents?.mapNotNull { doc ->
                val koshai = doc.toObject(KoshaiProfile::class.java)
                koshai?.apply { 
                    id = doc.id 
                    isBanned = doc.getBoolean("isBanned") ?: false
                }
            } ?: emptyList()
            if (currentTab == 1) filterAndDisplay()
        }
    }

    private fun filterAndDisplay() {
        val query = binding.etSearch.text.toString().lowercase()
        
        val filteredList = if (currentTab == 0) {
            allUsers.filter { 
                it.name.lowercase().contains(query) || 
                it.phone.contains(query) || 
                it.upazila.lowercase().contains(query) 
            }
        } else {
            allKoshais.filter { 
                it.name.lowercase().contains(query) || 
                it.phone.contains(query) || 
                it.district.lowercase().contains(query) ||
                it.upazila.lowercase().contains(query)
            }
        }
        
        adapter.updateList(filteredList)
        
        binding.layoutEmpty.root.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
        if (filteredList.isEmpty()) {
            val title = if (query.isEmpty()) "No Accounts Registered" else "No Matching Results"
            val subtitle = if (query.isEmpty()) "Wait for new users or Koshais to sign up." else "Try searching with a different name or location."
            
            binding.layoutEmpty.tvEmptyTitle.text = title
            binding.layoutEmpty.tvEmptySubtitle.text = subtitle
            binding.layoutEmpty.ivEmptyIcon.setImageResource(R.drawable.ic_empty_search)
        }
    }

    private fun toggleBanStatus(item: Any) {
        val collection = if (item is User) "users" else "koshais"
        val id = if (item is User) item.id else (item as KoshaiProfile).id
        val currentStatus = if (item is User) item.isBanned else (item as KoshaiProfile).isBanned
        
        val newStatus = !currentStatus
        
        // 1. Instant UI update (Optimistic)
        if (item is User) item.isBanned = newStatus
        else if (item is KoshaiProfile) item.isBanned = newStatus
        adapter.notifyDataSetChanged()

        // 2. Database update
        db.collection(collection).document(id)
            .update("isBanned", newStatus)
            .addOnSuccessListener {
                val action = if (newStatus) "Banned" else "Unbanned"
                Toast.makeText(requireContext(), "Account $action", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Revert if failed
                if (item is User) item.isBanned = currentStatus
                else if (item is KoshaiProfile) item.isBanned = currentStatus
                adapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
