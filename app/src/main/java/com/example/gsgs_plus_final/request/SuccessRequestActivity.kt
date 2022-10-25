package com.example.gsgs_plus_final.request

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.chat.ChatUser
import com.example.gsgs_plus_final.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SuccessRequestActivity : AppCompatActivity() {
    private lateinit var mDbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth



    override fun onBackPressed() {
        // super.onBackPressed()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_request)

        val db = Firebase.firestore
        auth = Firebase.auth


        val start = intent.getStringExtra("start")
        val end = intent.getStringExtra("end")
        val time = intent.getStringExtra("name")
        val id = intent.getStringExtra("id")

        val txt_start = findViewById<TextView>(R.id.start)
        val txt_end = findViewById<TextView>(R.id.end)
        val btn_back = findViewById<Button>(R.id.btn_back)

        txt_start.setText(start.toString())
        txt_end.setText(end.toString())
        //1이 받은 사람, 2가 피커
//        val docRef3 = db.collection("pick_up_request")
//        val pick_data = docRef3.document(time.toString())
//        pick_data.get().addOnSuccessListener { document ->
//            if (document != null) {
//                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
//                document["uid"].toString()
//                document["uid_2"].toString()
////                addUserToDatabase(time.toString(),document["uid"].toString())
////                addUserToDatabase(time.toString(),document["uid_2"].toString())
//
//            } else {
//                Log.d(TAG, "No such document")
//            }
//        }
//            .addOnFailureListener { exception ->
//                Log.d(TAG, "get failed with ", exception)
//            }

        btn_back.setOnClickListener {
            Toast.makeText(this, "홈 화면으로 이동합니다!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()




        }


    }

//    private fun addUserToDatabase(time:String,  uid: String) {
//        mDbRef = FirebaseDatabase.getInstance().getReference()
//        mDbRef.child("ChatList").child(time).setValue(ChatUser(uid))
//
//    }
}