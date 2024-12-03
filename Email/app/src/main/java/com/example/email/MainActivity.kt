package com.example.email

import android.os.Bundle
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Cấu hình Edge-to-Edge nếu cần
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Danh sách các tin nhắn mẫu
        val messages = listOf(
            Message("OpenAI", "Explore AI-powered solutions", "10:15 AM", true),
            Message("Alice Pham", "Let's connect for the upcoming project", "9:45 AM", false),
            Message("FreeCodeCamp", "New course: JavaScript Basics", "8:30 AM", true),
            Message("OpenAI", "Join the latest AI webinar", "10:15 AM", true),
            Message("Alice Pham", "Are you available for a meeting?", "9:45 AM", false),
            Message("FreeCodeCamp", "Enroll in Python 101", "8:30 AM", true),
            Message("OpenAI", "Updates on AI advancements", "10:15 AM", true),
            Message("Alice Pham", "Share your thoughts on our project", "9:45 AM", false),
            Message("FreeCodeCamp", "Get started with web development", "8:30 AM", true),

            // Thêm nhiều tin nhắn nếu cần
        )

        // Tìm GridView và gán adapter
        val gridView: GridView = findViewById(R.id.gridView)
        val adapter = MessageAdapter(this, messages)
        gridView.adapter = adapter
    }
}
