package com.apps.ideaink

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class CreateActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etContent: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        etTitle = findViewById(R.id.etTitle)
        etAuthor = findViewById(R.id.etAuthor)
        etContent = findViewById(R.id.etContent)

    }
}