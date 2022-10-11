package com.example.gsgs_plus_final.using

import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.data.routes
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
import com.skt.Tmap.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class UsingCheckActivity : AppCompatActivity() {

    var tmapView: TMapView? = null
    val poly1 = TMapPolyLine()

    private lateinit var auth: FirebaseAuth
//    private lateinit var lat_result: ArrayList<Double>
//    private lateinit var lon_result: ArrayList<Double>


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

        //경로 계산 함수
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
                        val time_t =
                            response.body()?.features?.get(0)?.properties?.totalTime.toString()
                        val min = Integer.parseInt(time_t) / 60
                        val hour = min / 60

                        if (hour == 0) {
                            time.text = min.toString() + "분"
                        } else {
                            if (min > 60) {
                                var h = min / 60
                                var m = min % 60
                                time.text = (hour + h).toString() + "시간" + m.toString() + "분"
                            } else {
                                time.text = hour.toString() + "시간" + min.toString() + "분"
                            }
                        }
                        val km_t =
                            response.body()?.features?.get(0)?.properties?.totalDistance.toString()

                        km.text = (km_t!!.toDouble() / 1000).toString() + "km"
                    }
                })
        }

        //intent로 해당 건수의 이름 가져오기
        val data = intent.getStringExtra("data")
        docRef.document(data.toString()).get().addOnSuccessListener { document ->
            if (document != null) {
                val split = document.data?.get("pick_up_item_addr_start").toString().split("!")
                addr1.setText(split[0])
                addr2.setText(split[1])
                re.setText(document.data?.get("pick_up_item_request").toString())
                val lat = document.data?.get("startX").toString()
                val lon = document.data?.get("startY").toString()
                val lat_picker:ArrayList<Double> = document.data?.get("picking_x") as ArrayList<Double>
                val lon_picker:ArrayList<Double> = document.data?.get("picking_y") as ArrayList<Double>


                if (lat_picker != null && lon_picker != null) {
                    getRoutes(
                        supplementService,
                        lon_picker[0],
                        lat_picker[0],
                        lon.toDouble(),
                        lat.toDouble(),
                        "l7xx961891362ed44d06a261997b67e5ace6",
                        2
                    )
                    val t = Thread() {
                        val pointS = TMapPoint(lat_picker[0], lon_picker[0])
                        val pointE = TMapPoint(lat.toDouble(), lon.toDouble())

                        val markerItem1 = TMapMarkerItem()
                        val markerItem2 = TMapMarkerItem()

                        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.pin)
                        val bitmap2 = BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.pin
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

                            poly.lineColor = Color.LTGRAY
                            poly.outLineColor = Color.DKGRAY
                            poly.lineAlpha = 100
                            poly.outLineAlpha = 100
                            poly.outLineWidth = 20f
                            poly.lineWidth = 10f

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

                }


            }
        }

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

                //x,y 값 업데이트 되는지 확인하기
                val lat_result: ArrayList<Double> = snapshot.data!!.get("picking_x") as ArrayList<Double>
                val lon_result: ArrayList<Double> = snapshot.data!!.get("picking_y") as ArrayList<Double>

                if (lat_result != null) {
                    Log.d("실시간 위치 탐색 x", lat_result.javaClass.name)
                    if (lon_result != null) {
                        Log.d("실시간 위치 탐색 y", lon_result.javaClass.name)
                    }
                }
                Log.d("실시간 위치 탐색 y", lon_result.toString())
                Log.d("실시간 위치 탐색 y", lat_result.get(lat_result.lastIndex).javaClass.name)
                Log.d("실시간 위치 탐색 y", lat_result.get(lat_result.lastIndex).toString())

                //배달자 마커 찍어주기
                val bitmap3 = BitmapFactory.decodeResource(this.resources, R.drawable.delivery_pin)
                val markerItem3 = TMapMarkerItem()
                markerItem3.icon = bitmap3 // 마커 아이콘 지정
                markerItem3.setPosition(0.5f, 1.0f) // 마커의 중심점을 중앙, 하단으로 설정
                markerItem3.tMapPoint = TMapPoint(
                    lat_result.get(lat_result.lastIndex),
                    lon_result.get(lon_result.lastIndex)
                ) // 마커의 좌표 지정
//                tmapView!!.setCenterPoint(
//                    lat_result.get(0), lon_result.get(0)
//                )
                tmapView!!.addMarkerItem(
                    "markerItem3",
                    markerItem3
                ) // 지도에 마커 추가

                val t2 = Thread() {
                    val lo = TMapPoint(
                        lat_result.get(lat_result.lastIndex),
                        lon_result.get(lon_result.lastIndex)
                    )

                    poly1.addLinePoint(lo)

                    poly1.outLineColor = Color.BLUE
                    poly1.outLineWidth = 20f

                    tmapView!!.addTMapPolyLine("Line2", poly1)

                }.start()


                if (result == "1") {

                    finish_button.visibility = View.VISIBLE
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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //픽업 확인 버튼(기사가 픽업완료 눌렀을 때 값 감지 해서 활성화 유무 판단)

        finish_button.setOnClickListener {
            Log.d("좋아", "!!!!!!!")

            docRef.document(data.toString()).update("ready_flag_2", 1)

        }

    }
}