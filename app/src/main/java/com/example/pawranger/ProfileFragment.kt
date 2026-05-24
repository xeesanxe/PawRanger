package com.example.pawranger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawranger.utils.SessionManager
import com.google.android.material.switchmaterial.SwitchMaterial

class ProfileFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmailHeader: TextView
    private lateinit var tvNameDetail: TextView
    private lateinit var tvEmailDetail: TextView
    private lateinit var tvPhoneDetail: TextView

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
                ivProfile.setImageURI(it)
                sessionManager.saveProfileImage(it.toString())
                Toast.makeText(context, "Foto profil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivProfile = view.findViewById(R.id.iv_profile_picture)
        tvName = view.findViewById(R.id.tv_profile_name)
        tvEmailHeader = view.findViewById(R.id.tv_profile_email)
        tvNameDetail = view.findViewById(R.id.tv_detail_name_value)
        tvEmailDetail = view.findViewById(R.id.tv_detail_email_value)
        tvPhoneDetail = view.findViewById(R.id.tv_detail_phone_value)

        updateUI()

        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        ivProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pickImageLauncher.launch(intent)
        }

        // Edit Name
        val nameEditClick = View.OnClickListener { showEditDialog("Nama", sessionManager.getUserName() ?: "") { sessionManager.saveUserName(it) } }
        tvName.setOnClickListener(nameEditClick)
        tvNameDetail.setOnClickListener(nameEditClick)

        // Edit Email
        val emailEditClick = View.OnClickListener { showEditDialog("Email", sessionManager.getUserEmail() ?: "") { sessionManager.saveUserEmail(it) } }
        tvEmailHeader.setOnClickListener(emailEditClick)
        tvEmailDetail.setOnClickListener(emailEditClick)

        // Edit Phone
        tvPhoneDetail.setOnClickListener { showEditDialog("No. Handphone", sessionManager.getUserPhone() ?: "") { sessionManager.saveUserPhone(it) } }

        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switch_dark_mode)
        switchDarkMode.isChecked = sessionManager.isDarkMode()
        
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.setDarkMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            activity?.recreate()
        }

        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            sessionManager.logout()
            findNavController().navigate(R.id.splashFragment)
        }
    }

    private fun updateUI() {
        val name = sessionManager.getUserName()
        val email = sessionManager.getUserEmail()
        val phone = sessionManager.getUserPhone()

        tvName.text = name
        tvNameDetail.text = name
        tvEmailHeader.text = email
        tvEmailDetail.text = email
        tvPhoneDetail.text = phone

        sessionManager.getProfileImage()?.let {
            try {
                ivProfile.setImageURI(Uri.parse(it))
            } catch (e: Exception) {
                ivProfile.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun showEditDialog(title: String, currentValue: String, onSave: (String) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ubah $title")

        val input = EditText(requireContext())
        input.setText(currentValue)
        builder.setView(input)

        builder.setPositiveButton("Simpan") { _, _ ->
            val newValue = input.text.toString()
            if (newValue.isNotEmpty()) {
                onSave(newValue)
                updateUI()
                Toast.makeText(context, "$title diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }
}
