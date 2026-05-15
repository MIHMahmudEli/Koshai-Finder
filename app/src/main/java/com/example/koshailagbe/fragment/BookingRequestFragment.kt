package com.example.koshailagbe.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentBookingRequestBinding
import com.example.koshailagbe.model.Booking
import com.example.koshailagbe.model.KoshaiProfile
import com.example.koshailagbe.utils.showSnackBar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BookingRequestFragment : Fragment() {

    private var _binding: FragmentBookingRequestBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private var koshaiId: String? = null
    private var koshaiProfile: KoshaiProfile? = null
    
    private var selectedDate: Date? = null
    private var cowCount = 0
    private var goatCount = 0
    
    private val slots = listOf("06:00-08:00", "08:00-10:00", "10:00-12:00", "13:00-15:00", "15:00-17:00")
    private var selectedSlot: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingRequestBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        koshaiId = arguments?.getString("koshaiId")
        
        loadKoshaiData()
        setupListeners()
        
        // Initial slot state
        updateSlotChips(emptyList())

        return binding.root
    }

    private fun loadKoshaiData() {
        val id = koshaiId ?: return
        db.collection("koshais").document(id).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                koshaiProfile = doc.toObject(KoshaiProfile::class.java)?.apply { this.id = doc.id }
                binding.tvKoshaiName.text = koshaiProfile?.name
                
                com.bumptech.glide.Glide.with(this)
                    .load(koshaiProfile?.photoUrl)
                    .placeholder(R.drawable.bg_dashboard_header)
                    .into(binding.ivKoshai)
            }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        
        binding.btnAddCow.setOnClickListener { cowCount++; updateUI() }
        binding.btnMinusCow.setOnClickListener { if (cowCount > 0) cowCount--; updateUI() }
        
        binding.btnAddGoat.setOnClickListener { goatCount++; updateUI() }
        binding.btnMinusGoat.setOnClickListener { if (goatCount > 0) goatCount--; updateUI() }
        
        binding.btnSubmitRequest.setOnClickListener { submitBooking() }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Service Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
            
        picker.addOnPositiveButtonClickListener { selection ->
            if (selection == null) return@addOnPositiveButtonClickListener
            selectedDate = Date(selection)
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.btnSelectDate.text = sdf.format(selectedDate!!)
            
            // Fetch availability
            checkSlotAvailability(selectedDate!!)
        }
        picker.show(childFragmentManager, "DATE_PICKER")
    }

    private fun checkSlotAvailability(date: Date) {
        val id = koshaiId ?: return
        val calendar = Calendar.getInstance()
        calendar.time = date
        val selectedDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val selectedYear = calendar.get(Calendar.YEAR)

        binding.tvSlotHint.text = "Checking availability..."

        db.collection("bookings")
            .whereEqualTo("koshaiId", id)
            .get()
            .addOnSuccessListener { snapshots ->
                if (!isAdded) return@addOnSuccessListener
                
                val existingBookings = snapshots.toObjects(Booking::class.java).filter { booking ->
                    val bCal = Calendar.getInstance()
                    bCal.time = booking.date.toDate()
                    bCal.get(Calendar.DAY_OF_YEAR) == selectedDayOfYear && 
                    bCal.get(Calendar.YEAR) == selectedYear
                }
                
                updateSlotChips(existingBookings)
            }
            .addOnFailureListener {
                if (isAdded) {
                    showSnackBar("Failed to load slots: ${it.message}")
                    updateSlotChips(emptyList())
                }
            }
    }

    private fun updateSlotChips(existingBookings: List<Booking>) {
        binding.slotGroup.removeAllViews()
        
        if (selectedDate == null) {
            binding.tvSlotHint.text = "Please select a date first"
            binding.tvSlotHint.setTextColor(Color.parseColor("#757575"))
        } else {
            binding.tvSlotHint.text = "Select a time window"
            binding.tvSlotHint.setTextColor(Color.parseColor("#1565C0"))
        }
        
        slots.forEach { slotTime ->
            val chip = com.google.android.material.chip.Chip(requireContext(), null, com.google.android.material.R.style.Widget_Material3_Chip_Filter)
            chip.id = View.generateViewId()
            chip.text = slotTime
            chip.isCheckable = true
            chip.isClickable = true
            chip.setPadding(12, 12, 12, 12)
            
            // Restore selection if it was already selected
            if (selectedSlot == slotTime) {
                chip.isChecked = true
            }

            if (selectedDate == null) {
                chip.isEnabled = false
                chip.alpha = 0.3f
            } else {
                val isConfirmed = existingBookings.any { it.slot == slotTime && it.status == "confirmed" }
                if (isConfirmed) {
                    chip.isEnabled = false
                    chip.alpha = 0.5f
                    chip.text = "$slotTime (Taken)"
                }
            }
            
            // Using setOnClickListener is more reliable for immediate feedback
            chip.setOnClickListener {
                if (chip.isEnabled) {
                    selectedSlot = slotTime
                    binding.tvSlotHint.text = "Selected Slot: $slotTime"
                    binding.tvSlotHint.setTextColor(Color.parseColor("#2E7D32"))
                    
                    // Manually ensure only this chip is checked in the group
                    binding.slotGroup.clearCheck()
                    chip.isChecked = true
                }
            }
            
            binding.slotGroup.addView(chip)
        }
    }

    private fun updateUI() {
        binding.tvCowCount.text = cowCount.toString()
        binding.tvGoatCount.text = goatCount.toString()
        
        val koshai = koshaiProfile ?: return
        val total = (cowCount * koshai.ratePerCow + goatCount * koshai.ratePerGoat).toInt()
        
        binding.tvPriceDetails.text = "${cowCount}x Cow (৳${(cowCount * koshai.ratePerCow).toInt()}), ${goatCount}x Goat (৳${(goatCount * koshai.ratePerGoat).toInt()})"
        binding.tvTotalPrice.text = "৳$total"
    }

    private fun submitBooking() {
        val userId = auth.currentUser?.uid ?: return
        val id = koshaiId ?: return
        val date = selectedDate ?: run { showSnackBar("Please select a date"); return }
        val slot = selectedSlot ?: run { showSnackBar("Please select a time slot"); return }
        val location = binding.etLocation.text.toString()
        
        if (cowCount == 0 && goatCount == 0) {
            showSnackBar("Please select at least one animal")
            return
        }
        if (location.isEmpty()) {
            showSnackBar("Please enter service address")
            return
        }

        val koshai = koshaiProfile ?: return
        val total = (cowCount * koshai.ratePerCow + goatCount * koshai.ratePerGoat)

        val booking = Booking(
            userId = userId,
            koshaiId = id,
            status = "pending",
            date = com.google.firebase.Timestamp(date),
            slot = slot,
            address = location,
            animalTypes = mapOf("cow" to cowCount, "goat" to goatCount),
            rateBreakdown = mapOf("total" to total, "surgeMultiplier" to 1.0),
            userName = auth.currentUser?.displayName ?: "User",
            koshaiName = koshai.name,
            createdAt = com.google.firebase.Timestamp.now()
        )

        db.collection("bookings").add(booking)
            .addOnSuccessListener {
                showSnackBar("Booking confirmed! Koshai will respond shortly.")
                findNavController().popBackStack(R.id.userHomeFragment, false)
            }
            .addOnFailureListener {
                showSnackBar("Failed to send request: ${it.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
