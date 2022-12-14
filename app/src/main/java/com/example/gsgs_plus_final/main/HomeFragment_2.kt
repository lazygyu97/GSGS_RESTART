package com.example.gsgs_plus_final.main

import android.R
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils.split
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gsgs_plus_final.adapter.PickUpListAdapter
import com.example.gsgs_plus_final.data.routes
import com.example.gsgs_plus_final.pickUp.BeforePickUpActivity
import com.example.gsgs_plus_final.vo.LoadingDialog
import com.example.gsgs_plus_final.vo.pick_list
import com.example.tmaptest.data.start
import com.example.tmaptest.retrofit.GeoCodingInterface
import com.example.tmaptest.retrofit.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skt.Tmap.*
import com.skt.Tmap.TMapGpsManager.GPS_PROVIDER
import com.skt.Tmap.TMapGpsManager.NETWORK_PROVIDER
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask
import kotlin.math.log
import kotlin.text.Typography.tm


class HomeFragment_2 : Fragment(), TMapGpsManager.onLocationChangedCallback {

    private lateinit var auth: FirebaseAuth
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: GeoCodingInterface

    val tm = Timer()
    private lateinit var timer: TimerTask

    var findStartX: String? = null
    var findStartY: String? = null
    var findEndX: String? = null
    var findEndY: String? = null
    var time: String? = null
    var fare: String? = null
    var km: String? = null
    var tmapView: TMapView? = null
    var tmap: TMapGpsManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        retrofit = RetrofitClient.getInstance() // retrofit ?????????
        supplementService = retrofit.create(GeoCodingInterface::class.java) // ????????? ????????????


        val v =
            inflater.inflate(com.example.gsgs_plus_final.R.layout.fragment_home_2, container, false)
        val mainAct = activity as MainActivity
        var dialog = LoadingDialog(requireContext())


        val maps = v.findViewById<ConstraintLayout>(com.example.gsgs_plus_final.R.id.TMapView)
        tmapView = TMapView(context)
        tmapView!!.setSKTMapApiKey("l7xx961891362ed44d06a261997b67e5ace6")


        tmapView!!.setZoom(13f)
        tmapView!!.setIconVisibility(false)
        tmapView!!.setMapType(TMapView.MAPTYPE_STANDARD)
        tmapView!!.setLanguage(TMapView.LANGUAGE_KOREAN)
        //????????? ?????? ????????? ??????
        maps.addView(tmapView)

        tmap = TMapGpsManager(context)
        if(Build.DEVICE.substring(0,3)=="emu"){
            Log.d("----device: ","????????? ???????????????")
            tmap!!.provider = GPS_PROVIDER
        }else{
            Log.d("----device: ","????????? ????????????!")
            tmap!!.provider = NETWORK_PROVIDER
        }
        tmap!!.minTime = 1000
        tmap!!.OpenGps()

        val page = v.findViewById<LinearLayout>(com.example.gsgs_plus_final.R.id.page)
        val close_btn = v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.close_btn)
        val list = v.findViewById<RecyclerView>(com.example.gsgs_plus_final.R.id.using_list_view)
        val accept = v.findViewById<Button>(com.example.gsgs_plus_final.R.id.accept)
        // use a linear layout manager
        val mLayoutManager = LinearLayoutManager(context);
        list.setLayoutManager(mLayoutManager);

        //DB????????? ?????? Firebase ?????? ?????? ??????

        val db = Firebase.firestore
        auth = Firebase.auth

        val docRef = db.collection("pick_up_request")
        val docRef2 = db.collection("users")
        val docRef3 = db.collection("pickers")


        //??????????????? ???????????????
        val animation_1 =
            AnimationUtils.loadAnimation(context, com.example.gsgs_plus_final.R.anim.translate_up)
        val animation_2 =
            AnimationUtils.loadAnimation(context, com.example.gsgs_plus_final.R.anim.translate_down)
        val pickList = ArrayList<pick_list>()


        fun load_request() {

            fun getRoutes_2(
                service: GeoCodingInterface, endX: Double,
                endY: Double, startX: Double,
                startY: Double, appKey: String,
                totalValue: Int
            ) {
                service.getRoutes(endX, endY, startX, startY, appKey, totalValue)
                    .enqueue(object :
                        Callback<routes> {

                        override fun onFailure(call: Call<routes>, error: Throwable) {
                            Log.d("TAG", "?????? ??????: {$error}")
                        }

                        override fun onResponse(
                            call: Call<routes>,
                            response: Response<routes>
                        ) {
                            Log.d("TAG", "??????")
                            Log.d(
                                "result",
                                response.body()?.features?.get(0)?.properties?.totalDistance.toString()
                            )
                            km =
                                response.body()?.features?.get(0)?.properties?.totalDistance.toString()

                            val km1 = km!!.toDouble() / 1000
                            if (km1 < 50.000) {
                                docRef.whereEqualTo("pick_up_check_flag", "0").get()
                                    .addOnSuccessListener { documents ->
                                        for (document in documents) {
                                            findStartX = document.data["startX"].toString()
                                            findStartY = document.data["startY"].toString()
                                            if (startX.toString() == findStartY && startY.toString() == findStartX) {
                                                Log.d("SEX", "power")
                                                val start_addr: String =
                                                    document.data["pick_up_item_addr_start"].toString()
                                                val end_addr: String =
                                                    document.data["pick_up_item_addr_end"].toString()
                                                val item_name: String =
                                                    document.data["pick_up_item_name"].toString()


                                                val start = start_addr.substring(8, 14)
                                                val end = end_addr.substring(8, 14)

                                                findStartX = document.data["startX"].toString()
                                                findStartY = document.data["startY"].toString()
                                                findEndX = document.data["endX"].toString()
                                                findEndY = document.data["endY"].toString()

                                                val request_cost: String =
                                                    document.data["pick_up_item_cost"].toString()
                                                val document_id: String = document.id
                                                val pick_up_flag: String =
                                                    document.data["pick_up_check_flag"].toString()
                                                pickList.apply {

                                                    add(
                                                        pick_list(
                                                            item_name,
                                                            start_addr,
                                                            end_addr,
                                                            start,
                                                            end,
                                                            findStartX,
                                                            findStartY,
                                                            findEndX,
                                                            findEndY,
                                                            request_cost,
                                                            document_id,
                                                            pick_up_flag
                                                        )
                                                    )

                                                }
                                                val adapter = PickUpListAdapter(pickList)
                                                var accept_doc_id: String? = "No id"


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
                                                        .enqueue(object :
                                                            Callback<routes> {

                                                            override fun onFailure(
                                                                call: Call<routes>,
                                                                error: Throwable
                                                            ) {
                                                                Log.d("TAG", "?????? ??????: {$error}")
                                                            }

                                                            override fun onResponse(
                                                                call: Call<routes>,
                                                                response: Response<routes>
                                                            ) {
                                                                Log.d("TAG", "??????")
                                                                Log.d(
                                                                    "result",
                                                                    response.body()?.features?.get(0)?.properties?.totalDistance.toString()
                                                                )
                                                                km =
                                                                    response.body()?.features?.get(0)?.properties?.totalDistance.toString()
                                                                fare =
                                                                    response.body()?.features?.get(0)?.properties?.totalFare.toString()
                                                                time =
                                                                    response.body()?.features?.get(0)?.properties?.totalTime.toString()
                                                                val min =
                                                                    Integer.parseInt(time) / 60
                                                                val hour = min / 60

                                                                if (hour == 0) {
                                                                    v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.time).text =
                                                                        min.toString() + "???"
                                                                } else {
                                                                    if (min > 60) {
                                                                        var h = min / 60
                                                                        var m = min % 60
                                                                        v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.time).text =
                                                                            (hour + h).toString() + "??????" + m.toString() + "???"
                                                                    } else {
                                                                        v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.time).text =
                                                                            hour.toString() + "??????" + min.toString() + "???"
                                                                    }
                                                                }

                                                                v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.fare).text =
                                                                    fare + "???"
                                                                v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.km).text =
                                                                    (km!!.toDouble() / 1000).toString() + "km"
                                                            }
                                                        })
                                                }



                                                adapter.setOnItemClickListener(object :
                                                    PickUpListAdapter.OnItemClickListener {
                                                    override fun onItemClick(
                                                        data: pick_list,
                                                        pos: Int
                                                    ) {


                                                        list.visibility = View.INVISIBLE
                                                        mainAct.HideBottomNavi(true)
                                                        page.startAnimation(animation_1)
                                                        page.visibility = View.VISIBLE

                                                        v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.request_cost).text =
                                                            data.request_cost


                                                        val split =
                                                            data.addr_start_f.toString().split("!")
                                                        val split2 =
                                                            data.addr_end_f.toString().split("!")

                                                        v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.addr1).text =
                                                            split[0]
                                                        v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.addr2).text =
                                                            split[1]
                                                        v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.addr3).text =
                                                            split2[0]
                                                        v.findViewById<TextView>(com.example.gsgs_plus_final.R.id.addr4).text =
                                                            split2[1]

                                                        accept_doc_id = data.document_id

                                                        var item_startX = data.startX
                                                        var item_startY = data.startY
                                                        var item_endX = data.endx
                                                        var item_endY = data.endY

                                                        getRoutes(
                                                            supplementService,
                                                            item_endY!!.toDouble(),
                                                            item_endX!!.toDouble(),
                                                            item_startY!!.toDouble(),
                                                            item_startX!!.toDouble(),
                                                            "l7xx961891362ed44d06a261997b67e5ace6",
                                                            2
                                                        )


                                                        val t = Thread() {

                                                            val pointS =
                                                                TMapPoint(
                                                                    item_startX!!.toDouble(),
                                                                    item_startY!!.toDouble()
                                                                )
                                                            val pointE =
                                                                TMapPoint(
                                                                    item_endX!!.toDouble(),
                                                                    item_endY!!.toDouble()
                                                                )

                                                            val markerItem1 = TMapMarkerItem()
                                                            val markerItem2 = TMapMarkerItem()

                                                            val tMapPoint1 =
                                                                TMapPoint(
                                                                    item_startX!!.toDouble(),
                                                                    item_startY!!.toDouble()
                                                                )
                                                            val tMapPoint2 =
                                                                TMapPoint(
                                                                    item_endX!!.toDouble(),
                                                                    item_endY!!.toDouble()
                                                                )


                                                            val bitmap =
                                                                BitmapFactory.decodeResource(
                                                                    context!!.resources,
                                                                    com.example.gsgs_plus_final.R.drawable.pin
                                                                )

                                                            markerItem1.icon = bitmap // ?????? ????????? ??????
                                                            markerItem1.setPosition(
                                                                0.5f,
                                                                1.0f
                                                            ) // ????????? ???????????? ??????, ???????????? ??????
                                                            markerItem1.tMapPoint =
                                                                tMapPoint1 // ????????? ?????? ??????

                                                            markerItem2.icon = bitmap // ?????? ????????? ??????
                                                            markerItem2.setPosition(
                                                                0.5f,
                                                                1.0f
                                                            ) // ????????? ???????????? ??????, ???????????? ??????
                                                            markerItem2.tMapPoint =
                                                                tMapPoint2 // ????????? ?????? ??????


                                                            tmapView!!.addMarkerItem(
                                                                "markerItem1",
                                                                markerItem1
                                                            ) // ????????? ?????? ??????
                                                            tmapView!!.addMarkerItem(
                                                                "markerItem2",
                                                                markerItem2
                                                            ) // ????????? ?????? ??????


                                                            try {
                                                                Log.d("??????", "??????")
                                                                val poly: TMapPolyLine =
                                                                    TMapData().findPathData(
                                                                        pointS,
                                                                        pointE
                                                                    )
                                                                poly.lineColor = Color.BLUE
                                                                poly.lineWidth = 20F
                                                                Log.d(
                                                                    "???????????????",
                                                                    (poly.distance / 1000).toString()
                                                                )
                                                                if (poly.distance / 1000 > 50) {
                                                                    tmapView!!.setZoom(8f)
                                                                } else if (poly.distance / 1000 > 10 && poly.distance / 1000 < 50) {
                                                                    tmapView!!.setZoom(11f)
                                                                } else {
                                                                    tmapView!!.setZoom(13f)
                                                                }

                                                                tmapView!!.addTMapPolyLine(
                                                                    "Line1",
                                                                    poly
                                                                )
                                                                Log.d(
                                                                    "1912390",
                                                                    item_startX.toString()
                                                                )
                                                                Log.d(
                                                                    "1912390",
                                                                    item_startY.toString()
                                                                )
                                                                tmapView!!.setCenterPoint(
                                                                    poly.linePoint[(poly.linePoint.size) / 2].longitude,
                                                                    poly.linePoint[(poly.linePoint.size) / 2].latitude
                                                                )


                                                            } catch (e: Exception) {

                                                            }

                                                        }.start()



                                                        docRef.document(accept_doc_id!!).get()
                                                            .addOnSuccessListener { task ->
                                                                if (task.data!!.get("pick_up_check_flag") == "1" || task.data!!.get(
                                                                        "pick_up_check_flag"
                                                                    ) == "2"
                                                                ) {

                                                                    Toast.makeText(
                                                                        context,
                                                                        "?????? ????????? ???????????????!\n???????????? ????????????!",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                    list.visibility = View.VISIBLE
                                                                    mainAct.HideBottomNavi(false)
                                                                    page.startAnimation(animation_2)
                                                                    page.visibility = View.INVISIBLE
                                                                    return@addOnSuccessListener


                                                                }
                                                            }
                                                    }
                                                })

                                                close_btn.setOnClickListener {
                                                    list.visibility = View.VISIBLE
                                                    mainAct.HideBottomNavi(false)
                                                    page.startAnimation(animation_2)
                                                    page.visibility = View.INVISIBLE
                                                }

                                                accept.setOnClickListener {
                                                    activity?.let {


                                                        docRef.document(accept_doc_id!!)
                                                            .update("pick_up_check_flag", "1")
                                                        docRef.document(accept_doc_id!!)
                                                            .update("uid_2", auth.currentUser!!.uid)
                                                        docRef3.document(auth.currentUser!!.uid)
                                                            .update(
                                                                "pick_up_list",
                                                                FieldValue.arrayUnion(accept_doc_id)
                                                            )


                                                        val intent = Intent(
                                                            context,
                                                            BeforePickUpActivity::class.java
                                                        )
                                                        intent.putExtra(
                                                            "MyLocation_lat",
                                                            tmap!!.location.latitude.toString()
                                                        )
                                                        intent.putExtra(
                                                            "MyLocation_lon",
                                                            tmap!!.location.longitude.toString()
                                                        )
                                                        intent.putExtra("Data", accept_doc_id)
                                                        startActivity(intent)
                                                    }

                                                }

                                                list.adapter = adapter
                                                dialog.dismiss()

                                            }
                                        }

                                    }
                            }
                            Log.d("distance", km1.toString())

                        }
                    })
            }
            //??????????????? ?????????(?????? ????????? ??????)
            docRef.whereEqualTo("pick_up_check_flag", "0").get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        //?????? ????????? ?????? ??? ??????????????? ?????? Lod
                        Log.d("pick_up_list:", document.data.toString())
                        // Log.d("documentId:",document.id)
                        Log.d("addrStart:", document.data["pick_up_item_addr_start"].toString())
                        Log.d("addrEnd:", document.data["pick_up_item_addr_end"].toString())

                        val start_addr: String = document.data["pick_up_item_addr_start"].toString()
                        val end_addr: String = document.data["pick_up_item_addr_end"].toString()

                        val start = start_addr.substring(8, 14)
                        val end = end_addr.substring(8, 14)

                        findStartX = document.data["startX"].toString()
                        findStartY = document.data["startY"].toString()
                        getRoutes_2(
                            supplementService,
                            tmap!!.location.longitude,
                            tmap!!.location.latitude,
                            findStartY!!.toDouble(),
                            findStartX!!.toDouble(),
                            "l7xx961891362ed44d06a261997b67e5ace6",
                            2
                        )


                    }


                    // Inflate the layout for this fragment


                }.addOnFailureListener { exception ->
                    Log.w("No Such document!!!", exception)
                }


        }


        //?????? ????????? ??????
        fun foo() {
            Log.d("realtime", "wow2")
            Log.d("check!@# : ", tmap!!.location.latitude.toString())
            if (tmap!!.location.latitude !== 0.0) {
                timer.cancel()
                load_request()
            }

        }

        fun createTimerTask(): TimerTask {
            timer = timerTask { foo() }

            return timer;
        }

        fun main() {
            timer = createTimerTask()
            tm.schedule(timer, 0, 500);
        }

        dialog.show()
        main()


        return v

    }

    override fun onDetach() {
        super.onDetach()
        tmap!!.CloseGps()
    }

    override fun onLocationChange(p0: Location?) {

    }
}