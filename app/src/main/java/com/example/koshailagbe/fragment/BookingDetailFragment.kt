package com.example.koshailagbe.fragment

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentBookingDetailBinding
import com.example.koshailagbe.model.Booking
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BookingDetailFragment : Fragment() {

    private var _binding: FragmentBookingDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private var bookingId: String? = null
    private var booking: Booking? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingDetailBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        bookingId = arguments?.getString("bookingId")
        if (bookingId == null) {
            showSnackBar("Error: Booking ID missing", isError = true)
            findNavController().popBackStack()
        } else {
            fetchBookingDetails()
        }

        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        
        binding.btnChat.setOnClickListener {
            booking?.let {
                val bundle = Bundle().apply {
                    putString("bookingId", it.id)
                    putString("receiverId", it.userId)
                }
                findNavController().navigate(R.id.action_bookingDetailFragment_to_chatFragment, bundle)
            }
        }

        binding.btnGetDirections.setOnClickListener {
            booking?.let {
                val gmmIntentUri = Uri.parse("google.navigation:q=${it.lat},${it.lng}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
        }

        binding.btnMainAction.setOnClickListener {
            booking?.let { handleMainAction(it) }
        }

        binding.btnCancel.setOnClickListener {
            booking?.let { updateStatus("cancelled") }
        }
    }

    private fun fetchBookingDetails() {
        bookingId?.let { id ->
            db.collection("bookings").document(id).addSnapshotListener { snapshot, e ->
                if (!isAdded) return@addSnapshotListener
                if (e != null) {
                    showSnackBar("Failed to load details: ${e.message}", isError = true)
                    return@addSnapshotListener
                }

                booking = snapshot?.toObject(Booking::class.java)?.apply { this.id = snapshot.id }
                booking?.let { updateUI(it) }
            }
        }
    }

    private fun updateUI(booking: Booking) {
        binding.tvBookingId.text = "Booking ID: #${booking.id.takeLast(8).uppercase()}"
        binding.tvStatus.text = booking.status.uppercase()
        
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        binding.tvDate.text = "${sdf.format(booking.date.toDate())} (${booking.slot})"
        
        binding.tvAddress.text = booking.address
        binding.tvCustomerName.text = "Customer (UID: ${booking.userId.takeLast(6)})"
        
        // Animal breakdown
        binding.animalContainer.removeAllViews()
        booking.animalTypes.forEach { (type, count) ->
            if (count > 0) {
                val itemView = TextView(requireContext()).apply {
                    text = "${count}x ${type.replaceFirstChar { it.uppercase() }}"
                    textSize = 16f
                    setPadding(0, 4, 0, 4)
                }
                binding.animalContainer.addView(itemView)
            }
        }

        // Price details
        val total = booking.rateBreakdown["total"] ?: 0.0
        binding.tvTotalAmount.text = "৳$total"
        binding.tvDepositPaid.text = "৳${booking.depositPaid}"
        binding.tvBalance.text = "৳${total - booking.depositPaid}"

        // Action button logic
        updateActionButton(booking.status)
    }

    private fun updateActionButton(status: String) {
        when (status) {
            "pending" -> {
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FBC02D"))
                binding.btnMainAction.text = "Accept Booking"
                binding.btnMainAction.setBackgroundColor(Color.parseColor("#2E7D32"))
                binding.btnCancel.text = "Decline Booking"
                binding.btnMainAction.visibility = View.VISIBLE
            }
            "confirmed" -> {
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32"))
                binding.btnMainAction.text = "Start Journey (En Route)"
                binding.btnMainAction.setBackgroundColor(Color.parseColor("#1565C0"))
                binding.btnCancel.visibility = View.VISIBLE
                binding.btnMainAction.visibility = View.VISIBLE
            }
            "en_route" -> {
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))
                binding.btnMainAction.text = "I have Arrived"
                binding.btnMainAction.setBackgroundColor(Color.parseColor("#1565C0"))
                binding.btnCancel.visibility = View.GONE
                binding.btnMainAction.visibility = View.VISIBLE
            }
            "arrived" -> {
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2"))
                binding.btnMainAction.text = "Mark as Completed"
                binding.btnMainAction.setBackgroundColor(Color.parseColor("#2E7D32"))
                binding.btnCancel.visibility = View.GONE
                binding.btnMainAction.visibility = View.VISIBLE
            }
            "completed" -> {
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#455A64"))
                binding.btnMainAction.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
            }
            "cancelled" -> {
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                binding.btnMainAction.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
            }
        }
    }

    private fun handleMainAction(booking: Booking) {
        val nextStatus = when (booking.status) {
            "pending" -> "confirmed"
            "confirmed" -> "en_route"
            "en_route" -> "arrived"
            "arrived" -> "completed"
            else -> return
        }
        updateStatus(nextStatus)
    }

    private fun updateStatus(newStatus: String) {
        val currentBooking = booking ?: return
        val id = bookingId ?: return

        val bookingRef = db.collection("bookings").document(id)
        val koshaiRef = db.collection("koshais").document(currentBooking.koshaiId)

        if (newStatus == "completed") {
            db.runTransaction { transaction ->
                val bookingSnap = transaction.get(bookingRef)
                val alreadyCounted = bookingSnap.getBoolean("statsCounted") ?: false

                transaction.update(bookingRef, "status", newStatus)

                if (!alreadyCounted) {
                    val totalEarnings = (currentBooking.rateBreakdown["total"] ?: 0.0).toLong()
                    transaction.update(koshaiRef, "totalJobs", com.google.firebase.firestore.FieldValue.increment(1))
                    transaction.update(koshaiRef, "earnings", com.google.firebase.firestore.FieldValue.increment(totalEarnings))
                    transaction.update(bookingRef, "statsCounted", true)
                }
            }.addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                showSnackBar("Booking marked as completed")
            }.addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                showSnackBar("Update failed: ${it.message}", isError = true)
            }
        } else {
            db.collection("bookings").document(id).update("status", newStatus)
                .addOnSuccessListener {
                    if (!isAdded) return@addOnSuccessListener
                    showSnackBar("Booking updated to $newStatus")
                }
                .addOnFailureListener {
                    if (!isAdded) return@addOnFailureListener
                    showSnackBar("Update failed: ${it.message}", isError = true)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
