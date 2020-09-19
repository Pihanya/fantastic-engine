package ru.bepis

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class FeedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
    }

    fun onMapButtonClicked(view: View) {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }
}