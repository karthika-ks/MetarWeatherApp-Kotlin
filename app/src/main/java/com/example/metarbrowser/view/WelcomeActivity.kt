package com.example.metarbrowser.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.metarbrowser.R

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
    }

    fun onSearchFromGermanStation(view: View) {
        val intent = Intent(this, SearchFromListActivity::class.java)
        startActivity(intent)
    }

    fun onSearchByCode(view: View) {
        val intent = Intent(this, SearchByCodeActivity::class.java)
        startActivity(intent)
    }
}
