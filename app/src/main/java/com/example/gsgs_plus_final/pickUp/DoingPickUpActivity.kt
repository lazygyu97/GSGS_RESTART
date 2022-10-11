package com.example.gsgs_plus_final.pickUp

import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.data.routes
import com.example.tmaptest.retrofit.GeoCodingInterface
import com.example.tmaptest.retrofit.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skt.Tmap.*
import com.skt.Tmap.TMapGpsManager.GPS_PROVIDER
import com.skt.Tmap.TMapGpsManager.NETWORK_PROVIDER
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class DoingPickUpActivity : AppCompatActivity(), TMapGpsManager.onLocationChangedCallback {

    var tmapView: TMapView? = null
    val poly1= TMapPolyLine()

    private lateinit var auth: FirebaseAuth
    private lateinit var startX: String
    private lateinit var startY: String
    private lateinit var endX: String
    private lateinit var endY: String
    private lateinit var addr_end: String
    private lateinit var rs: String
    private lateinit var data:String

    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: GeoCodingInterface
    private lateinit var km: String
    private lateinit var time: String
    private lateinit var tmaptapi: TMapTapi
    private lateinit var real_time: ListenerRegistration


    private var tmap: TMapGpsManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doing_pick_up)

        auth = Firebase.auth

        retrofit = RetrofitClient.getInstance() // retrofit 초기화
        supplementService = retrofit.create(GeoCodingInterface::class.java) // 서비스 가져오기

        //실시간 현재위치
        tmap = TMapGpsManager(this)
        tmap!!.minTime = 1000
        tmap!!.minDistance = 5F

        if(Build.DEVICE.substring(0,3)=="emu"){
            Log.d("----device: ","이것은 에뮬레이터")
            tmap!!.provider = GPS_PROVIDER
        }else{
            Log.d("----device: ","이것은 스마트폰!")
            tmap!!.provider = NETWORK_PROVIDER
        }

        tmap!!.OpenGps()

        val btn_finish = findViewById<Button>(R.id.btn_finish)
        val db = Firebase.firestore
        val docRef = db.collection("pick_up_request")
        val docRef2 = db.collection("pickers")

        val time_txt = findViewById<TextView>(R.id.time)
        val km_txt = findViewById<TextView>(R.id.km)
        val go_tmap = findViewById<Button>(R.id.go_tmap)
        val pick_addr1 = findViewById<TextView>(R.id.addr1)
        val pick_addr2 = findViewById<TextView>(R.id.addr2)
        val request = findViewById<TextView>(R.id.re)

        data = intent.getStringExtra("Data").toString()


        //지도 띄어주기
        val maps = findViewById<ConstraintLayout>(R.id.TMapView)
        tmapView = TMapView(this)
        tmapView!!.setSKTMapApiKey("l7xx961891362ed44d06a261997b67e5ace6")
        tmapView!!.setZoom(13f)
        tmapView!!.setIconVisibility(false)
        tmapView!!.setMapType(TMapView.MAPTYPE_STANDARD)
        tmapView!!.setLanguage(TMapView.LANGUAGE_KOREAN)
        maps.addView(tmapView)

        tmaptapi = TMapTapi(this)

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

        //배송중인 건수에 이름을 기반으로 데이터 조회
        docRef.document(data.toString()).get().addOnSuccessListener { document ->
            if (document != null) {

                startX = document.data?.get("startX").toString()
                startY = document.data?.get("startY").toString()
                endX = document.data?.get("endX").toString()
                endY = document.data?.get("endY").toString()
                addr_end = document.data?.get("pick_up_item_addr_end").toString()
                rs = document.data?.get("pick_up_item_request").toString()

                val split = addr_end.split("!")
                pick_addr1.setText(split[0])
                pick_addr2.setText(split[1])
                request.setText(rs)

                //예상경로 찍어주기
                getRoutes(
                    supplementService,
                    startY.toDouble(),
                    startX.toDouble(),
                    endY.toDouble(),
                    endX.toDouble(),
                    "l7xx961891362ed44d06a261997b67e5ace6",
                    2
                )

                val t = Thread() {
                    val pointS = TMapPoint(startX.toDouble(), startY.toDouble())
                    val pointE = TMapPoint(endX.toDouble(), endY.toDouble())

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
                        poly.outLineColor=Color.DKGRAY
                        poly.lineAlpha=100
                        poly.outLineAlpha=100
                        poly.outLineWidth=20f
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

            } else {

            }
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "get failed with ", exception)
        }


        btn_finish.setOnClickListener {

            docRef.document(data.toString()).get().addOnSuccessListener { task ->
                if (task.data!!.get("uid_2").toString() == auth.currentUser!!.uid) {
                    docRef.document(data.toString()).update("pick_up_check_flag", "3")
                }
            }

            val intent = Intent(this, FinishPickUpActivity::class.java)
            startActivity(intent)
        }

    }

    //실시간 위치변화 감지
    override fun onLocationChange(p0: Location?) {

        Log.d("현재위치1", p0!!.latitude.toString())
        Log.d("현재위치1", p0.longitude.toString())
        val bitmap3 = BitmapFactory.decodeResource(this.resources, R.drawable.delivery_pin)
        val markerItem3 = TMapMarkerItem()
        markerItem3.icon = bitmap3 // 마커 아이콘 지정
        markerItem3.setPosition(0.5f, 1.0f) // 마커의 중심점을 중앙, 하단으로 설정
        markerItem3.tMapPoint = TMapPoint(p0.latitude, p0.longitude) // 마커의 좌표 지정
        tmapView!!.addMarkerItem(
            "markerItem3",
            markerItem3
        ) // 지도에 마커 추가

        // db에 패스데이터 넘기기
        val db = Firebase.firestore
        val docRef = db.collection("pick_up_request")

        val t2 = Thread() {
           val lo= TMapPoint(p0.latitude, p0.longitude)

            poly1.addLinePoint(lo)

            poly1.outLineColor = Color.BLUE
            poly1.outLineWidth = 20f

            tmapView!!.addTMapPolyLine("Line2", poly1)

        }.start()

        docRef.document(data)
            .update(
                "doing_x",
                FieldValue.arrayUnion(p0.latitude)
            )

        docRef.document(data)
            .update(
                "doing_y",
                FieldValue.arrayUnion(p0.longitude)
            )

    }
}