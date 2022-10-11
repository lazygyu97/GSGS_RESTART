package com.example.gsgs_plus_final.pickUp

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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


class BeforePickUpActivity : AppCompatActivity(), TMapGpsManager.onLocationChangedCallback {

    var tmapView: TMapView? = null
    var result: ArrayList<String>? = null
    val poly1 = TMapPolyLine()
    private var tmap: TMapGpsManager? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: GeoCodingInterface
    private lateinit var km: String
    private lateinit var time: String
    private lateinit var tmaptapi: TMapTapi
    private lateinit var data:String
    private lateinit var real_time: ListenerRegistration

    // 티맵 설치 설치 권유 다이얼로그
    private fun ask_download() {
        val ask_down = AlertDialog.Builder(this)
        ask_down
            .setMessage("Tmap이 설치 되어있지 않습니다\n설치하시겠습니까?")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val url = result?.get(1)
                    Log.d("설치 설치", url.toString())
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id ->
                })
        // 다이얼로그를 띄워주기
        ask_down.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_pick_up)

        //DB접근을 위한 Firebase 객체 선언 부분
        val db = Firebase.firestore
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
        val pick_addr1 = findViewById<TextView>(R.id.addr1)
        val pick_addr2 = findViewById<TextView>(R.id.addr2)
        val re = findViewById<TextView>(R.id.re)
        val time_txt = findViewById<TextView>(R.id.time)
        val km_txt = findViewById<TextView>(R.id.km)
        val go_tmap = findViewById<Button>(R.id.go_tmap)
        var lat : String
        var lon : String

        var intent2 = intent.getStringExtra("data")

        val DB = intent.getStringExtra("Data")


        if(intent2!=null){
            data = intent.getStringExtra("data").toString()
            lat =  intent.getStringExtra("MyLocation_lat2").toString()
            lon =  intent.getStringExtra("MyLocation_lon2").toString()
            Log.d("Before에서 넘어온 Data",data)
        }else{
            data = intent.getStringExtra("Data").toString()
            lat = intent.getStringExtra("MyLocation_lat").toString()
            lon = intent.getStringExtra("MyLocation_lon").toString()
            Log.d("Before에서 넘어온 Data2",data)
        }


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

        Log.d("beforepickup", data.toString())
        val docRef = db.collection("pick_up_request").document(data)
        val docRef2 = db.collection("pick_up_request")

        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                val split = document.data?.get("pick_up_item_addr_start").toString().split("!")

                pick_addr1.setText(split[0])
                pick_addr2.setText(split[1])
                re.setText(document.data?.get("pick_up_item_request").toString())

                //예상 경로 찍어주기
                val start_x = document.data?.get("startX").toString()
                val start_y = document.data?.get("startY").toString()
                Log.d("아니?",lat)
                Log.d("아니?",lon)
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

            } else {

            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }


        //tmap 연동 코드
        go_tmap.setOnClickListener {
            val isTmapApp = tmaptapi.isTmapApplicationInstalled
            Log.d("tmap 설치유무 ", isTmapApp.toString())

            if (!isTmapApp) {
                result = tmaptapi.tMapDownUrl
                Log.d("tmap 설치 URL", result.toString())
                ask_download()
            } else {
                docRef.get().addOnSuccessListener { document ->
                    if (document != null) {
                        val start_x = document.data?.get("startX").toString()
                        val start_y = document.data?.get("startY").toString()
                        tmaptapi.invokeNavigate(
                            pick_addr1.text.toString(),
                            start_x.toFloat(), start_y.toFloat(), 0, true
                        )
                    }
                }

            }
        }

        btn_finish.setOnClickListener {

            docRef2.document(data.toString()).get().addOnSuccessListener { task ->
                if (task.data!!.get("uid_2").toString() == auth.currentUser!!.uid) {
                    docRef2.document(data.toString()).update("ready_flag_1", "1")
                }
            }
        }

        //데이터 값의 변동이 있을때 감지해서 실시간으로 기사님 위치 판단
        real_time= docRef2.document(data).addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            //변화가 있을때 실행되는 코드
            if (snapshot != null && snapshot.exists()) {
                Log.d("change status", "변화 감지.")

                val result = snapshot.data!!.get("ready_flag_2").toString()

                if (result == "1"&& snapshot.data!!.get("doing_x")==null) {
                    val intent = Intent(this, DoingPickUpActivity::class.java)
                    intent.putExtra("Data", DB)
                    startActivity(intent)
                    finishAndRemoveTask()
                } else {//변화가 없으면 실행되는 코드
                    Log.d("change status", "변화없음.")

                }

            } else {
                Log.d(ContentValues.TAG, "Current data: null")
            }

        }//addSnapShotListener

    }


    override fun onLocationChange(p0: Location?) {

        Log.d("현재위치1", p0!!.latitude.toString())
        Log.d("현재위치1", p0.longitude.toString())

        val bitmap3 = BitmapFactory.decodeResource(this.resources, R.drawable.delivery_pin)
        val markerItem3 = TMapMarkerItem()
        markerItem3.icon = bitmap3 // 마커 아이콘 지정
        markerItem3.setPosition(0.5f, 1.0f) // 마커의 중심점을 중앙, 하단으로 설정
        markerItem3.tMapPoint = TMapPoint(p0!!.latitude, p0!!.longitude) // 마커의 좌표 지정
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
                "picking_x",
                FieldValue.arrayUnion(p0.latitude)
            )

        docRef.document(data)
            .update(
                "picking_y",
                FieldValue.arrayUnion(p0.longitude)
            )
    }

    override fun onDestroy() {
        super.onDestroy()
        tmap!!.CloseGps()
        Log.d("BEFORE_PICKUP","스냅샷리스너 1 종료")
        real_time.remove()
    }
}