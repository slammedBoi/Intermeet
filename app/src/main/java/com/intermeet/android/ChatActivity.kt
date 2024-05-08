package com.intermeet.android
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.content.Intent
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID


class ChatActivity : AppCompatActivity() {


    private lateinit var receiverUid: String
    private lateinit var chatView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: Button
    private lateinit var profileImage: ImageView
    private lateinit var backButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var userName: TextView
    private lateinit var calenderIcon: ImageView
    private lateinit var calendarMonth: TextView
    private lateinit var calendarView: com.kizitonwose.calendar.view.CalendarView
    private lateinit var hangoutName: EditText
    private lateinit var hangoutTimeBegin: TextView
    private lateinit var hangoutTimeEnd: TextView
    private lateinit var hangoutLocation: EditText
    private lateinit var hangoutDescription: EditText
    private lateinit var saveEventButton: Button
    private lateinit var addHangoutButton: ImageButton
    private lateinit var timePickerSpinner: TimePicker
    private lateinit var hangoutList: RecyclerView
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    var receiverRoom: String? = null
    var senderRoom: String? = null
    companion object {
        private const val TAG = "MyFirebaseMessagingService"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatView = findViewById(R.id.chatView)
        messageBox = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)
        profileImage = findViewById(R.id.profile_image)
        messageList = ArrayList()
        userName = findViewById(R.id.user_name)
        backButton = findViewById(R.id.back_arrow)
        calenderIcon = findViewById(R.id.calendar_icon)
        messageAdapter = MessageAdapter(this, messageList)

        chatView.layoutManager = LinearLayoutManager(this)
        chatView.adapter = messageAdapter

        Log.d(TAG, "ChatActivity: onCreate - Intent action: ${intent.action}")

        receiverUid = intent.getStringExtra("userId") ?: throw IllegalArgumentException("User ID must be provided")
        Log.d(TAG, "ChatActivity started with userId: $receiverUid")


        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        fetchUserName(receiverUid)

        // Add data to recyclerView
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for(postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val messageObject = Message(message, senderUid)

            mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            messageBox.setText("")
        }

        backButton.setOnClickListener {
            onBackPressed()  // Use the system's default back pressed handling
        }

        calenderIcon.setOnClickListener {
            showCalendarDialog()
        }

    }

    private fun fetchUserName(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                val photoDownloadUrls = snapshot.child("photoDownloadUrls").children.mapNotNull { it.getValue(String::class.java) }
                userName.text = "${firstName} ${lastName}"

                Glide.with(this@ChatActivity)
                    .load(photoDownloadUrls[0])
                    .circleCrop()
                    .into(profileImage)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("openFragment", "chat")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        } else {
            super.onBackPressed()  // Follow the normal back behavior
        }
        finish()  // Ensure this activity is finished after handling the back action
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCalendarDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.calendar_view)
        calendarMonth = dialog.findViewById(R.id.calendar_month)
        calendarView = dialog.findViewById(R.id.calendar_view)
        addHangoutButton = dialog.findViewById(R.id.add_hangout_button)
        hangoutList = dialog.findViewById(R.id.hangout_list)
        setupHangoutRecyclerView()

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            val dotView = view.findViewById<View>(R.id.hangoutDot)

            init {
                view.setOnClickListener {
                    if(day.position == DayPosition.MonthDate) {
                        selectDate(day.date)
                    }
                }
            }
        }

        // Individual day cells
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()
                container.day = data
                if(data.position == DayPosition.MonthDate) {
                    container.textView.visibility = View.VISIBLE
                    when(data.date) {
                        today -> {
                            container.dotView.visibility = View.GONE
                        }
                        selectedDate -> {
                            container.textView.setBackgroundResource(R.drawable.hangout_dot)
                            container.dotView.visibility = View.GONE
                        }
                        else -> {
                            container.textView.background = null
                            mDbRef.child("chats").child(senderRoom!!).child("calendar").child(data.date.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val hangout = snapshot.getValue(Hangout::class.java)
                                    if (hangout != null) {
                                        container.dotView.visibility = View.VISIBLE
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle error
                                }
                            })
                        }
                    }
                }
                else {
                    container.textView.visibility = View.GONE
                    container.dotView.visibility = View.GONE
                }
            }
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)  // Adjust as needed
        val endMonth = currentMonth.plusMonths(100)  // Adjust as needed
        val daysOfWeek = daysOfWeek()
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        val titlesContainer = dialog.findViewById<ViewGroup>(R.id.titlesContainer)
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }

        calendarView.monthScrollListener = {
            val titleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
            calendarMonth.text = titleFormatter.format(it.yearMonth)
        }

        addHangoutButton.setOnClickListener {
            showAddHangout()
        }


        dialog.show()
    }

    private fun setupHangoutRecyclerView() {
        hangoutList.layoutManager = LinearLayoutManager(this)
        hangoutList.adapter = HangoutAdapter(ArrayList(), this,
            onDeleteClick = { hangout ->
                // Implement deletion logic here
                deleteHangout(hangout)
            },
            onEditClick = { hangout ->
                // Implement edit logic here
                showEditHangoutDialog(hangout)
            }
        )
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { calendarView.notifyDateChanged(it) }
            calendarView.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    // Update RecycleView with list of Hangouts
    private fun updateAdapterForDate(date: LocalDate) {
        val dateStr = date.toString()
        val hangouts = ArrayList<Hangout>()  // This list will hold new hangouts or be empty.

        mDbRef.child("chats").child(senderRoom!!).child("calendar").child(dateStr)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hangouts.clear()  // Clear previous entries.
                    for (hangoutSnapshot in snapshot.children) {
                        val hangout = hangoutSnapshot.getValue(Hangout::class.java)
                        hangout?.let { hangouts.add(it) }
                    }
                    // Update the RecyclerView with new data or clear it if no data.
                    updateRecyclerView(hangouts)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Failed to fetch hangouts: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateRecyclerView(hangouts: List<Hangout>) {
        if (hangoutList.adapter == null) {
            hangoutList.adapter = HangoutAdapter(hangouts, this,
                onDeleteClick = { hangout ->
                    deleteHangout(hangout)
                },
                onEditClick = { hangout ->
                    showEditHangoutDialog(hangout)
                }
            )
        } else {
            (hangoutList.adapter as HangoutAdapter).hangouts = hangouts
            (hangoutList.adapter as HangoutAdapter).notifyDataSetChanged()
        }
    }

    private fun showEditHangoutDialog(hangout: Hangout) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.add_hangout_view)
        val hangoutName = dialog.findViewById<EditText>(R.id.hangout_name_text)
        val hangoutTimeBegin = dialog.findViewById<TextView>(R.id.hangout_time_begin_text)
        val hangoutTimeEnd = dialog.findViewById<TextView>(R.id.hangout_time_end_text)
        val hangoutLocation = dialog.findViewById<EditText>(R.id.hangout_location_text)
        val hangoutDescription = dialog.findViewById<EditText>(R.id.hangout_description_text)
        val saveEventButton = dialog.findViewById<Button>(R.id.save_event_button)

        hangoutName.setText(hangout.name)
        hangoutTimeBegin.text = hangout.beginTime
        hangoutTimeEnd.text = hangout.endTime
        hangoutLocation.setText(hangout.location)
        hangoutDescription.setText(hangout.description)

        saveEventButton.setOnClickListener {
            val updatedHangout = hangout.copy(
                name = hangoutName.text.toString(),
                beginTime = hangoutTimeBegin.text.toString(),
                endTime = hangoutTimeEnd.text.toString(),
                location = hangoutLocation.text.toString(),
                description = hangoutDescription.text.toString()
            )
            updateHangout(hangout.id!!, updatedHangout)
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun updateHangout(hangoutId: String, newHangout: Hangout) {
        selectedDate?.let { date ->
            val dateStr = date.toString()
            mDbRef.child("chats").child(senderRoom!!).child("calendar").child(dateStr).child(hangoutId)
                .setValue(newHangout)
                .addOnSuccessListener {
                    Toast.makeText(this, "Hangout updated successfully", Toast.LENGTH_SHORT).show()
                    updateAdapterForDate(date)  // Refresh the list
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update hangout", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun deleteHangout(hangout: Hangout) {
        selectedDate?.let { date ->
            mDbRef.child("chats").child(senderRoom!!).child("calendar").child(date.toString()).child(hangout.id!!)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Hangout deleted successfully", Toast.LENGTH_SHORT).show()
                    updateAdapterForDate(date)  // Refresh the list
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete hangout", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun buttonSaveEvent(hangout: Hangout) {
        val hangoutName = hangout.name
        mDbRef.child("chats").child(senderRoom!!).child("calendar").child(selectedDate.toString()).child(hangout.id!!)
            .setValue(hangout).addOnSuccessListener {
                mDbRef.child("chats").child(receiverRoom!!).child("calendar").child(selectedDate.toString()).child(hangout.id!!)
                    .setValue(hangout)
            }
        selectedDate?.let { updateAdapterForDate(it) }
    }

    private fun showAddHangout() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.add_hangout_view)
        hangoutName = dialog.findViewById(R.id.hangout_name_text)
        hangoutTimeBegin = dialog.findViewById(R.id.hangout_time_begin_text)
        hangoutTimeEnd = dialog.findViewById(R.id.hangout_time_end_text)
        hangoutLocation = dialog.findViewById(R.id.hangout_location_text)
        hangoutDescription = dialog.findViewById(R.id.hangout_description_text)
        saveEventButton = dialog.findViewById(R.id.save_event_button)

        hangoutTimeBegin.setOnClickListener {
            showTimePickerDialog("begin")
        }

        hangoutTimeEnd.setOnClickListener {
            showTimePickerDialog("end")
        }


        saveEventButton.setOnClickListener {
            val uniqueID = UUID.randomUUID().toString()
            val hangout = Hangout(
                id = uniqueID,
                name = hangoutName.getText().toString(),
                beginTime = hangoutTimeBegin.getText().toString(),
                endTime = hangoutTimeEnd.getText().toString(),
                location = hangoutLocation.getText().toString(),
                description = hangoutDescription.getText().toString()
            )

            buttonSaveEvent(hangout)

            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePickerDialog(beginOrEnd: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.time_spinner_view)

        timePickerSpinner = dialog.findViewById(R.id.time_spinner)
        timePickerSpinner.setIs24HourView(false)
        timePickerSpinner.setOnTimeChangedListener { view, hourOfDay, minute ->
            var selectedTime = String.format("%02d:%02d", hourOfDay, minute)
            if(beginOrEnd == "begin") {
                if(hourOfDay < 12) {
                    hangoutTimeBegin.text = "${selectedTime} AM"
                }
                else {
                    val modifiedHour = hourOfDay - 12
                    selectedTime = String.format("%02d:%02d", modifiedHour, minute)
                    hangoutTimeBegin.text = "${selectedTime} PM"
                }
            }
            else {
                if(hourOfDay < 12) {
                    hangoutTimeEnd.text = "- ${selectedTime} AM"
                }
                else {
                    val modifiedHour = hourOfDay - 12
                    selectedTime = String.format("%02d:%02d", modifiedHour, minute)
                    hangoutTimeEnd.text = "- ${selectedTime} PM"
                }
            }
        }
        dialog.show()
    }

    private fun handleBackButton() {
        if (isTaskRoot) {
            // If this is the root activity, start MainActivity with the right fragment
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openFragment", "chat")
            startActivity(intent)
        }
        finish()  // Finish this activity
    }

    override fun onResume() {
        super.onResume()
        AppState.isChatActivityVisible = true  // Set when the activity comes into view
    }

    override fun onPause() {
        super.onPause()
        AppState.isChatActivityVisible = false  // Reset when the activity goes out of view
    }

}
