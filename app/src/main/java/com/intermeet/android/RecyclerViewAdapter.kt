
//import androidx.core.content.ContextCompat.startActivity
//import androidx.appcompat.app.AppCompatActivity
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.intermeet.android.DiscoverActivity
import com.intermeet.android.R
import com.intermeet.android.UserData
import com.squareup.picasso.Picasso

class RecyclerViewAdapter(
    private val courseDataArrayList: List<String>,
    private val mcontext: LikesPageFragment,
    private val disccontext: DiscoverActivity
) : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

    //private var OnClickListener: View.OnClickListener
    private var onClickListener: OnClickDetect? = null
    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        // Inflate Layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: RecyclerViewHolder, position: Int) {
        // Set the data to textview and imageview.
        val recyclerData = courseDataArrayList[position]
        //holder.courseTV.text = recyclerData.title

        //here load images to view
        //holder.courseIV.setImageResource(recyclerData.imgid)

        val databaseReference = FirebaseDatabase.getInstance().getReference("users/$recyclerData")
        //val databaseImage = FirebaseDatabase.getInstance().getReference("users/$recyclerData")

        databaseReference.addValueEventListener(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)

                userData?.let { user ->
                    // Setting firstName and calculating age
                    val firstName = user.firstName
                    val birthday = user.birthday
                    val age = calculateAge(birthday)
                    holder.likesText.text = "$firstName, $age"

                    // Setting profile photo if available
                    user.photoDownloadUrls?.firstOrNull()?.let { url ->
                        Picasso.get().load(url).into(holder.likesImage);
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        holder.likesCard.setOnClickListener{
            if(onClickListener != null)
            {
                onClickListener!!.onClickDetect(position, courseDataArrayList[position])
            }
        }
    }

    fun setOnClickListener(OnClickListener : OnClickDetect)
    {
        this.onClickListener = OnClickListener
    }

    public interface OnClickDetect
    {
        fun onClickDetect(position : Int, userID : String)
    }

    override fun getItemCount(): Int {
        // this method returns the size of recyclerview
        return courseDataArrayList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateAge(birthday: String?): Int {
        // Implement logic to calculate age based on birthday
        // Example: Parse birthday string, calculate age based on current date, and return age
        // For brevity, a simplified implementation is shown here:
        // Note: You may need to handle date parsing and calculation more accurately in a real app.

        if (birthday.isNullOrEmpty()) return 0 // Default age if birthday is not provided
        var day: Int? = null
        var month: Int? = null
        var year: Int? = null
        val parts = birthday.split("-")

        if (parts.size == 3) {
            day = parts[0].toIntOrNull()
            month = parts[1].toIntOrNull()
            year = parts[2].toIntOrNull()

            if (day != null && month != null && year != null) {
                println("Day: $day")
                println("Month: $month")
                println("Year: $year")
            }
        }

        val currentYear = java.time.LocalDate.now().year
        val currentMonth = java.time.LocalDate.now().monthValue
        val currentDay = java.time.LocalDate.now().dayOfMonth
        var currentAge = currentYear - year!!
        if(currentMonth < month!!) {
            currentAge -= 1
        }
        else if(currentMonth == month!!) {
            if(currentDay < day!!) {
                currentAge -= 1
            }
        }

        return currentAge
    }

    // View Holder Class to handle Recycler View.
    inner class RecyclerViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val likesCard : CardView = itemView.findViewById(R.id.newCard)
        val likesText: TextView = itemView.findViewById(R.id.idTVCourse)
        val likesImage: ImageView = itemView.findViewById(R.id.likespage_image)
    }
}

