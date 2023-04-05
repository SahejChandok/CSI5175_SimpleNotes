package com.example.notesapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    lateinit var appDatabase: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appDatabase = AppDatabase.getDatabase(this)
        val addButton = findViewById<FloatingActionButton>(R.id.add_notes)
        addButton.setOnClickListener {
            val intent = Intent(this, CreateNoteActivity::class.java)
            startActivity(intent)
        }
    }
}