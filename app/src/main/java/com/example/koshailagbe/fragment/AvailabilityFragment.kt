package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentAvailabilityBinding
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AvailabilityFragment : Fragment() {

    private var _binding: FragmentAvailabilityBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAvailabilityBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        loadCurrentSettings()
        setupListeners()

        return binding.root
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupListeners() {
        binding.sliderSurge.addOnChangeListener { _, value, _ ->
            binding.tvSurgeValue.text = getString(R.string.surge_rate_format, value)
        }

        binding.btnSaveAvailability.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadCurrentSettings() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("koshais").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                val status = doc.getString("status") ?: "offline"
                val eidMode = doc.getBoolean("isEidMode") ?: false
                val surgeRate = (doc.get("surgeRate") as? Number)?.toDouble() ?: 1.5

                when (status) {
                    "online" -> binding.statusToggleGroup.check(R.id.btnOnline)
                    "busy" -> binding.statusToggleGroup.check(R.id.btnBusy)
                    else -> binding.statusToggleGroup.check(R.id.btnOffline)
                }

                binding.switchEidMode.isChecked = eidMode
                binding.sliderSurge.value = surgeRate.toFloat()
                binding.tvSurgeValue.text = getString(R.string.surge_rate_format, surgeRate)
            }
    }

    private fun saveSettings() {
        val uid = auth.currentUser?.uid ?: return
        
        val status = when (binding.statusToggleGroup.checkedButtonId) {
            R.id.btnOnline -> "online"
            R.id.btnBusy -> "busy"
            else -> "offline"
        }
        
        val eidMode = binding.switchEidMode.isChecked
        val surgeRate = binding.sliderSurge.value.toDouble()

        val updates = hashMapOf<String, Any>(
            "status" to status,
            "isEidMode" to eidMode,
            "surgeRate" to surgeRate
        )

        db.collection("koshais").document(uid).update(updates)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                showSnackBar(getString(R.string.msg_availability_updated))
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                showSnackBar(getString(R.string.msg_update_failed, it.message), isError = true)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
