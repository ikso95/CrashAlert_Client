package pl.foxcode.crashalertclient.model

import android.content.Context
import android.graphics.BitmapFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pl.foxcode.crashalertclient.R

class Marker (
    val longitude : String = "",
    val latitude : String = "",
    val userID : String = "",
    val time : String = ""
){
    fun placeMarkerOnMap(map: GoogleMap, context : Context , location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
            .draggable(true)
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_crash_marker2)
            )

        )
        map.addMarker(markerOptions)
    }
}