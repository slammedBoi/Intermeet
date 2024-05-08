import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.intermeet.android.Event
import com.intermeet.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.intermeet.android.LockableBottomSheetBehavior
import com.intermeet.android.UserAdapter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.*

class EventsFragment : Fragment(), OnMapReadyCallback, LocationListener {

    private lateinit var eventsTitleTextView: TextView
    private lateinit var eventsMenuBarButton: Button
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private lateinit var eventList: ListView
    private lateinit var searchBar: AutoCompleteTextView
    private lateinit var placesClient: PlacesClient
    private lateinit var mapButton: Button
    private lateinit var myLocation: Button
    private lateinit var debugButton: Button
    private lateinit var autocompleteAdapter: AutocompleteAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var currentCoords: LatLng
    private lateinit var progressBar: ProgressBar
    private lateinit var rectangleBackground: View
    private lateinit var participantText: TextView
    private val REQUEST_LOCATION_PERMISSION = 1001
    private var cameraMovedOnce = false
    private var eventsList: MutableList<Event> = mutableListOf()
    private var userMarker: Marker? = null
    private var permissionCallback: PermissionCallback? = null
    private val geocodeCache = mutableMapOf<String, LatLng>()

    private val eventMarkersMap: MutableMap<String, Marker?> = mutableMapOf()

    interface PermissionCallback {
        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_events, container, false)

        // Set up bottomSheet for events
        val bottomSheet = view.findViewById<View>(R.id.eventSheet)
        rectangleBackground = view.findViewById(R.id.buttonHolder)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            peekHeight = 320
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Check if the new state is expanded or collapsed
                when (newState) {
                    BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_EXPANDED -> {
                        // Hide the buttons when bottom sheet is expanded or dragging
                        mapButton.visibility = View.GONE
                        myLocation.visibility = View.GONE
                        rectangleBackground.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // Show the buttons when bottom sheet is collapsed
                        mapButton.visibility = View.VISIBLE
                        myLocation.visibility = View.VISIBLE
                        rectangleBackground.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        mapButton = view.findViewById(R.id.events_mapIcon)
        mapButton.setOnClickListener {
            toggleMapType()
        }

        myLocation = view.findViewById(R.id.myLocation_button)
        myLocation.setOnClickListener {
            moveToUserLocation()
        }

        // Initialize Places API client
        Places.initialize(requireActivity().applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireActivity())

        // Initialize UI elements
        eventsTitleTextView = view.findViewById(R.id.events_title)
        eventsMenuBarButton = view.findViewById(R.id.events_menuBar)
        mapView = view.findViewById(R.id.mapView)
        progressBar = view.findViewById(R.id.mapProgressBar)
        debugButton = view.findViewById(R.id.events_debug)
        searchBar = view.findViewById(R.id.search_edit_text)
        eventList = view.findViewById(R.id.eventList)

        // Initialize Geocoder
        geocoder = Geocoder(requireContext())

        // Set up autocomplete for search bar
        autocompleteAdapter = AutocompleteAdapter(requireContext())
        searchBar.setAdapter(autocompleteAdapter)
        searchBar.threshold = 1 // Start autocomplete after 1 character

        // Fill up the event bottom sheet with events from the database
        fetchEventsFromDatabase { fetchedEventsList ->
            eventsList = fetchedEventsList
            val eventAdapter = EventSheetAdapter(requireContext(), eventsList)
            eventList.adapter = eventAdapter
        }

        // Fills the database when events based on the currently logged in user's current location
        debugButton.setOnClickListener {
            getUserLocation { userLocation ->
                val addressComponents = userLocation.split(", ")
                val city = addressComponents.getOrNull(1)?.replace(" ", "+") ?: ""
                getEventsByLocation(city)
            }

            fetchEventsFromDatabase { fetchedEventsList ->
                eventsList = fetchedEventsList
                val eventAdapter = EventSheetAdapter(requireContext(), eventsList)
                eventList.adapter = eventAdapter
            }
        }

        eventsMenuBarButton.setOnClickListener {
            showDropdownMenu()
        }

        val behavior = LockableBottomSheetBehavior.from(bottomSheet)
        eventList.setOnScrollChangeListener { _, _, _, _, _ ->
            behavior.isDraggable = !eventList.canScrollVertically(-1) // The bottom sheet is draggable only when the ListView is not scrollable upwards
        }

        // Clicking any event in the bottom sheet will bring up its respective event card
        eventList.setOnItemClickListener { parent, view, position, _ ->
            val event = parent.adapter.getItem(position) as Event
            val eventMarker = eventMarkersMap[event.title]

            eventMarker?.let { marker ->
                // Move the camera to the position of the marker
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))

                // Collapse the bottom sheet after an event is clicked
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        // Initialize and set up the map
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        // Set up text changed listener for autocomplete
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fetchAutocompletePredictions(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun fetchAutocompletePredictions(query: String) {
        // Specify the search bounds
        val bounds = RectangularBounds.newInstance(
            LatLng(33.6717, -118.3436),
            LatLng(34.0194, -118.1553)
        ) // Example bounds, adjust as needed

        // Create a request
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setLocationBias(bounds)
            .build()

        // Fetch the autocomplete predictions
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val predictions = response.autocompletePredictions
                val formattedPredictions = predictions.map { prediction ->
                    // Extract address components from the prediction
                    val addressComponents = prediction.getFullText(null).toString().split(", ")
                    if (addressComponents.size >= 4) {
                        // Construct the address with street, city, state, and zip code
                        "${addressComponents[0]}, ${addressComponents[1]}, ${addressComponents[2]}, ${addressComponents[3]}"
                    } else {
                        // If there are not enough components, use the default prediction text
                        prediction.getFullText(null).toString()
                    }
                }.toTypedArray()

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, formattedPredictions)
                searchBar.setAdapter(adapter)

                // Set up item click listener for autocomplete predictions
                searchBar.setOnItemClickListener { parent, view, position, id ->
                    val selectedPrediction = predictions[position]
                    val placeId = selectedPrediction.placeId
                    // Fetch details for the selected place
                    fetchPlaceDetails(placeId)
                }
            }
            .addOnFailureListener { exception: Exception ->
                Log.e("Autocomplete", "Autocomplete prediction fetch failed: $exception")
            }
    }

    private fun showDropdownMenu() {
        val popupMenu = PopupMenu(requireContext(), eventsMenuBarButton)
        popupMenu.menuInflater.inflate(R.menu.events_dropdown, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_by_name -> {
                    // Sort events by name
                    sortEventsByName(eventsList)
                    true
                }
                R.id.sort_by_distance -> {
                    // Sort events by distance
                    sortEventsByDistance(currentCoords, eventsList)
                    true
                }
                R.id.sort_by_date -> {
                    // Sort events by date
                    sortEventsByDate(eventsList)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun sortEventsByName(eventsList: MutableList<Event>) {
        // Sort events by name
        eventList.adapter = EventSheetAdapter(requireContext(), eventsList.sortedBy { it.title })
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sortEventsByDistance(userLocation: LatLng, eventsList: MutableList<Event>) {
        progressBar.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.IO) {  // It's better to use a structured concurrency approach in production
            val sortedEvents = eventsList.sortedBy { event ->
                val address = event.addressList.joinToString(", ")
                val eventLocation = performGeocoding(address)
                calculateDistance(userLocation, eventLocation)
            }
            withContext(Dispatchers.Main) {
                eventList.adapter = EventSheetAdapter(requireContext(), sortedEvents)
                (eventList.adapter as EventSheetAdapter).notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun sortEventsByDate(eventsList: MutableList<Event>) {
        // Sort events by date
        eventList.adapter = EventSheetAdapter(requireContext(), eventsList.sortedBy { it.startDate })
    }

    private fun fetchPlaceDetails(placeId: String?) {
        if (placeId != null) {
            val placeFields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response: FetchPlaceResponse ->
                    val place = response.place
                    val address = place.address
                    val latLng = place.latLng
                    if (address != null && latLng != null) {
                        // Update the search bar text with the full address
                        searchBar.setText(address)
                        // Add a marker to the map at the selected location
                        googleMap.clear() // Clear previous markers
                        googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
                        // Move the map camera to the selected place's location
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                }
                .addOnFailureListener { exception: Exception ->
                    Log.e("FetchPlaceDetails", "Failed to fetch place details: $exception")
                }
        }
    }

    // When the map is done initializing move the camera to the user's current location
    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        googleMap.setOnMarkerClickListener { marker ->
            // Open the event card when a marker is clicked
            openEventCard(marker)
            true // Return true to consume the event and prevent the default behavior (opening the info window)
        }

        fetchEventsFromDatabase { eventsList ->
            addMarkersToMap(eventsList)
        }

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with location updates
            startLocationUpdates()
        } else {
            // Permission not granted, request permission
            permissionCallback = object : PermissionCallback {
                override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
                    if (requestCode == REQUEST_LOCATION_PERMISSION) {
                        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            // Permission granted, proceed with location updates
                            startLocationUpdates()
                        } else {
                            // Permission denied, handle accordingly
                            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun openEventCard(marker: Marker) {
        Log.d("MarkerClick", "Marker clicked: ${marker.title}")
        val event = marker.tag as? Event
        event?.let {
            // Move the camera to the position of the marker
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))

            // Show the event card for the clicked marker
            showEventCard(event)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showEventCard(event: Event) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.event_details_card)
        val participant1 = dialog.findViewById<ImageView>(R.id.participant1)
        val participant2 = dialog.findViewById<ImageView>(R.id.participant2)
        val participant3 = dialog.findViewById<ImageView>(R.id.participant3)
        val moreParticipantsText = dialog.findViewById<TextView>(R.id.more_participants)

        val eventCardImage = dialog.findViewById<ImageView>(R.id.event_image)
        Glide.with(requireContext())
            .load(event.thumbnail)
            .into(eventCardImage)

        val eventCardTitle = dialog.findViewById<TextView>(R.id.event_title)
        eventCardTitle.text = event.title

        val eventCardDate = dialog.findViewById<TextView>(R.id.event_date)
        eventCardDate.text = event.whenInfo

        val eventCardAddress = dialog.findViewById<TextView>(R.id.event_address)
        eventCardAddress.text = "${event.addressList[0]}, ${event.addressList[1]}"

        val eventCardDescription = dialog.findViewById<TextView>(R.id.event_description)
        eventCardDescription.text = event.description

        val eventCardDistance = dialog.findViewById<TextView>(R.id.event_distance)
        val fullAddress = event.addressList.joinToString(", ")
        val coords = performGeocoding(fullAddress)
        eventCardDistance.text = "${calculateDistance(currentCoords, coords)} mi"

        val eventRef = FirebaseDatabase.getInstance().getReference("events").child(event.id).child("peopleGoing")
        val goingText = dialog.findViewById<TextView>(R.id.going_text)

        val peopleGoingEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val peopleGoing = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                goingText.text = "Going (${peopleGoing.size})"

                fetchUsersGoingToEvent(event.id) { users ->
                    if(users.isNotEmpty()) {
                        participantText.visibility = View.GONE
                        fetchUserDetails(users[0]) { user ->
                            if (user.photoDownloadUrls.isNotEmpty()) {
                                context?.let {
                                    Glide.with(it)
                                        .load(user.photoDownloadUrls.firstOrNull())
                                        .override(100, 100) // Set fixed size
                                        .circleCrop()
                                        .into(participant1)
                                }
                            }
                        }

                        // One participant
                        if(users.size > 1) {
                            fetchUserDetails(users[1]) { user ->
                                if (user.photoDownloadUrls.isNotEmpty()) {
                                    context?.let {
                                        Glide.with(it)
                                            .load(user.photoDownloadUrls.firstOrNull())
                                            .override(100, 100) // Set fixed size
                                            .circleCrop()
                                            .into(participant2)
                                    }
                                }
                            }
                        }

                        // Two participants
                        if(users.size > 2) {
                            fetchUserDetails(users[2]) { user ->
                                if (user.photoDownloadUrls.isNotEmpty()) {
                                    context?.let {
                                        Glide.with(it)
                                            .load(user.photoDownloadUrls.firstOrNull())
                                            .override(100, 100) // Set fixed size
                                            .circleCrop()
                                            .into(participant3)
                                    }
                                }
                            }
                        }

                        // Three or more participants
                        if (users.size > 3) {
                            val additionalCount = users.size - 3
                            moreParticipantsText.text = "+$additionalCount"
                            moreParticipantsText.visibility = View.VISIBLE
                        } else {
                            moreParticipantsText.visibility = View.GONE
                        }
                    }
                    else {
                        participantText.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EventCard", "Failed to listen for changes in people going: ${error.message}")
            }
        }
        eventRef.addValueEventListener(peopleGoingEventListener)

        participantText = dialog.findViewById(R.id.participant_text)
        fetchUsersGoingToEvent(event.id) { users ->
            if(users.isNotEmpty()) {
                participantText.visibility = View.GONE
                fetchUserDetails(users[0]) { user ->
                    if (user.photoDownloadUrls.isNotEmpty()) {
                        context?.let {
                            Glide.with(it)
                                .load(user.photoDownloadUrls.firstOrNull())
                                .override(100, 100) // Set fixed size
                                .circleCrop()
                                .into(participant1)
                        }
                    }
                }

                // One participant
                if(users.size > 1) {
                    fetchUserDetails(users[1]) { user ->
                        if (user.photoDownloadUrls.isNotEmpty()) {
                            context?.let {
                                Glide.with(it)
                                    .load(user.photoDownloadUrls.firstOrNull())
                                    .override(100, 100) // Set fixed size
                                    .circleCrop()
                                    .into(participant2)
                            }
                        }
                    }
                }

                // Two participants
                if(users.size > 2) {
                    fetchUserDetails(users[2]) { user ->
                        if (user.photoDownloadUrls.isNotEmpty()) {
                            context?.let {
                                Glide.with(it)
                                    .load(user.photoDownloadUrls.firstOrNull())
                                    .override(100, 100) // Set fixed size
                                    .circleCrop()
                                    .into(participant3)
                            }
                        }
                    }
                }

                // Three or more participants
                if (users.size > 3) {
                    val additionalCount = users.size - 3
                    moreParticipantsText.text = "+$additionalCount"
                    moreParticipantsText.visibility = View.VISIBLE
                } else {
                    moreParticipantsText.visibility = View.GONE
                }
            }
            else {
                participantText.visibility = View.VISIBLE
            }
        }

        participant1.setOnClickListener {
            fetchUsersGoingToEvent(event.id) { users ->
                val usersDialog = Dialog(requireContext())
                usersDialog.setContentView(R.layout.users_list_dialog)
                val usersListView = usersDialog.findViewById<ListView>(R.id.users_list)
                val adapter = UserAdapter(requireContext(), users)
                usersListView.adapter = adapter
                usersDialog.show()
            }
        }

        participant2.setOnClickListener {
            fetchUsersGoingToEvent(event.id) { users ->
                val usersDialog = Dialog(requireContext())
                usersDialog.setContentView(R.layout.users_list_dialog)
                val usersListView = usersDialog.findViewById<ListView>(R.id.users_list)
                val adapter = UserAdapter(requireContext(), users)
                usersListView.adapter = adapter
                usersDialog.show()
            }
        }

        participant3.setOnClickListener {
            fetchUsersGoingToEvent(event.id) { users ->
                val usersDialog = Dialog(requireContext())
                usersDialog.setContentView(R.layout.users_list_dialog)
                val usersListView = usersDialog.findViewById<ListView>(R.id.users_list)
                val adapter = UserAdapter(requireContext(), users)
                usersListView.adapter = adapter
                usersDialog.show()
            }
        }

        val goingButton = dialog.findViewById<Button>(R.id.going_button)

        val currentUserId = getCurrentUserId()
        eventRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val peopleGoing = snapshot.children.mapNotNull { it.getValue(String::class.java)}
                if(currentUserId in peopleGoing) {
                    goingButton.text = "Already Going"
                    goingButton.isEnabled = false
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        goingButton.setOnClickListener {
            // Add the user's ID to the list of people going
            val currentUserId = getCurrentUserId()
            if (currentUserId != null) {
                addUserIdToEvent(event.id, currentUserId)
            }
        }
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.setOnDismissListener {
            eventRef.removeEventListener(peopleGoingEventListener)
        }

        dialog.show()
    }

    private fun fetchUserDetails(userId: String, callback: (UserAdapter.User) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                val photoDownloadUrls = snapshot.child("photoDownloadUrls").children.mapNotNull { it.getValue(String::class.java) }
                callback(UserAdapter.User(userId, firstName, lastName, photoDownloadUrls))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserAdapter", "Failed to fetch user details: ${error.message}")
            }
        })
    }

    private fun getCurrentUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid
    }

    private fun addUserIdToEvent(eventId: String, userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("events")

        // Reference the specific event by its ID
        val eventRef = databaseReference.child(eventId)

        // Add the user ID to the peopleGoing list in the event object
        eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val selectedEvent = dataSnapshot.getValue(Event::class.java)
                selectedEvent?.let { event ->
                    // Add the user ID to the peopleGoing list if it's not already there
                    if (!event.peopleGoing.contains(userId)) {
                        event.peopleGoing.add(userId)
                        // Update the event object in Firebase
                        eventRef.setValue(event)
                            .addOnSuccessListener {
                                Log.d("AddUserIdToEvent", "User added to event: $eventId")
                            }
                            .addOnFailureListener {
                                Log.e("AddUserIdToEvent", "Failed to add user to event: $eventId")
                            }
                    } else {
                        Log.d("AddUserIdToEvent", "User already exists in event: $eventId")
                    }
                } ?: run {
                    Log.e("AddUserIdToEvent", "Event not found: $eventId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("AddUserIdToEvent", "Database query cancelled: ${databaseError.message}")
            }
        })
    }

    private fun Location.distanceToInMiles(dest: Location): Int {
        val earthRadiusMiles = 3958.8 // Earth radius in miles
        val deltaLatitude = Math.toRadians(dest.latitude - this.latitude)
        val deltaLongitude = Math.toRadians(dest.longitude - this.longitude)
        val a = sin(deltaLatitude / 2) * sin(deltaLatitude / 2) +
                cos(Math.toRadians(this.latitude)) * cos(Math.toRadians(dest.latitude)) *
                sin(deltaLongitude / 2) * sin(deltaLongitude / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceInMiles = earthRadiusMiles * c
        return Math.round(distanceInMiles).toInt()
    }

    private fun calculateDistance(userLocation: LatLng, eventLocation: LatLng): Int {
        val user = Location("")
        user.latitude = userLocation.latitude
        user.longitude = userLocation.longitude

        val event = Location("")
        event.latitude = eventLocation.latitude
        event.longitude = eventLocation.longitude

        return user.distanceToInMiles(event)
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    interval = 5000 // 5 seconds
                    fastestInterval = 1000 // 1 second
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                },
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let { location ->
                            // Handle location update
                            onLocationChanged(location)
                            cameraMovedOnce = true
                            progressBar.visibility = View.GONE  // Hide the ProgressBar when location is found
                        }
                    }
                },
                Looper.getMainLooper() // Looper for handling callbacks on main thread
            )
        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionCallback?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Method to perform geocoding
    private fun performGeocoding(address: String): LatLng {
        geocodeCache[address]?.let {
            return it // Return the cached coordinates
        }

        var coords = LatLng(0.0,0.0)
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val location = addresses[0]
                    val latitude = location.latitude
                    val longitude = location.longitude
                    coords = LatLng(latitude, longitude)
                    geocodeCache[address] = coords
                    Log.d("Geocoding", "Latitude: $latitude, Longitude: $longitude")
                } else {
                    Log.e("Geocoding", "Address not found")
                }
            }
        } catch (e: IOException) {
            Log.e("Geocoding", "Geocoding failed: ${e.message}")
        }
        return coords
    }

    // Method to perform reverse geocoding
    private fun performReverseGeocoding(latLng: LatLng): String {
        var fullAddress = ""
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    fullAddress = address.getAddressLine(0)
                    Log.d("Reverse Geocoding", "Address: $fullAddress")
                } else {
                    Log.e("Reverse Geocoding", "No address found for the given coordinates")
                }
            }
        } catch (e: IOException) {
            Log.e("Reverse Geocoding", "Reverse geocoding failed: ${e.message}")
        }
        return fullAddress
    }

    private fun addMarkersToMap(events: List<Event>) {
        for (event in events) {
            val fullAddress = event.addressList.joinToString(", ")
            val coords = performGeocoding(fullAddress)
            Log.d("addMarkersToMap()", "${event.title} added at ${coords}")

            // Add marker to the map and store the title-marker pair in the map
            val marker = googleMap.addMarker(MarkerOptions().position(coords).title(event.title))
            if (marker != null) {
                marker.tag = event
            } // Set the event object as the tag for the marker
            eventMarkersMap[event.title] = marker
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getEventsByLocation(city: String) {
        val apiKey = resources.getString(R.string.serpapi_key)
        val url = "https://serpapi.com/search.json?engine=google_events&q=Events+in+${city}&hl=en&gl=us&api_key=${apiKey}"
        Log.d("SerpAPI", "Query: $url")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = reader.readText()

                    handleEvents(response)
                } else {
                    Log.e("SerpAPI", "HTTP error: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("SerpApi", "Error: ${e.message}", e)
            }
        }
    }

    private fun handleEvents(response: String) {
        val jsonResponse = JSONObject(response)
        val eventsArray = jsonResponse.getJSONArray("events_results")

        for (i in 0 until eventsArray.length()) {
            val eventObject = eventsArray.getJSONObject(i)
            val title = eventObject.getString("title")
            Log.d("SerpAPI", "Event found: ${title}")
            val startDate = eventObject.getJSONObject("date").getString("start_date")
            val whenInfo = eventObject.getJSONObject("date").getString("when")
            val addressArray = eventObject.getJSONArray("address")
            val addressList = mutableListOf<String>()
            for (j in 0 until addressArray.length()) {
                addressList.add(addressArray.getString(j))
            }
            val link = eventObject.getString("link")
            val description = eventObject.getString("description")
            val thumbnail = eventObject.getString("thumbnail")
            val peopleGoing = mutableListOf<String>()
            val event = Event("", title, startDate, whenInfo, addressList, link, description, thumbnail, peopleGoing)
            uploadEvent(event)
        }
    }

    private fun uploadEvent(event: Event) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("events")

        // Query to check if the event already exists
        databaseReference.orderByChild("title").equalTo(event.title).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Event already exists, do not add it again
                    Log.d("Add Event", "Event already exists in database")
                } else {
                    // Event does not exist, generate random ID and add it
                    val eventId = UUID.randomUUID().toString() // Generate random UUID
                    val eventRef = databaseReference.child(eventId)
                    event.id = eventId // Set the generated ID to the event
                    eventRef.setValue(event)
                        .addOnSuccessListener {
                            Log.d("Add Event", "Event added successfully with ID: $eventId")
                        }
                        .addOnFailureListener {
                            Log.d("Add Event", "Could not add event")
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("Add Event", "Database query cancelled: ${databaseError.message}")
            }
        })
    }

    private fun getUserLocation(callback: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val database = Firebase.database
        val userRef = userId?.let { database.getReference("user_locations").child(it).child("l") }

        userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val locationList: List<*>? = dataSnapshot.value as? List<*>
                if (locationList != null && locationList.size >= 2) {
                    val latitude = (locationList[0] as? Double) ?: return
                    val longitude = (locationList[1] as? Double) ?: return
                    val userLocation = performReverseGeocoding(LatLng(latitude, longitude))
                    callback(userLocation)
                } else {
                    Log.d("Location", "Location data not found or incomplete.")
                    callback("")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Location", "Failed to read location.", databaseError.toException())
                callback("") // Invoke the callback with an empty string in case of failure
            }
        })
    }

    private fun fetchEventsFromDatabase(callback: (MutableList<Event>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("events")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val eventsList = mutableListOf<Event>()
                for (snapshot in dataSnapshot.children) {
                    val event = snapshot.getValue(Event::class.java)
                    event?.let {
                        eventsList.add(it)
                    }
                }
                callback(eventsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FetchEvents", "Failed to fetch events from database: ${databaseError.message}")
                callback(mutableListOf()) // Pass an empty list in case of failure
            }
        })
    }

    override fun onLocationChanged(location: Location) {
        // Check if the Fragment is attached to a context
        if (isAdded) {
            // Fragment is attached, safe to access resources
            Log.d("onLocationChanged", "Current Latitude: ${location.latitude} Current Longitude: ${location.longitude}")
            currentCoords = LatLng(location.latitude, location.longitude)
            val latLng = LatLng(location.latitude, location.longitude)

            // Remove the previous user marker if it exists
            userMarker?.remove()

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.user_icon)

            // Define the desired width and height for the resized image
            val width = 50 // Specify the desired width in pixels
            val height = 50 // Specify the desired height in pixels

            // Resize the image
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

            // Convert the resized bitmap to a BitmapDescriptor
            val customMarkerIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap)

            // Add a new marker for the updated user location with the resized custom icon
            userMarker = googleMap.addMarker(MarkerOptions().position(latLng).title("User").icon(customMarkerIcon))

            if(!cameraMovedOnce) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                progressBar.visibility = View.GONE
            }
        }
    }

    // Method to toggle between map types
    private fun toggleMapType() {
        if (googleMap.mapType == GoogleMap.MAP_TYPE_TERRAIN) {
            // Switch to satellite view
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        } else {
            // Switch to normal view
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    private fun moveToUserLocation() {
        // Check if the last known location is available
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    // Move the camera to the user's current location
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                } ?: run {
                    // Handle the case when the last known location is not available
                    Toast.makeText(
                        requireContext(),
                        "Unable to retrieve current location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun fetchUsersGoingToEvent(eventId: String, callback: (List<String>) -> Unit) {
        val eventRef = FirebaseDatabase.getInstance().getReference("events").child(eventId).child("peopleGoing")
        eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersGoing = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                callback(usersGoing)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchUsersGoingToEvent", "Error fetching users: ${error.message}")
                callback(emptyList()) // Return an empty list in case of error
            }
        })
    }

    companion object {
        fun newInstance(): EventsFragment {
            return EventsFragment()
        }
    }
}