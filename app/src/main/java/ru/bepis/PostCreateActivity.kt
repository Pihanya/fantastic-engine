package ru.bepis

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_post_create.*
import ru.bepis.model.SubjectType


class PostCreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_create)

        postEditText.requestFocus()

        moodLayout.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Выберите настроение")
            builder.setItems(R.array.emotions) { dialog, which ->
                moodText.text = resources.getStringArray(R.array.emotions)[which]
            }
            builder.show()
        }

        topicLayout.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Выберите тематику")
            builder.setItems(SubjectType.values().map{it.title}.toTypedArray()) { dialog, which ->
                topicText.text = SubjectType.values()[which].title
            }
            builder.show()
        }
    }

    fun onPostCreateClicked(view: View) {
        val intent = Intent(this, FeedActivity::class.java)
        startActivity(intent)
    }
}