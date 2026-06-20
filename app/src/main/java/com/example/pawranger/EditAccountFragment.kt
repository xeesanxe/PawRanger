package com.example.pawranger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.utils.SessionManager

class EditAccountFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var ivProfile: ImageView
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                selectedImageUri = it
                ivProfile.setImageURI(it)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_edit_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivProfile = view.findViewById(R.id.iv_edit_profile_picture)
        etName = view.findViewById(R.id.et_edit_name)
        etPhone = view.findViewById(R.id.et_edit_phone)

        // Load data
        etName.setText(sessionManager.getUserName())
        etPhone.setText(sessionManager.getUserPhone())
        sessionManager.getProfileImage()?.let {
            ivProfile.setImageURI(Uri.parse(it))
        }

        view.findViewById<View>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(R.id.btn_change_photo).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pickImageLauncher.launch(intent)
        }

        view.findViewById<View>(R.id.btn_save_changes).setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        sessionManager.saveUserName(name)
        sessionManager.saveUserPhone(phone)
        selectedImageUri?.let {
            sessionManager.saveProfileImage(it.toString())
        }

        Toast.makeText(requireContext(), "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}
