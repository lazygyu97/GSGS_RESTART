package com.example.gsgs_plus_final.main

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.request.DoingRequestActivity
import com.example.gsgs_plus_final.vo.PickUpRequest
import com.example.gsgs_plus_final.vo.pick_mark
import com.example.tmaptest.data.start
import com.example.tmaptest.retrofit.GeoCodingInterface
import com.example.tmaptest.retrofit.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skt.Tmap.TMapGpsManager
import com.skt.Tmap.TMapGpsManager.GPS_PROVIDER
import com.skt.Tmap.TMapGpsManager.NETWORK_PROVIDER
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapView
import com.skt.Tmap.TMapView.OnClickListenerCallback
import com.skt.Tmap.poi_item.TMapPOIItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask


class HomeFragment_1 : Fragment(), TMapGpsManager.onLocationChangedCallback,
    OnClickListenerCallback {


//    private var viewProfile: View? = null
//    var pickImageFromAlbum = 0

    val db = Firebase.firestore

    lateinit var mainActivity: MainActivity
    private lateinit var auth: FirebaseAuth
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: GeoCodingInterface

    val tm = Timer()
    private lateinit var timer: TimerTask

    var picker_get_loc = ArrayList<pick_mark>()
    val docRef2 = db.collection("pickers")

    var tmapView: TMapView? = null
    var tmap: TMapGpsManager? = null

    //?????? ?????? ?????? ??????
    var startX: String? = null
    var startY: String? = null
    var endX: String? = null
    var endY: String? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fun get_picker_x_y() {
            docRef2.get().addOnSuccessListener { result ->
                for (document in result) {

                    picker_get_loc.add(
                        pick_mark(
                            document.get("addr_x").toString(),
                            document.get("addr_y").toString(),
                            document.get("name").toString()
                        )
                    )

                }

                for (i in picker_get_loc) {

                    val markerItem_i = TMapMarkerItem()

                    val tMapPoint1 =
                        TMapPoint(i.addr_x!!.toDouble(), i.addr_y!!.toDouble())

                    val bitmap = BitmapFactory.decodeResource(
                        requireContext().resources,
                        R.drawable.delivery_pin
                    )

                    markerItem_i.icon = bitmap // ?????? ????????? ??????
                    markerItem_i.setPosition(0.5f, 1.0f) // ????????? ???????????? ??????, ???????????? ??????
                    markerItem_i.tMapPoint = tMapPoint1 // ????????? ?????? ??????
                    markerItem_i.canShowCallout = true;
                    markerItem_i.calloutTitle = i.name

                    tmapView!!.addMarkerItem("markerItem_" + i, markerItem_i) // ????????? ?????? ??????

                }
            }

        }



        val v = inflater.inflate(R.layout.fragment_home_1, container, false)
        val mainAct = activity as MainActivity

        retrofit = RetrofitClient.getInstance() // retrofit ?????????
        supplementService = retrofit.create(GeoCodingInterface::class.java) // ????????? ????????????

        val maps = v.findViewById<ConstraintLayout>(R.id.TMapView)
        tmapView = TMapView(context)
        tmapView!!.setSKTMapApiKey("l7xx961891362ed44d06a261997b67e5ace6")


        tmapView!!.setZoom(17f)
        tmapView!!.setIconVisibility(true)
        tmapView!!.setMapType(TMapView.MAPTYPE_STANDARD)
        tmapView!!.setLanguage(TMapView.LANGUAGE_KOREAN)
        get_picker_x_y()
        maps.addView(tmapView)

        //?????? ????????? ??????
        fun foo() {

            if (tmap!!.location.latitude == 0.0 && tmap!!.location.latitude == null) {
                Log.d("realtime", "nulllllllllll!")

            }else{
                tmapView!!.setLocationPoint(tmap!!.location.longitude, tmap!!.location.latitude)
                tmapView!!.setCenterPoint(tmap!!.location.longitude, tmap!!.location.latitude)
            }

        }
        fun createTimerTask(): TimerTask {
            timer = timerTask { foo() }

            return timer;
        }

        fun main() {
            timer = createTimerTask()
            tm.schedule(timer, 1000, 4000);
        }
        main()

        tmapView!!.setOnClickListenerCallBack(object : OnClickListenerCallback {
            override fun onPressUpEvent(
                p0: ArrayList<TMapMarkerItem>?,
                p1: ArrayList<TMapPOIItem>?,
                point: TMapPoint?,
                pointf: PointF?
            ): Boolean {
                return true
            }

            override fun onPressEvent(
                p0: ArrayList<TMapMarkerItem>?,
                p1: ArrayList<TMapPOIItem>?,
                point: TMapPoint?,
                pointf: PointF?
            ): Boolean {
                Log.d("dd", "dd")
                timer.cancel()
                return true
            }
        })

        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            var permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(mainAct, permissions, 100)
        }

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


        val animation_1 = AnimationUtils.loadAnimation(context, R.anim.translate_up)
        val animation_2 = AnimationUtils.loadAnimation(context, R.anim.translate_down)

        val db = Firebase.firestore
        auth = Firebase.auth

        val docRef = db.collection("pick_up_request")
        val docRef2 = db.collection("pickers")
        val docRef3 = db.collection("users")
        val lo_btn = v.findViewById<Button>(R.id.lo_btn)
        val page = v.findViewById<LinearLayout>(R.id.page)
        val pick_up_btn = v.findViewById<Button>(R.id.pick_up_btn)
        val close_btn = v.findViewById<TextView>(R.id.close_btn)
        val pl_pick = v.findViewById<Button>(R.id.pick_up_item_requestBtn)

        val pick_up_item_name = v.findViewById<EditText>(R.id.pick_up_item_name)
//      val pick_up_item_img = v.findViewById<ImageView>(R.id.pick_up_item_img)
        val pick_up_item_addr_start = v.findViewById<EditText>(R.id.pick_up_item_addr_start)
        val pick_up_item_addr_start_detail =
            v.findViewById<EditText>(R.id.pick_up_item_addr_start_detail)
        val pick_up_item_addr_end = v.findViewById<EditText>(R.id.pick_up_item_addr_end)
        val pick_up_item_addr_end_detaol =
            v.findViewById<EditText>(R.id.pick_up_item_addr_end_detail)
        val pick_up_item_request = v.findViewById<EditText>(R.id.pick_up_item_request)
        val pick_up_item_cost = v.findViewById<EditText>(R.id.pick_up_item_cost)
        val web_back = v.findViewById<LinearLayout>(R.id.web_back)
        val web_layout = v.findViewById<ConstraintLayout>(R.id.web_layout)
        val find_addr_1 = v.findViewById<Button>(R.id.find_addr_1)
        val find_addr_2 = v.findViewById<Button>(R.id.find_addr_2)
        val webview = v.findViewById<WebView>(R.id.webView)
        val pick_up_item_imgBtn_1 = v.findViewById<Button>(R.id.pick_up_item_imgBtn_1)
        val pick_up_item_imgBtn_2 = v.findViewById<Button>(R.id.pick_up_item_imgBtn_2)
        val start_X = v.findViewById<TextView>(R.id.start_X)
        val start_Y = v.findViewById<TextView>(R.id.start_Y)
        val end_X = v.findViewById<TextView>(R.id.end_X)
        val end_Y = v.findViewById<TextView>(R.id.end_Y)
        //val pick_up_item_img = v.findViewById<ImageView>(R.id.pick_up_item_img)


        var webView: WebView? = null

        val handler = Handler()

        class WebAppInterface {
            @JavascriptInterface
            fun setAddress(arg1: String, arg2: String, arg3: String) {
                handler.post {
                    if (pick_up_item_addr_start.text.toString().length < 3) {
                        Log.d("?????????", pick_up_item_addr_start.text.toString().length.toString())
                        web_back.visibility = View.GONE
                        web_layout.visibility = View.GONE
                        pick_up_item_addr_start.setText(
                            String.format("(%s) %s %s", arg1, arg2, arg3)
                        )
                    } else {
                        web_back.visibility = View.GONE
                        web_layout.visibility = View.GONE
                        pick_up_item_addr_end.setText(String.format("(%s) %s %s", arg1, arg2, arg3))
                    }

                }

            }
        }
        fun input_db() {
            Log.d("**************", start_X.text.toString())

            docRef3.document(auth.currentUser!!.email.toString()).get().addOnSuccessListener {

                    document ->
                val result = makeRequestUid()

                val pick_up_request = PickUpRequest(
                    document.data!!.get("name").toString(),
                    auth.currentUser!!.email.toString(),
                    document.data!!.get("p_num").toString(),
                    auth.currentUser!!.uid,
                    pick_up_item_name.text.toString(),
                    pick_up_item_addr_start.text.toString()+"!"+ pick_up_item_addr_start_detail.text.toString(),
                    pick_up_item_addr_end.text.toString()+"!"+pick_up_item_addr_end_detaol.text.toString(),
                    pick_up_item_request.text.toString(),
                    pick_up_item_cost.text.toString(),
                    "0",
                    "0",
                    "0",
                    start_X.text.toString(),
                    start_Y.text.toString(),
                    end_X.text.toString(),
                    end_Y.text.toString()
                )
                Log.d("dddddddddddsadf", start_X.text.toString())

                val start = pick_up_item_addr_start.text.toString().substring(8, 14)
                Log.d("?????? ???2 :", start)
                val end = pick_up_item_addr_end.text.toString().substring(8, 14)
                val user_id = auth.currentUser!!.email

                docRef.document(result).set(pick_up_request)
                docRef3.document(auth.currentUser!!.email.toString())
                    .update("pick_up_list", FieldValue.arrayUnion(makeRequestUid()))
                activity?.let {
                    val intent = Intent(context, DoingRequestActivity::class.java)

                    intent.putExtra("result", result)
                    intent.putExtra("start", start)
                    intent.putExtra("id", user_id)
                    intent.putExtra("end", end)
                    startActivity(intent)
                }
            }
        }

        fun st(s1: String, s2: String) {
            Log.d("fuckckckc ", s1)
            start_X.setText(s1)
            Log.d("sexesx ", start_X.text.toString())
            start_Y.setText(s2)
        }

        fun en(s1: String, s2: String) {
            end_X.setText(s1)
            end_Y.setText(s2)
            input_db()

        }


        //????????????
        fun getSearchList_1(
            service: GeoCodingInterface,
            version: String,
            fullAddr: String,
            appKey: String
        ) {
            service.requestList(version, fullAddr, appKey).enqueue(object : Callback<start> {

                override fun onFailure(call: Call<start>, error: Throwable) {
                    Log.d("TAG", "?????? ??????: {$error}")
                }

                override fun onResponse(
                    call: Call<start>,
                    response: Response<start>
                ) {
                    Log.d("TAG", "??????")
                    response.body()?.coordinateInfo?.coordinate?.get(0)?.newLat
                    response.body()?.coordinateInfo?.coordinate?.get(0)?.newLon

                    Log.d(
                        "sangHoon :",
                        response.body()?.coordinateInfo?.coordinate?.get(0).toString()
                    )

                    if (response.body()?.coordinateInfo?.coordinate?.get(0)?.lat.toString()
                            .isNotEmpty()
                    ) {
                        startX = response.body()?.coordinateInfo?.coordinate?.get(0)?.lat.toString()
                    } else if (response.body()?.coordinateInfo?.coordinate?.get(0)?.newLat.toString()
                            .isNotEmpty()
                    ) {
                        startX =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLat.toString()
                    } else {
                        startX =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLatEntr.toString()
                    }

                    if (response.body()?.coordinateInfo?.coordinate?.get(0)?.lon.toString()
                            .isNotEmpty()
                    ) {
                        startY = response.body()?.coordinateInfo?.coordinate?.get(0)?.lon.toString()
                    } else if (response.body()?.coordinateInfo?.coordinate?.get(0)?.newLon.toString()
                            .isNotEmpty()
                    ) {
                        startY =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLon.toString()
                    } else {
                        startY =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLonEntr.toString()
                    }

                    Log.d("StartX :", startX!!)
                    Log.d("StartY :", startY!!)
                    st(startX!!, startY!!)
                    v.findViewById<TextView>(R.id.start_X).setText(startX)
                    v.findViewById<TextView>(R.id.start_Y).setText(startY)
                }
            })

        }

        fun getSearchList_2(
            service: GeoCodingInterface,
            version: String,
            fullAddr: String,
            appKey: String
        ) {
            service.requestList(version, fullAddr, appKey).enqueue(object : Callback<start> {

                override fun onFailure(call: Call<start>, error: Throwable) {
                    Log.d("TAG", "?????? ??????: {$error}")
                }

                override fun onResponse(
                    call: Call<start>,
                    response: Response<start>
                ) {


                    if (response.body()?.coordinateInfo?.coordinate?.get(0)?.lat.toString()
                            .isNotEmpty()
                    ) {
                        endX = response.body()?.coordinateInfo?.coordinate?.get(0)?.lat.toString()
                    } else if (response.body()?.coordinateInfo?.coordinate?.get(0)?.newLat.toString()
                            .isNotEmpty()
                    ) {
                        endX =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLat.toString()
                    } else {
                        endX =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLatEntr.toString()
                    }

                    if (response.body()?.coordinateInfo?.coordinate?.get(0)?.lon.toString()
                            .isNotEmpty()
                    ) {
                        endY = response.body()?.coordinateInfo?.coordinate?.get(0)?.lon.toString()
                    } else if (response.body()?.coordinateInfo?.coordinate?.get(0)?.newLon.toString()
                            .isNotEmpty()
                    ) {
                        endY =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLon.toString()
                    } else {
                        endY =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLonEntr.toString()
                    }

                    Log.d("EndX :", endX!!)
                    Log.d("EndY :", endY!!)
                    en(endX!!, endY!!)

                    v.findViewById<TextView>(R.id.end_X).setText(endX)
                    v.findViewById<TextView>(R.id.end_Y).setText(endY)
                }
            })
        }



        lo_btn.setOnClickListener {
            main()
        }

        pick_up_btn.setOnClickListener {
            pick_up_btn.visibility = View.INVISIBLE
            mainAct.HideBottomNavi(true)
            page.startAnimation(animation_1)
            page.visibility = View.VISIBLE
            timer.cancel()
        }
        close_btn.setOnClickListener {
            pick_up_btn.visibility = View.VISIBLE
            mainAct.HideBottomNavi(false)
            page.startAnimation(animation_2)
            page.visibility = View.INVISIBLE
            main()

        }

        pl_pick.setOnClickListener {

            if (pick_up_item_name.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "???????????? ??????????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (pick_up_item_addr_start.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "????????? ????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (pick_up_item_addr_end.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "????????? ????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (pick_up_item_cost.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "??????????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //???????????? ??????, ??????
            val s_1 = pick_up_item_addr_start.text.toString()
            val e_1 = pick_up_item_addr_end.text.toString()

            val s_2 = s_1.substring(7)
            val e_2 = e_1.substring(7)
            Log.d("!!!!!!!!!", start_X.text.toString())

            getSearchList_1(supplementService, "1", s_2, "l7xx961891362ed44d06a261997b67e5ace6")
            getSearchList_2(supplementService, "1", e_2, "l7xx961891362ed44d06a261997b67e5ace6")

        }
        find_addr_1.setOnClickListener {

            web_back.visibility = View.VISIBLE
            web_layout.visibility = View.VISIBLE
            pick_up_item_addr_start.setText("")
            webView = webview
            WebView.setWebContentsDebuggingEnabled(true)
            webview.addJavascriptInterface(WebAppInterface(), "gsgs")

            webView!!.apply {
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.setSupportMultipleWindows(true)
            }
            webView!!.loadUrl("https://gsgsaddr.web.app")
        }

        find_addr_2.setOnClickListener {

            web_back.visibility = View.VISIBLE
            web_layout.visibility = View.VISIBLE
            pick_up_item_addr_end.setText("")
            webView = webview
            WebView.setWebContentsDebuggingEnabled(true)
            webview.addJavascriptInterface(WebAppInterface(), "gsgs")

            webView!!.apply {
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.setSupportMultipleWindows(true)
            }
            webView!!.loadUrl("https://gsgsaddr.web.app")
        }

        web_back.setOnClickListener {
            web_back.visibility = View.GONE
            web_layout.visibility = View.GONE
        }

        pick_up_item_imgBtn_1.setOnClickListener {

        }
        pick_up_item_imgBtn_2.setOnClickListener {
            //Open Album
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
        }

        // Inflate the layout for this fragment
        return v


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun makeRequestUid(): String {
        var date = Date()
        var formatter = SimpleDateFormat("yyyy??? MM??? dd??? HH??? mm??? ss???", Locale("ko", "KR"))
        var formatted = formatter.format(date)

        return formatted

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode === 100) {
            if (grantResults.size > 0) {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) System.exit(0)
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        tmap!!.CloseGps()
        Log.d("!!!!!!!!!!!!!!", tmap!!.CloseGps().toString())
        timer.cancel()
    }

    override fun onDetach() {
        super.onDetach()
        tmap!!.CloseGps()
        Log.d("!!!!!!!!!!!!!!", tmap!!.CloseGps().toString())
        timer.cancel()
    }

    override fun onPressEvent(
        p0: ArrayList<TMapMarkerItem>?,
        p1: ArrayList<TMapPOIItem>?,
        p2: TMapPoint?,
        p3: PointF?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onPressUpEvent(
        p0: ArrayList<TMapMarkerItem>?,
        p1: ArrayList<TMapPOIItem>?,
        p2: TMapPoint?,
        p3: PointF?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onLocationChange(p0: Location) {
        Log.d("#######%%%%%%", p0.toString())
        tmapView!!.setLocationPoint(p0.longitude, p0.latitude)
        tmapView!!.setCenterPoint(p0.longitude, p0.latitude)

    }

}

