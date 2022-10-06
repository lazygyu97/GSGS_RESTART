package com.example.gsgs_plus_final.using

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.main.MainActivity
import com.example.gsgs_plus_final.main.UseListFragment_1
import com.example.gsgs_plus_final.request.SuccessRequestActivity
import com.example.tmaptest.data.start
import com.example.tmaptest.retrofit.GeoCodingInterface
import com.example.tmaptest.retrofit.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skt.Tmap.TMapView
import retrofit2.Retrofit

class UsingCheckActivity : AppCompatActivity() {

    var tmapView: TMapView? = null

    private lateinit var auth: FirebaseAuth


    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: GeoCodingInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_using_check)

        //retrofit 초기화
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(GeoCodingInterface::class.java)

        //DB초기화
        val db = Firebase.firestore
        val docRef = db.collection("pick_up_request")
        auth = Firebase.auth

        //지도 띄어주기
        val maps = findViewById<ConstraintLayout>(R.id.TMapView)
        tmapView = TMapView(this)
        tmapView!!.setSKTMapApiKey("l7xx961891362ed44d06a261997b67e5ace6")
        tmapView!!.setZoom(13f)
        tmapView!!.setIconVisibility(false)
        tmapView!!.setMapType(TMapView.MAPTYPE_STANDARD)
        tmapView!!.setLanguage(TMapView.LANGUAGE_KOREAN)
        maps.addView(tmapView)

        //각종 레이아웃 변수 선언
        var back_button = findViewById<Button>(R.id.btn_back)
        var finish_button = findViewById<Button>(R.id.btn_finish)
        var addr1 = findViewById<TextView>(R.id.addr1)
        var addr2 = findViewById<TextView>(R.id.addr2)
        var re = findViewById<TextView>(R.id.re)
        var km = findViewById<TextView>(R.id.km)
        var time = findViewById<TextView>(R.id.time)

        //intent로 해당 건수의 이름 가져오기
        val data = intent.getStringExtra("data")

        //데이터 값의 변동이 있을때 감지해서 실시간으로 기사님 위치 판단
        docRef.document(data.toString()).addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            //변화가 있을때 실행되는 코드
            if (snapshot != null && snapshot.exists()) {
                Log.d("change status", "변화 감지.")

                val result = snapshot.data!!.get("ready_flag_1").toString()

                if (result == "1") {

                    finish_button.visibility=View.VISIBLE
                } else {//변화가 없으면 실행되는 코드
                    Log.d("change status", "변화없음.")
                    return@addSnapshotListener
                }

            } else {
                Log.d(ContentValues.TAG, "Current data: null")
            }

        }//addSnapShotListener

        //뒤로가기 버튼
        back_button.setOnClickListener {
            val intent = Intent(this, UseListFragment_1::class.java)
            startActivity(intent)
        }

        //픽업 확인 버튼(기사가 픽업완료 눌렀을 때 값 감지 해서 활성화 유무 판단)

        finish_button.setOnClickListener {
            Log.d("좋아","!!!!!!!")

            docRef.document(data.toString()).update("ready_flag_2",1)

        }

    }
}