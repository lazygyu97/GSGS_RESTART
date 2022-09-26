package com.example.gsgs_plus_final.pickUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.gsgs_plus_final.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DoingPickUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doing_pick_up)


        val btn_finish = findViewById<Button>(R.id.btn_finish)
        val db = Firebase.firestore
        val docRef = db.collection("pick_up_request")
        val docRef2 = db.collection("pickers")
        val data = intent.getStringExtra("Data")
       // Log.d("testset",data.toString())
        auth = Firebase.auth


        btn_finish.setOnClickListener {

            docRef.document(data.toString()).get().addOnSuccessListener {
                    task -> if(task.data!!.get("uid_2").toString() == auth.currentUser!!.uid){
                docRef.document(data.toString()).update("pick_up_check_flag","2")
              }
            }

            val intent = Intent(this, FinishPickUpActivity::class.java)
            startActivity(intent)
        }
    }
}