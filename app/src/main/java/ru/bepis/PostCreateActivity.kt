package ru.bepis

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_post_create.*


class PostCreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_create)

        moodLayout.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Выберите настроение")
            builder.setItems(R.array.emotions) { _, _ -> }
            builder.show()
        }
    }

    fun onPostCreateClicked(view: View) {
        val intent = Intent(this, FeedActivity::class.java)
        startActivity(intent)
    }
}