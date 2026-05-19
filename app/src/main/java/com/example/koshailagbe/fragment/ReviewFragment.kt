package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.databinding.FragmentReviewBinding
import com.example.koshailagbe.model.Review
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class ReviewFragment : Fragment() {

    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var bookingId: String? = null
    private var koshaiId: String? = null
    private var koshaiName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        bookingId = arguments?.getString("bookingId")
        koshaiId = arguments?.getString("koshaiId")
        koshaiName = arguments?.getString("koshaiName")

        setupUI()
        setupListeners()

        return binding.root
    }

    private fun setupUI() {
        binding.tvKoshaiName.text = "How was your service with $koshaiName?"
        
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            binding.tvRatingText.text = when (rating.toInt()) {
                1 -> "Poor"
                2 -> "Fair"
                3 -> "Good"
                4 -> "Very Good"
                5 -> "Excellent!"
                else -> ""
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        
        binding.btnSubmit.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        val rating = binding.ratingBar.rating
        val comment = binding.etComment.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (koshaiId == null || bookingId == null) {
            showSnackBar("Error: Missing information", isError = true)
            return
        }

        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Submitting..."

        // Fetch actual user name from Firestore to ensure it's correct
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            val userName = doc.getString("name") ?: auth.currentUser?.displayName ?: "User"

            val review = Review(
                userId = userId,
                userName = userName,
                rating = rating,
                comment = comment,
                koshaiId = koshaiId!!
            )

            // 1. Add review to Koshai's reviews collection
            db.collection("koshais").document(koshaiId!!).collection("reviews").add(review)
                .addOnSuccessListener {
                    updateKoshaiRating(rating)
                }
                .addOnFailureListener {
                    showSnackBar("Failed to submit review: ${it.message}", isError = true)
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Submit Review"
                }
        }.addOnFailureListener {
            val userName = auth.currentUser?.displayName ?: "User"
            val review = Review(
                userId = userId,
                userName = userName,
                rating = rating,
                comment = comment,
                koshaiId = koshaiId!!
            )
            db.collection("koshais").document(koshaiId!!).collection("reviews").add(review)
                .addOnSuccessListener { updateKoshaiRating(rating) }
                .addOnFailureListener {
                    showSnackBar("Failed to submit review: ${it.message}", isError = true)
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Submit Review"
                }
        }
    }

    private fun updateKoshaiRating(newRating: Float) {
        val koshaiRef = db.collection("koshais").document(koshaiId!!)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(koshaiRef)
            val currentRating = snapshot.getDouble("rating") ?: 0.0
            val totalRatings = snapshot.getLong("totalRatings") ?: 0
            
            val newTotalRatings = totalRatings + 1
            val newAvgRating = ((currentRating * totalRatings) + newRating) / newTotalRatings
            
            transaction.update(koshaiRef, "rating", newAvgRating)
            transaction.update(koshaiRef, "totalRatings", newTotalRatings)
            
            // 2. Mark booking as reviewed
            val bookingRef = db.collection("bookings").document(bookingId!!)
            transaction.update(bookingRef, "isReviewed", true)
            
            null
        }.addOnSuccessListener {
            showSnackBar("Thank you for your review!")
            findNavController().popBackStack()
        }.addOnFailureListener {
            showSnackBar("Error updating rating: ${it.message}", isError = true)
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.text = "Submit Review"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
