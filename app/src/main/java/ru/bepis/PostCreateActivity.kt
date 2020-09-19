package ru.bepis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class PostCreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_create)
    }

    fun onPostCreateClicked(view: View) {
        val intent = Intent(this, FeedActivity::class.java)
        startActivity(intent)
    }
}