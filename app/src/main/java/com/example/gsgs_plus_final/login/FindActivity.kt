package com.example.gsgs_plus_final.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.gsgs_plus_final.R
import androidx.fragment.app.Fragment
import com.example.gsgs_plus_final.main.HomeFragment

class FindActivity : AppCompatActivity() {

    private val frame: FrameLayout by lazy {
        findViewById(R.id.frame)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find)

        supportFragmentManager.beginTransaction().add(frame.id, IdFindFragment()).commit()

        val id_btn = findViewById<Button>(R.id.id_button)
        val pw_btn = findViewById<Button>(R.id.pw_button)
        val back_btn= findViewById<Button>(R.id.btn_back)

        id_btn.setOnClickListener {
            id_btn.setBackgroundResource(R.drawable.button_shape_2)
            pw_btn.setBackgroundResource(R.drawable.button_shape)
            supportFragmentManager.beginTransaction().replace(R.id.frame,IdFindFragment()).commit()
        }
        pw_btn.setOnClickListener {
            id_btn.setBackgroundResource(R.drawable.button_shape)
            pw_btn.setBackgroundResource(R.drawable.button_shape_2)
            supportFragmentManager.beginTransaction().replace(R.id.frame,PwFindFragment()).commit()

        }
        back_btn.setOnClickListener {
            var intent_1 = Intent(this, LoginActivity::class.java)
            startActivity(intent_1)
        }
    }
}