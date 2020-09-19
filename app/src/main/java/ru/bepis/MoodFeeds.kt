package ru.bepis

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.mood_activity.*

class MoodFeeds : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mood_activity)

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        toolbar.title = Store.mood

        when(Store.mood) {
            "it" -> {
                textView1.text = "${textView1.text} * хорошнее настроение"
                textView2.text = "${textView2.text} * хорошнее настроение"
            }
            "winter" -> {
                textView1.text = "${textView1.text} * грустное настроение"
                textView2.text = "${textView2.text} * грустное настроение"
            }
        }
    }
}
