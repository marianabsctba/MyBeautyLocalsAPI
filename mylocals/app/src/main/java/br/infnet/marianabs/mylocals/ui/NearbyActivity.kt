package br.infnet.marianabs.mylocals.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.infnet.marianabs.mylocals.R
import br.infnet.marianabs.mylocals.databinding.ActivityNearbyBinding
import br.infnet.marianabs.mylocals.model.MyPlaces
import br.infnet.marianabs.mylocals.model.Results
import br.infnet.marianabs.mylocals.remote.IGoogleAPIService
import br.infnet.marianabs.mylocals.util.Common
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class NearbyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNearbyBinding
    private lateinit var nearbyList: ArrayList<Results>
    private lateinit var mService: IGoogleAPIService
    private lateinit var nearbyRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mService = Common.googleApiService

        val bundle: Bundle = intent.extras!!
        val latitude = bundle.getDouble("latitude", 0.0)
        val longitude = bundle.getDouble("longitude", 0.0)
        val typePlace = bundle.getString("typePlace")

        binding.backButton.setOnClickListener { finish() }
        binding.headerTitle.text = "Nearby ${typePlace!!.capitalize()}s"

        nearbyRecyclerView = findViewById(R.id.nearby_recycler_view)
        nearbyRecyclerView.layoutManager = LinearLayoutManager(this)
        nearbyRecyclerView.setHasFixedSize(true)
        nearbyList = arrayListOf<Results>()
        getNearByPlaceData(latitude, longitude, typePlace)
    }

    private fun getNearByPlaceData(latitude: Double, longitude: Double, typePlace: String) {

        val url = getUrl(latitude,longitude, typePlace)
        Log.d("URL_DEBUG", url)

        mService.getNearbyPlaces(url)
            .enqueue(object : Callback<MyPlaces> {
                override fun onResponse(
                    call: Call<MyPlaces>,
                    response: Response<MyPlaces>
                ) {
                    if (response.isSuccessful) {
                        val results = response.body()!!.results!!

                        for (i in results.indices) {
                            val nearbyPlaces = results[i]
                            nearbyList.add(nearbyPlaces)
                        }

                        val adapter = NearbyAdapter(nearbyList)
                        nearbyRecyclerView.adapter = adapter
                        adapter.setOnItemClickListener(object: NearbyAdapter.onItemClickListener {
                            override fun onItemClick(position: Int) {
                                val intent = Intent(this@NearbyActivity, PlaceDetailsActivity::class.java)
                                intent.putExtra("placeId", results[position].place_id)
                                intent.putExtra("photoReference",
                                    results[position].photos?.get(0)?.photo_reference
                                )
                                intent.putExtra("placeName", results[position].name)
                                intent.putExtra("rateNumber", results[position].rating)
                                intent.putExtra("openStatus", results[position].opening_hours?.open_now)
                                intent.putExtra("currentLat", latitude)
                                intent.putExtra("currentLng", longitude)
                                intent.putExtra("destinationLat", results[position].geometry?.location?.lat)
                                intent.putExtra("destinationLng", results[position].geometry?.location?.lng)

                                startActivity(intent)
                            }
                        })
                    }
                }

                override fun onFailure(call: Call<MyPlaces>, t: Throwable) {
                    Toast.makeText(baseContext, "" + t.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=10000") //10 km
        googlePlaceUrl.append("&keyword=$typePlace")
        googlePlaceUrl.append("&key=${getString(R.string.browser_key)}")
        return  googlePlaceUrl.toString()
    }
}