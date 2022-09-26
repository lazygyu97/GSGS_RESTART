package com.example.gsgs_plus_final.login

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.gsgs_plus_final.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.util.concurrent.TimeUnit

class IdFindFinishActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_find_finish)

        val getName = intent.getStringExtra("name").toString()
        val getFindId = intent.getStringExtra("find_id").toString()

        Log.d("getName",getName)
        Log.d("getFindId",getFindId)
        findViewById<TextView>(R.id.id_find_finish_name).text = getName
        findViewById<TextView>(R.id.id_find_finish_id).text = getFindId



        val btn_back = findViewById<Button>(R.id.btn_back)
        btn_back.setOnClickListener {
            //뒤로가기
            var intent1 = Intent(this, LoginActivity::class.java)
            startActivity(intent1)
        }
    }
}








