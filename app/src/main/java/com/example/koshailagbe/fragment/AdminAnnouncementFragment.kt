package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentAdminAnnouncementBinding
import com.example.koshailagbe.model.Announcement
import com.google.firebase.firestore.FirebaseFirestore

class AdminAnnouncementFragment : Fragment() {

    private var _binding: FragmentAdminAnnouncementBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAnnouncementBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnSend.setOnClickListener { sendAnnouncement() }

        return binding.root
    }

    private fun sendAnnouncement() {
        val title = binding.etTitle.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()
        
        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val target = when (binding.chipGroupTarget.checkedChipId) {
            R.id.chipUsers -> "users"
            R.id.chipKoshais -> "koshais"
            else -> "all"
        }

        binding.btnSend.isEnabled = false
        binding.btnSend.text = "Sending..."

        val announcement = Announcement(
            title = title,
            message = message,
            target = target
        )

        db.collection("announcements").add(announcement)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Announcement broadcasted successfully!", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                binding.btnSend.isEnabled = true
                binding.btnSend.text = "Send Announcement"
                Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
