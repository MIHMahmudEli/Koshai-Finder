package com.example.koshailagbe.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentEditProfileBinding
import com.example.koshailagbe.utils.SharedPrefsHelper
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    
    private var imageUri: Uri? = null
    private var currentPhotoUrl: String? = null
    private var userRole: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.ivProfile.setImageURI(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        userRole = SharedPrefsHelper.getUserRole(requireContext())

        setupUI()
        loadCurrentData()
        setupListeners()

        return binding.root
    }

    private fun setupUI() {
        val isKoshai = userRole == SharedPrefsHelper.ROLE_KOSHAI
        binding.tilBio.visibility = if (isKoshai) View.VISIBLE else View.GONE
        binding.llKoshaiSettings.visibility = if (isKoshai) View.VISIBLE else View.GONE
    }

    private fun loadCurrentData() {
        val uid = auth.currentUser?.uid ?: return
        val collection = if (userRole == SharedPrefsHelper.ROLE_KOSHAI) "koshais" else "users"

        binding.progressBar.visibility = View.VISIBLE
        db.collection(collection).document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                binding.progressBar.visibility = View.GONE
                
                if (doc.exists()) {
                    binding.etName.setText(doc.getString("name"))
                    binding.etPhone.setText(doc.getString("phone"))
                    binding.etDistrict.setText(doc.getString("district"))
                    binding.etUpazila.setText(doc.getString("upazila"))
                    
                    if (userRole == SharedPrefsHelper.ROLE_KOSHAI) {
                        binding.etBio.setText(doc.getString("bio"))
                        binding.switchEidMode.isChecked = doc.getBoolean("isEidMode") ?: false
                    }
                    
                    currentPhotoUrl = doc.getString("photoUrl")
                    if (!currentPhotoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(currentPhotoUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(binding.ivProfile)
                    }
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                binding.progressBar.visibility = View.GONE
                showSnackBar(getString(R.string.error_load_failed, it.message), isError = true)
            }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        
        binding.btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.btnSave.setOnClickListener { validateAndSave() }
        binding.btnSaveChanges.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val district = binding.etDistrict.text.toString().trim()
        val upazila = binding.etUpazila.text.toString().trim()
        val bio = if (userRole == SharedPrefsHelper.ROLE_KOSHAI) binding.etBio.text.toString().trim() else null
        val isEidMode = if (userRole == SharedPrefsHelper.ROLE_KOSHAI) binding.switchEidMode.isChecked else null

        if (name.isEmpty() || district.isEmpty() || upazila.isEmpty()) {
            showSnackBar(getString(R.string.error_empty_fields), isError = true)
            return
        }

        if (phone.isNotEmpty() && !isValidPhone(phone)) {
            showSnackBar(getString(R.string.error_invalid_phone), isError = true)
            return
        }

        setLoadingState(true)

        if (imageUri != null) {
            uploadImageAndSave(name, phone, district, upazila, bio, isEidMode)
        } else {
            updateFirestore(name, phone, district, upazila, bio, isEidMode, currentPhotoUrl)
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length >= 11 && android.util.Patterns.PHONE.matcher(phone).matches()
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (!isAdded) return
        binding.btnSave.isEnabled = !isLoading
        binding.btnSaveChanges.isEnabled = !isLoading
        binding.btnSave.alpha = if (isLoading) 0.5f else 1.0f
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun uploadImageAndSave(name: String, phone: String, district: String, upazila: String, bio: String?, isEidMode: Boolean?) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_photos/$uid.jpg")

        imageUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateFirestore(name, phone, district, upazila, bio, isEidMode, downloadUri.toString())
                    }.addOnFailureListener {
                        if (isAdded) {
                            showSnackBar(getString(R.string.error_download_url, it.message), isError = true)
                            setLoadingState(false)
                        }
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        showSnackBar(getString(R.string.error_upload_failed, it.message), isError = true)
                        setLoadingState(false)
                    }
                }
        } ?: run {
            // Should not happen if imageUri is checked before calling, but for safety:
            updateFirestore(name, phone, district, upazila, bio, isEidMode, currentPhotoUrl)
        }
    }

    private fun updateFirestore(name: String, phone: String, district: String, upazila: String, bio: String?, isEidMode: Boolean?, photoUrl: String?) {
        val uid = auth.currentUser?.uid ?: return
        val collection = if (userRole == SharedPrefsHelper.ROLE_KOSHAI) "koshais" else "users"

        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "district" to district,
            "upazila" to upazila
        )
        photoUrl?.let { updates["photoUrl"] = it }
        bio?.let { updates["bio"] = it }
        isEidMode?.let { updates["isEidMode"] = it }

        db.collection(collection).document(uid).update(updates)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                showSnackBar(getString(R.string.msg_profile_updated))
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                showSnackBar(getString(R.string.error_update_failed, it.message), isError = true)
                setLoadingState(false)
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
