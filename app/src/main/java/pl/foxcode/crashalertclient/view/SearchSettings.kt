package pl.foxcode.crashalertclient.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.activity_search_settings.*
import pl.foxcode.crashalertclient.R

class SearchSettings : AppCompatActivity() {

    private val SharedPrefferencesSearchRange = "search_range"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_settings)

        var current_search_range = intent.extras?.getInt("current_search_range")

        if(current_search_range!=null)
        {
            slider.value = current_search_range.toFloat()
        }

        textView_search_range.text =  getString(R.string.search_range) + slider.value.toString() + "km"

        slider.addOnChangeListener { slider, value, fromUser ->
            textView_search_range.text =  getString(R.string.search_range) + value + "km"
            current_search_range = slider.value.toInt()
        }

        button_search_range_confirm.setOnClickListener {

                val sharedPreferences = getSharedPreferences(SharedPrefferencesSearchRange, Context.MODE_PRIVATE)
                var editor = sharedPreferences.edit()
                editor.putInt(SharedPrefferencesSearchRange, current_search_range!!)
                editor.commit()


                val intent = Intent(applicationContext, MapActivity::class.java)
                intent.putExtra("search_range", current_search_range!!)
                setResult(Activity.RESULT_OK, intent)
                finish()



        }


    }




}