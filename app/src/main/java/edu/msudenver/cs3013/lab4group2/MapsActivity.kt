package edu.msudenver.cs3013.lab4group2

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.msudenver.cs3013.lab4group2.databinding.ActivityMapsBinding
import androidx.fragment.app.activityViewModels


// testing if removed



class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var marker: Marker? = null
    private val locationViewModel: LocationViewModel by activityViewModels()

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMapsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getLastLocation()
                } else {
                    // if should show rationale, show it
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        showPermissionRationale {
                            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                        }
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        //fused location last location with addOnFailureListener and addOnCanceledListener listeners added
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    updateMapLocation(currentLocation)
                    addMarkerAtLocation(currentLocation, "Current Location")
                    //zoom in
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
                }
            }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap.apply {
            setOnMapClickListener { latLng ->
                addOrMoveSelectedPositionMarker(latLng)
            }
        }

        when {
            hasLocationPermission() -> getLocation()
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }

            else -> requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
        //Add a marker in Sydney and move the camera
        // val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


    private fun getLocation() {
        // Log.d("MapsActivity", "getLocation() called.")
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                //   val userLocation = LatLng(location.latitude, location.longitude)
                //   updateMapLocation(userLocation)
                //   addMarkerAtLocation(userLocation, "My Location")

                val Colorado = LatLng(38.0, -105.7)
                updateMapLocation(Colorado)
                addMarkerAtLocation(Colorado, "Some Nerd")
            }
        }

    }

    //zoom factor 2- 21
    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 9f))
    }

    private fun addMarkerAtLocation(
        location: LatLng, title: String, markerIcon: BitmapDescriptor? = null
    ) = mMap.addMarker(MarkerOptions().title(title).position(location).apply {
        markerIcon?.let { icon(markerIcon) }
    }
    )

    private fun getBitmapDescriptorFromVector(
        @DrawableRes
        vectorDrawableResourceId: Int
    ): BitmapDescriptor? {
        val bitmap = ContextCompat.getDrawable(
            requireContext(),
            vectorDrawableResourceId
        )?.let { vectorDrawable ->
            vectorDrawable.setBounds(
                0, 0,
                vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight
            )
            val drawableWithTint = DrawableCompat
                .wrap(vectorDrawable)
            DrawableCompat.setTint(
                drawableWithTint,
                Color.MAGENTA
            )
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawableWithTint.draw(canvas)
            bitmap
        } ?: return null
        return BitmapDescriptorFactory.fromBitmap(bitmap)
            .also { bitmap?.recycle() }
    }

    //this will move the marker to a spot you click on the map and then press the button
    private fun addOrMoveSelectedPositionMarker(latLng: LatLng) {
        view?.findViewById<Button>(R.id.button1)?.setOnClickListener {
            if (marker == null) {
                marker = addMarkerAtLocation(latLng, "Vehicle Location",
                    getBitmapDescriptorFromVector(R.drawable.car_location)
                )
            } else {
                marker?.apply { position = latLng }
            }
        }
        locationViewModel.updateParkingLocation(latLng.toString())
    }


    private fun hasLocationPermission() =
//check if ACCESS_FINE_LOCATION permission is granted
        ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED


    private fun showPermissionRationale(
        positiveAction: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Location permission")
            .setMessage("We need your permission to find your current position")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                positiveAction()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }
}
