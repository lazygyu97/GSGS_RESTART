package com.example.gsgs_plus_final.pickUp

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.data.routes
import com.example.tmaptest.retrofit.GeoCodingInterface
import com.example.tmaptest.retrofit.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skt.Tmap.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class BeforePickUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    var tmapView: TMapView? = null

    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: GeoCodingInterface
    private lateinit var km: String
    private lateinit var time: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_pick_up)

        //DB접근을 위한 Firebase 객체 선언 부분
        val db = Firebase.firestore
        auth = Firebase.auth

        retrofit = RetrofitClient.getInstance() // retrofit 초기화
        supplementService = retrofit.create(GeoCodingInterface::class.java) // 서비스 가져오기

        val btn_finish = findViewById<Button>(R.id.btn_finish)
        val pick_addr1 = findViewById<TextView>(R.id.addr1)
        val pick_addr2 = findViewById<TextView>(R.id.addr2)
        val re = findViewById<TextView>(R.id.re)
        val time_txt = findViewById<TextView>(R.id.time)
        val km_txt = findViewById<TextView>(R.id.km)

        val DB = intent.getStringExtra("Data")
        val lat = intent.getStringExtra("MyLocation_lat").toString()
        val lon = intent.getStringExtra("MyLocation_lon").toString()
        Log.d("sdfsdf", lat)

        val maps = findViewById<ConstraintLayout>(R.id.TMapView)
        tmapView = TMapView(this)
        tmapView!!.setSKTMapApiKey("l7xx961891362ed44d06a261997b67e5ace6")
        tmapView!!.setZoom(13f)
        tmapView!!.setIconVisibility(false)
        tmapView!!.setMapType(TMapView.MAPTYPE_STANDARD)
        tmapView!!.setLanguage(TMapView.LANGUAGE_KOREAN)
        maps.addView(tmapView)

        fun getRoutes(
            service: GeoCodingInterface, endX: Double,
            endY: Double, startX: Double,
            startY: Double, appKey: String,
            totalValue: Int
        ) {
            service.getRoutes(
                endX,
                endY,
                startX,
                startY,
                appKey,
                totalValue
            )
                .enqueue(object : Callback<routes> {

                    override fun onFailure(
                        call: Call<routes>,
                        error: Throwable
                    ) {
                        Log.d("TAG", "실패 원인: {$error}")
                    }

                    override fun onResponse(
                        call: Call<routes>,
                        response: Response<routes>
                    ) {
                        Log.d("TAG", "성공")
                        Log.d(
                            "result",
                            response.body()?.features?.get(0)?.properties?.totalDistance.toString()
                        )
                        time = response.body()?.features?.get(0)?.properties?.totalTime.toString()
                        val min = Integer.parseInt(time) / 60
                        val hour = min / 60

                        if (hour == 0) {
                           time_txt.text = min.toString() + "분"
                        } else {
                            if (min > 60) {
                                var h = min / 60
                                var m = min % 60
                              time_txt.text = (hour + h).toString() + "시간" + m.toString() + "분"
                            } else {
                             time_txt.text = hour.toString() + "시간" + min.toString() + "분"
                            }
                        }
                        km = response.body()?.features?.get(0)?.properties?.totalDistance.toString()

                        km_txt.text = (km!!.toDouble() / 1000).toString() + "km"
                    }
                })
        }

        Log.d("beforepickup", DB.toString())
        val docRef = db.collection("pick_up_request").document(DB.toString())

        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                val split = document.data?.get("pick_up_item_addr_start").toString().split("!")

                pick_addr1.setText(split[0])
                pick_addr2.setText(split[1])
                re.setText(document.data?.get("pick_up_item_request").toString())

                //예상 경로 찍어주기
                val start_x = document.data?.get("startX").toString()
                val start_y = document.data?.get("startY").toString()
                getRoutes(
                    supplementService,
                    lon.toDouble(),
                    lat.toDouble(),
                    start_y.toDouble(),
                    start_x.toDouble(),
                    "l7xx961891362ed44d06a261997b67e5ace6",
                    2
                )
                val t = Thread() {
                    val pointS = TMapPoint(start_x.toDouble(), start_y.toDouble())
                    val pointE = TMapPoint(lat.toDouble(), lon.toDouble())

                    val markerItem1 = TMapMarkerItem()
                    val markerItem2 = TMapMarkerItem()

                    val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.pin)
                    val bitmap2 = BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.delivery_pin
                    )

                    markerItem1.icon = bitmap // 마커 아이콘 지정
                    markerItem1.setPosition(0.5f, 1.0f) // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem1.tMapPoint = pointS // 마커의 좌표 지정

                    markerItem2.icon = bitmap2 // 마커 아이콘 지정
                    markerItem2.setPosition(0.5f, 1.0f) // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem2.tMapPoint = pointE // 마커의 좌표 지정


                    tmapView!!.addMarkerItem(
                        "markerItem1",
                        markerItem1
                    ) // 지도에 마커 추가
                    tmapView!!.addMarkerItem(
                        "markerItem2",
                        markerItem2
                    ) // 지도에 마커 추가

                    try {
                        val poly: TMapPolyLine = TMapData().findPathData(pointS, pointE)

                        poly.lineColor = Color.BLUE
                        poly.lineWidth = 20F

                        if (poly.distance / 1000 > 50) {
                            tmapView!!.setZoom(8f)
                        } else if (poly.distance / 1000 > 10 && poly.distance / 1000 < 50) {
                            tmapView!!.setZoom(11f)
                        } else {
                            tmapView!!.setZoom(13f)
                        }

                        tmapView!!.addTMapPolyLine("Line1", poly)

                        tmapView!!.setCenterPoint(
                            poly.linePoint[(poly.linePoint.size) / 2].longitude,
                            poly.linePoint[(poly.linePoint.size) / 2].latitude
                        )
                    } catch (e: Exception) {

                    }

                }.start()

            } else {

            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }




        btn_finish.setOnClickListener {

            val intent = Intent(this, DoingPickUpActivity::class.java)
            intent.putExtra("Data", DB)
            startActivity(intent)
        }
    }
}