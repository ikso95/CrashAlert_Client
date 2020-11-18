package pl.foxcode.crashalertclient

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import pl.foxcode.crashalertclient.model.Marker
import kotlin.collections.ArrayList

class DatabaseManager(database: FirebaseDatabase) {

    val databaseReference = database.getReference("Data")
    val databaseReference2 = database.getReference("Users")
    private lateinit var listOfMarkers: ArrayList<Marker>

    /*fun addMarkerInput(databaseMarkerInput : DatabaseMarker){
        databaseReference.child("${Date().time}").setValue(databaseMarkerInput)
    }

    fun addUserInput(databaseUserInput : DatabaseUser, uid : String){
        databaseReference2.child(uid).setValue(databaseUserInput)
    }*/

    fun getAllMarkersData() {
        listOfMarkers = ArrayList()

        GlobalScope.launch {
            funA()
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("markers", "getAllMarkersData: error")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (i in dataSnapshot.children) {
                    val newMarker = i.getValue(Marker::class.java)
                    if (newMarker != null) {
                        Log.d("markers", "getAllMarkersData: success" + i)
                        listOfMarkers.add(newMarker)
                    }
                }
            }
        })
    }




    suspend fun funA() : ArrayList<Marker>? = withContext(Dispatchers.Default){
        listOfMarkers = ArrayList()
        try {
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d("markers", "getAllMarkersData: error")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (i in dataSnapshot.children) {
                        val newMarker = i.getValue(Marker::class.java)
                        if (newMarker != null) {
                            Log.d("markers", "getAllMarkersData: success" + i)
                            listOfMarkers.add(newMarker)
                        }
                    }
                }
            })
            listOfMarkers
        }catch (e : Exception){
          null
        }
    }


}