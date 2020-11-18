package pl.foxcode.crashalertclient.view

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.navigation_header.*
import pl.foxcode.crashalertclient.AlertDialogMenager
import pl.foxcode.crashalertclient.DatabaseManager
import pl.foxcode.crashalertclient.R
import pl.foxcode.crashalertclient.model.Marker
import kotlin.collections.ArrayList

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    //private lateinit var currentCenter: LatLng
    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val SEARCH_SETTINGS_REQUEST_CODE = 3
    }

    private lateinit var mAuth: FirebaseAuth

    private lateinit var alertDialogMenager : AlertDialogMenager
    private lateinit var databaseManager : DatabaseManager

    private var search_range =5

    private val SharedPrefferencesSearchRange = "search_range"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val sharedPreferences = getSharedPreferences(SharedPrefferencesSearchRange, Context.MODE_PRIVATE)
        search_range = sharedPreferences.getInt(SharedPrefferencesSearchRange, 5)


        //authentication
        mAuth = FirebaseAuth.getInstance()

        //realtime database
        val database = FirebaseDatabase.getInstance()
        databaseManager = DatabaseManager(database)

        alertDialogMenager= AlertDialogMenager(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                lastLocation = locationResult.lastLocation
            }
        }

        createLocationRequest()


        floatingActionButton_open_menu.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
            textView_user_email.text = mAuth.currentUser?.email
            textView_user_ID.text = mAuth.currentUser?.uid

        }


        //navigation drawer onClick actions
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener {
            if(it.itemId == R.id.menu_item_search_settings)
            {
                val intent = Intent(applicationContext, SearchSettings::class.java)
                intent.putExtra("current_search_range",search_range)
                startActivityForResult(intent,SEARCH_SETTINGS_REQUEST_CODE)
            }
            if (it.itemId == R.id.menu_item_change_email) {
                alertDialogMenager.showDialogChangeEmail(mAuth)
            }
            if (it.itemId == R.id.menu_item_change_password) {
                alertDialogMenager.showDialogChangePassword(mAuth)
            }
            if (it.itemId == R.id.menu_item_log_out) {
                mAuth.signOut()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
            }
            false
        }





    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMarkerClickListener(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestFuseLocationPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12.5f))
            }
        }

        getFirebaseData()


    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun createLocationRequest() {

        locationRequest = LocationRequest()
        locationRequest.interval = 5000 // 5sec
        locationRequest.fastestInterval = 3000 //3sec
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(
                        this@MapActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }


    private lateinit var listOfMarkers: ArrayList<Marker>
    fun getFirebaseData(){
        Log.d("markers", "getAllMarkersData: success" )

        val databaseReference = FirebaseDatabase.getInstance().getReference("Data")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listOfMarkers = ArrayList()
                for (i in dataSnapshot.children) {

                    val newMarker = i.getValue(Marker::class.java)
                    if (newMarker != null) {
                        listOfMarkers.add(newMarker)
                    }
                }
                setupAdapter(listOfMarkers)

            }
        })
    }

    fun setupAdapter(listOfMarkers : ArrayList<Marker>){
        map.clear()
        for(i in listOfMarkers){
            val latLng = LatLng(i.latitude.toDouble(),i.longitude.toDouble())
            val markerLocation = Location(lastLocation)
            markerLocation.latitude = i.latitude.toDouble()
            markerLocation.longitude = i.longitude.toDouble()

            if(lastLocation.distanceTo(markerLocation) <= search_range * 1000)

            Marker().placeMarkerOnMap(map, applicationContext,latLng)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        if(requestCode == SEARCH_SETTINGS_REQUEST_CODE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                if(data!=null)
                {
                    search_range = data.extras!!.getInt("search_range")
                    Log.d("range", "onActivityResult: "+ search_range)
                    getFirebaseData()
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    public override fun onResume() {
        super.onResume()

        //start geting location updates
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    private fun requestFuseLocationPermission(permission: String) {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(permission) -> {
                MaterialAlertDialogBuilder(applicationContext)
                    .setTitle(getString(R.string.permission_dialog_title))
                    .setMessage(getString(R.string.permission_dialog_message))
                    .setNeutralButton(R.string.ok, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, i: Int) {
                            dialog?.dismiss()
                        }
                    })
                    .show()
            }
            else -> {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onMarkerClick(p0: com.google.android.gms.maps.model.Marker?): Boolean {
        return false
    }


}