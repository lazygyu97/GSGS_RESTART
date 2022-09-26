package com.example.gsgs_plus_final.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.gsgs_plus_final.R

class JoinFinshActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_finsh)

        val user_name = intent.getStringExtra("user_name")
        val join_finish_name = findViewById<TextView>(R.id.join_finish_name)

        join_finish_name.text = user_name

        val back_btn = findViewById<Button>(R.id.btn_back)
        back_btn.setOnClickListener {

            var intent_1 = Intent(this, LoginActivity::class.java)
            startActivity(intent_1)
        }
    }
}