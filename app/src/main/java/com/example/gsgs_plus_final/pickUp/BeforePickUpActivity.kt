
package com.example.gsgs_plus_final.pickUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.gsgs_plus_final.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private lateinit var auth: FirebaseAuth

class BeforePickUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_pick_up)

        val btn_finish = findViewById<Button>(R.id.btn_finish)
        val data = intent.getStringExtra("Data")

        btn_finish.setOnClickListener {

            val intent = Intent(this, DoingPickUpActivity::class.java)
            intent.putExtra("Data",data)
            startActivity(intent)
        }
    }
}