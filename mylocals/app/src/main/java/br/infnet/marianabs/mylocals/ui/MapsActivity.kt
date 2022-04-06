package br.infnet.marianabs.mylocals.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
//import android.location.LocationRequest
import android.os.Build
import br.infnet.marianabs.mylocals.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.infnet.marianabs.mylocals.databinding.ActivityMapsBinding
import br.infnet.marianabs.mylocals.model.MyPlaces
import br.infnet.marianabs.mylocals.remote.IGoogleAPIService
import br.infnet.marianabs.mylocals.util.Common

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
//import com.google.android.gms.location.LocationRequest

import com.google.android.material.bottomnavigation.BottomNavigationView
import io.grpc.InternalChannelz.id
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    private lateinit var mLastLocation: Location
    private var mMarker: Marker? = null

    //Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val MY_PERMISSION_CODE: Int = 1000
    }

    private lateinit var mService: IGoogleAPIService
    internal lateinit var currentPlaces: MyPlaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //inisialisasi Service
        mService = Common.googleApiService

        //Request runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallBack()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                Looper.myLooper()?.let {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                        it)
                }

            } else {
                buildLocationRequest()
                buildLocationCallBack()
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                Looper.myLooper()?.let {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                        it)
                }
            }

            findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
                .setOnItemSelectedListener { item ->
                    Log.d("itemId here", item.itemId.toString())
                    when (item.itemId) {
                        R.id.action_market -> nearByPlace("market")
                        R.id.action_restaurant -> nearByPlace("restaurant")
                        R.id.action_hotel -> nearByPlace("hotel")
                        R.id.action_mall -> nearByPlace("mall")
                        R.id.action_tour -> nearByPlace("tourism") }
                    true
                }

        }

    }

    private fun nearByPlace(typePlace:String) {

        mMap.clear()
        val url = getUrl(latitude, longitude, typePlace)
        Log.d("URL_PLACE", url)

        mService.getNearbyPlaces(url)
            .enqueue(object : Callback<MyPlaces> {
                override fun onResponse(
                    call: Call<MyPlaces>,
                    response: Response<MyPlaces>
                ) {
                    currentPlaces = response.body()!!

                    if (response.isSuccessful) {
                        Log.d("RESPONSE", response.body()!!.results.toString())

                        for (i in response.body()!!.results!!.indices) {
                            val markerOptions = MarkerOptions()
                            val googlePlaces = response.body()!!.results!![i]
                            val lat = googlePlaces.geometry!!.location!!.lat
                            val lng = googlePlaces.geometry!!.location!!.lng
                            val placeName = googlePlaces.name
                            val latLng = LatLng(lat, lng)

                            markerOptions.position(latLng)
                            markerOptions.title(placeName)
                            when (typePlace) {
                                "market" -> markerOptions.icon(bitMapFromVector(R.drawable.ic_baseline_shopping_cart_24))
                                "restaurant" -> markerOptions.icon(bitMapFromVector(R.drawable.ic_baseline_restaurant_24))
                                "hotel" -> markerOptions.icon(bitMapFromVector(R.drawable.ic_baseline_hotel_24))
                                "mall" -> markerOptions.icon(bitMapFromVector(R.drawable.ic_baseline_local_mall_24))
                                "tourism" -> markerOptions.icon(bitMapFromVector(R.drawable.ic_baseline_tour_24))
                                else -> markerOptions.icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_BLUE
                                    )
                                )
                            }

                            markerOptions.snippet(i.toString())

                            mMap.addMarker(markerOptions)
                        }

                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLng(
                                LatLng(
                                    latitude,
                                    longitude
                                )
                            )
                        )
                        mMap.animateCamera((CameraUpdateFactory.zoomTo(12f)))
                    }
                }

                override fun onFailure(call: Call<MyPlaces>, t: Throwable) {
                    Toast.makeText(baseContext, "" + t.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {

        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=10000") //10 km
        googlePlaceUrl.append("&keyword=$typePlace")
        googlePlaceUrl.append("&key=${getString(R.string.browser_key)}")

        return  googlePlaceUrl.toString()

    }

    private fun bitMapFromVector(vectorResID:Int):BitmapDescriptor {
        val vectorDrawable=ContextCompat.getDrawable(applicationContext, vectorResID)
        vectorDrawable!!.setBounds(0,0,vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight)
        val bitmap=Bitmap.createBitmap(vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                mLastLocation = p0!!.locations[p0.locations.size-1] //Get Last Location

                if(mMarker != null){
                    mMarker!!.remove()
                }

                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude

                val latLng = LatLng(latitude,longitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("Your Position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                mMarker = mMap.addMarker(markerOptions)

                //Move Camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
            }


        }

    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission() : Boolean{
        Log.d("Permission Activity", "Maps Activity")
        return if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            false
        } else
            true
    }

    //Override OnRequestPermissionResult
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            MY_PERMISSION_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        if(checkLocationPermission()){
                            mMap.isMyLocationEnabled = true
                        }
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Init Google Play Services
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
            }
        }
        else
            mMap.isMyLocationEnabled = true

        //Enable Zoom control
        mMap.uiSettings.isZoomGesturesEnabled=true
    }
}