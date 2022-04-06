package br.infnet.marianabs.mylocals.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import br.infnet.marianabs.mylocals.HomeActivity
import br.infnet.marianabs.mylocals.databinding.ActivityEditProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        retrieveAndDisplayPhoto()
        user.displayName.let { binding.fullnameField.setText(it) }

        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { photoUri ->
                binding.loadingSpinner.visibility = View.VISIBLE
                uploadImage(photoUri)
            }
        )

        binding.changePhotoButton.setOnClickListener { getImage.launch("image/*") }
        binding.saveButton.setOnClickListener { updateDisplayName() }
    }

    private fun updateDisplayName() {
        val fullname = binding.fullnameField.text.toString()
        val profileUpdates = UserProfileChangeRequest
            .Builder()
            .setDisplayName(fullname)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
    }

    private fun uploadImage(photoUri: Uri) {
        val dateFormatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val now = Date()
        val userId = auth.currentUser?.uid
        val filename = "${userId}_${dateFormatter.format(now)}"
        val fileLocation = "profile/$filename"

        storageReference = FirebaseStorage.getInstance().getReference(fileLocation)
        storageReference.putFile(photoUri)
            .addOnSuccessListener {
                updateUserPhotoUri()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Upload Failed!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserPhotoUri() {
        storageReference.downloadUrl
            .addOnSuccessListener { photoUri ->
                val profileUpdates = UserProfileChangeRequest
                    .Builder()
                    .setPhotoUri(photoUri)
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "User photo updated!", Toast.LENGTH_SHORT).show()
                        user.reload()
                        binding.loadingSpinner.visibility = View.GONE
                        retrieveAndDisplayPhoto()
                    }
                    .addOnFailureListener {
                        Toast.makeText(applicationContext, "Failed!", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun retrieveAndDisplayPhoto() {
        val photoUri = user.photoUrl

        if (photoUri != null) {
            Glide.with(this).load(photoUri.toString()).into(binding.profileImage)
        }
    }
}