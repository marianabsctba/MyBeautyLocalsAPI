package br.infnet.marianabs.mylocals.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.net.Uri
import br.infnet.marianabs.mylocals.R
import br.infnet.marianabs.mylocals.databinding.ActivityPlaceDetailsBinding


class PlaceDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        val bundle: Bundle = intent.extras!!
        val placeId = bundle.getString("placeId")
        val photoReference = bundle.getString("photoReference")
        val placeName = bundle.getString("placeName")
        val openStatus = bundle.getBoolean("openStatus")
        val rateNumber = bundle.getDouble("rateNumber")
        val currentLat = bundle.getDouble("currentLat")
        val currentLng = bundle.getDouble("currentLng")
        val destinationLat = bundle.getDouble("destinationLat")
        val destinationLng = bundle.getDouble("destinationLng")

        val photoUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        photoUrl.append("?maxwidth=400")
        photoUrl.append("&photo_reference=$photoReference")
        photoUrl.append("&key=${getString(R.string.browser_key)}")

        Glide.with(this).load(photoUrl.toString()).into(binding.thumbnailImage)

        binding.placeName.text = placeName
        if (openStatus == true) {
            binding.openStatus.text = "Open Now"
            binding.openStatusIcon.setColorFilter(ContextCompat.getColor(applicationContext, R.color.green))
        } else {
            binding.openStatus.text = "Closed"
            binding.openStatusIcon.setColorFilter(ContextCompat.getColor(applicationContext, R.color.red_500))
        }

        binding.rateNumber.text = "$rateNumber / 5.0"
        startHalalVotesSnapshot(placeId)

        binding.voteHalalButton.setOnClickListener { userVotePlaceAsHalal(placeId, placeName) }
        binding.navigationButton.setOnClickListener { openNavigationOnMaps(currentLat, currentLng, destinationLat, destinationLng) }
    }

    private fun openNavigationOnMaps(currentLat: Double, currentLng: Double, destinationLat: Double, destinationLng: Double) {
        val uri = "http://maps.google.com/maps?f=d&hl=en&saddr=${currentLat},${currentLng}&daddr=${destinationLat},${destinationLng}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(Intent.createChooser(intent, "Select an application"))
    }


    private fun startHalalVotesSnapshot(placeId: String?) {
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        db.collection("halalPlaces")
            .document(placeId!!)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("SNAPSHOT_ERROR", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("CURRENT_DATA", "${snapshot.data}")
                    val votersId = snapshot.data!!["votersId"] as ArrayList<String>
                    val halalNumber = votersId.size
                    binding.halalNumber.text = "$halalNumber Halal Votes"

                    if (userId in votersId) {
                        binding.voteHalalButton.text = "Halal Voted!"
                        binding.voteHalalButton.setTextColor(ContextCompat.getColor(applicationContext, R.color.turquoise))
                    }
                } else {
                    Log.d("CURRENT_DATA_EMPTY", "null")
                }
            }
    }

    private fun userVotePlaceAsHalal(placeId: String?, placeName: String?) {
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        var currentVotersId: ArrayList<String> = ArrayList()

        db.collection("halalPlaces")
            .document(placeId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    currentVotersId = document.data!!["votersId"] as ArrayList<String>
                }

                if (userId !in currentVotersId) {
                    currentVotersId.add(userId!!)

                    val data = hashMapOf(
                        "placeId" to placeId,
                        "placeName" to placeName,
                        "votersId" to currentVotersId
                    )

                    db.collection("halalPlaces")
                        .document(placeId)
                        .set(data)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "You voted halal restaurant!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, it.toString(), Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, it.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}