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
    val poly1 = TMapPolyLine()

    private lateinit var auth: FirebaseAuth
    private lateinit var startX: String
    private lateinit var startY: String
    private lateinit var endX: String
    private lateinit var endY: String
    private lateinit var addr_end: String
    private lateinit var rs: String
    private lateinit var data: String

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

        retrofit = RetrofitClient.getInstance() // retrofit ?????????
        supplementService = retrofit.create(GeoCodingInterface::class.java) // ????????? ????????????

        //????????? ????????????
        tmap = TMapGpsManager(this)
        tmap!!.minTime = 1000
        tmap!!.minDistance = 5F

        if (Build.DEVICE.substring(0, 3) == "emu") {
            Log.d("----device: ", "????????? ???????????????")
            tmap!!.provider = GPS_PROVIDER
        } else {
            Log.d("----device: ", "????????? ????????????!")
            tmap!!.provider = NETWORK_PROVIDER
        }


        var intent2 = intent.getStringExtra("data")

        if (intent2 != null) {
            data = intent.getStringExtra("data").toString()
            Log.d("Before?????? ????????? Data", data)
        } else {
            data = intent.getStringExtra("Data").toString()
            Log.d("Before?????? ????????? Data2", data)
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




        //?????? ????????????
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
            totalValue: Int,
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
                        error: Throwable,
                    ) {
                        Log.d("TAG", "?????? ??????: {$error}")
                    }

                    override fun onResponse(
                        call: Call<routes>,
                        response: Response<routes>,
                    ) {
                        Log.d("TAG", "??????")
                        Log.d(
                            "result",
                            response.body()?.features?.get(0)?.properties?.totalDistance.toString()
                        )
                        time = response.body()?.features?.get(0)?.properties?.totalTime.toString()
                        val min = Integer.parseInt(time) / 60
                        val hour = min / 60

                        if (hour == 0) {
                            time_txt.text = min.toString() + "???"
                        } else {
                            if (min > 60) {
                                var h = min / 60
                                var m = min % 60
                                time_txt.text = (hour + h).toString() + "??????" + m.toString() + "???"
                            } else {
                                time_txt.text = hour.toString() + "??????" + min.toString() + "???"
                            }
                        }
                        km = response.body()?.features?.get(0)?.properties?.totalDistance.toString()

                        km_txt.text = (km!!.toDouble() / 1000).toString() + "km"
                    }
                })
        }

        //???????????? ????????? ????????? ???????????? ????????? ??????
        docRef.document(data.toString()).get().addOnSuccessListener { document ->
            if (document != null) {

                startX = document.data?.get("startX").toString()
                startY = document.data?.get("startY").toString()
                endX = document.data?.get("endX").toString()
                endY = document.data?.get("endY").toString()
                addr_end = document.data?.get("pick_up_item_addr_end").toString()
                rs = document.data?.get("pick_up_item_request").toString()

                val split1 = addr_end.split("!")
                Log.d("?????????...",split1.toString())
                pick_addr1.setText(split1[0])
                pick_addr2.setText(split1[1])
                request.setText(rs)

                //???????????? ????????????
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

                    markerItem1.icon = bitmap // ?????? ????????? ??????
                    markerItem1.setPosition(0.5f, 1.0f) // ????????? ???????????? ??????, ???????????? ??????
                    markerItem1.tMapPoint = pointS // ????????? ?????? ??????

                    markerItem2.icon = bitmap2 // ?????? ????????? ??????
                    markerItem2.setPosition(0.5f, 1.0f) // ????????? ???????????? ??????, ???????????? ??????
                    markerItem2.tMapPoint = pointE // ????????? ?????? ??????


                    tmapView!!.addMarkerItem(
                        "markerItem1",
                        markerItem1
                    ) // ????????? ?????? ??????
                    tmapView!!.addMarkerItem(
                        "markerItem2",
                        markerItem2
                    ) // ????????? ?????? ??????

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
            Log.d(ContentValues.TAG, "get failed with ", exception)
        }


        btn_finish.setOnClickListener {

            docRef.document(data.toString()).get().addOnSuccessListener { task ->
                if (task.data!!.get("uid_2").toString() == auth.currentUser!!.uid) {
                    docRef.document(data.toString()).update("ready_flag_2", "1")
                }
            }

        }
        //????????? ?????? ????????? ????????? ???????????? ??????????????? ????????? ?????? ??????
        real_time = docRef.document(data).addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            //????????? ????????? ???????????? ??????
            if (snapshot != null && snapshot.exists()) {
                Log.d("change status", "?????? ??????.")

                val result = snapshot.data!!.get("pick_up_check_flag").toString()

                if (result == "3") {
                    val intent = Intent(this, FinishPickUpActivity::class.java)
                    startActivity(intent)

                } else {//????????? ????????? ???????????? ??????
                    Log.d("change status", "????????????.")

                }

            } else {
                Log.d(ContentValues.TAG, "Current data: null")
            }

        }//addSnapShotListener


    }

    //????????? ???????????? ??????
    override fun onLocationChange(p0: Location?) {

        Log.d("????????????1", p0!!.latitude.toString())
        Log.d("????????????1", p0.longitude.toString())
        val bitmap3 = BitmapFactory.decodeResource(this.resources, R.drawable.delivery_pin)
        val markerItem3 = TMapMarkerItem()
        markerItem3.icon = bitmap3 // ?????? ????????? ??????
        markerItem3.setPosition(0.5f, 1.0f) // ????????? ???????????? ??????, ???????????? ??????
        markerItem3.tMapPoint = TMapPoint(p0.latitude, p0.longitude) // ????????? ?????? ??????
        tmapView!!.addMarkerItem(
            "markerItem3",
            markerItem3
        ) // ????????? ?????? ??????

        // db??? ??????????????? ?????????
        val db = Firebase.firestore
        val docRef = db.collection("pick_up_request")

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