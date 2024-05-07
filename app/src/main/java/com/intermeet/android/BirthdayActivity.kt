package com.intermeet.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
//import kotlinx.android.synthetic.main.activity_main.*
import com.intermeet.android.helperFunc.DatePickerHelper
import com.intermeet.android.helperFunc.getUserDataRepository
import java.util.*

class BirthdayActivity : AppCompatActivity() {
    lateinit var datePicker: DatePickerHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birthday)

        datePicker = DatePickerHelper(this, true)


        ButtonFunc()
    }

    private fun showDatePickerDialog(): String {
        val cal = Calendar.getInstance()
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)
        datePicker.showDialog(d, m, y, object : DatePickerHelper.Callback {
            override fun onDateSelected(dayofMonth: Int, month: Int, year: Int) {
                val dayStr = if (dayofMonth < 10) "0${dayofMonth}" else "${dayofMonth}"
                val mon = month + 1
                val monthStr = if (mon < 10) "0${mon}" else "${mon}"
                val tvDate: TextView = findViewById(R.id.tvDate)
                val date = "${dayStr}-${monthStr}-${year}"
                tvDate.text = date

                // Retrieve userDataRepository
                val userDataRepository = getUserDataRepository()
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.birthday = date
            }
        })
        val birthdayDate: TextView = findViewById(R.id.tvDate)
        return birthdayDate.text.toString()
    }

    private fun ButtonFunc() {
        var birthday = ""
        val selectDate: Button = findViewById(R.id.btSelectDate)
        selectDate.setOnClickListener {
            birthday = showDatePickerDialog()

        }

        val nextButton: Button = findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }
    }
}