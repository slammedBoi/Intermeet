import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intermeet.android.R

// MyAdapter.kt
class AdapterTest(private val items: List<String>) : RecyclerView.Adapter<AdapterTest.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.test_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind data to views here
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views here
        private val titleTextView: TextView = itemView.findViewById(R.id.testingView)
        private val expandedContent: LinearLayout = itemView.findViewById(R.id.expandedContent)

        init {
            // Setup click listener to expand/collapse card
            itemView.setOnClickListener {
                expandedContent.visibility = if (expandedContent.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }

        fun bind(item: String) {
            // Bind data to views
            titleTextView.text = item
        }
    }
}
