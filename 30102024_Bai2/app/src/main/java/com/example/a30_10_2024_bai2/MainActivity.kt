package com.example.a30_10_2024_bai2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val students = listOf(
            Student("Pham Hoang Minh Chau", "20225697"),
            Student("Pham Hoang Minh Vy", "20225698"),
            Student("Pham Minh Chau", "20225699"),
            Student("Pham Hoang Minh Chau", "2025697"),
            Student("Pham Hoang Minh Vy", "20227698"),
            Student("Pham Minh Chauuu", "20225669"),
            Student("Pham Hoang Min Chau", "40225697"),
            Student("Pham Hoang Min Vy", "20525698"),
            Student("Pham Minh Cha", "20225689"),

        )

        val listView = findViewById<ListView>(R.id.listView)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, students.map { "${it.name} - ${it.mssv}" })
        listView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()
                if (keyword.length > 2) {
                    val filteredList = students.filter {
                        it.name.contains(keyword, ignoreCase = true) || it.mssv.contains(keyword)
                    }
                    adapter.clear()
                    adapter.addAll(filteredList.map { "${it.name} - ${it.mssv}" })
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.clear()
                    adapter.addAll(students.map { "${it.name} - ${it.mssv}" })
                    adapter.notifyDataSetChanged()
                }
            }
        })

    }
}