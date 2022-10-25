package com.example.gsgs_plus_final.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.adapter.UseListAdapter
import com.example.gsgs_plus_final.pickUp.BeforePickUpActivity
import com.example.gsgs_plus_final.pickUp.DoingPickUpActivity
import com.example.gsgs_plus_final.using.UsingCheckActivity
import com.example.gsgs_plus_final.using.UsingCheckActivity_2
import com.example.gsgs_plus_final.vo.LoadingDialog
import com.example.gsgs_plus_final.vo.pick_list
import com.example.gsgs_plus_final.vo.pick_list2
import com.example.tmaptest.data.start
import com.example.tmaptest.retrofit.GeoCodingInterface
import com.example.tmaptest.retrofit.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skt.Tmap.TMapGpsManager
import com.skt.Tmap.TMapView
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask


class UseListFragment_1 : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: GeoCodingInterface

    val tm = Timer()
    private lateinit var timer: TimerTask

    var tmapView: TMapView? = null
    var tmap: TMapGpsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        retrofit = RetrofitClient.getInstance() // retrofit 초기화
        supplementService = retrofit.create(GeoCodingInterface::class.java) // 서비스 가져오기

        val v = inflater.inflate(R.layout.fragment_use_list1, container, false)
        var dialog = LoadingDialog(requireContext())

        tmapView = TMapView(context)
        tmapView!!.setSKTMapApiKey("l7xx961891362ed44d06a261997b67e5ace6")

        tmap = TMapGpsManager(context)
        if (Build.DEVICE.substring(0, 3) == "emu") {
            Log.d("----device: ", "이것은 에뮬레이터")
            tmap!!.provider = TMapGpsManager.GPS_PROVIDER
        } else {
            Log.d("----device: ", "이것은 스마트폰!")
            tmap!!.provider = TMapGpsManager.NETWORK_PROVIDER
        }
        tmap!!.minTime = 1000
        tmap!!.OpenGps()

        val mainAct = activity as MainActivity
        mainAct.changeTop(false)

        val ing_btn = v.findViewById<LinearLayout>(R.id.ing_button)
        val ed_btn = v.findViewById<LinearLayout>(R.id.ed_button)
        val mLayoutManager = LinearLayoutManager(context);
        val list = v.findViewById<RecyclerView>(R.id.use_list_view)
        list.setLayoutManager(mLayoutManager);

        val db = Firebase.firestore
        auth = Firebase.auth
        val docRef = db.collection("pick_up_request")
        val docRef2 = db.collection("users")
        val docRef3 = db.collection("pickers")
        val pickList = ArrayList<pick_list2>()




        docRef2.document(auth.currentUser!!.email.toString()).get().addOnSuccessListener { task ->

            if (task.data!!.get("doing_flag") == "0") {


                docRef.whereEqualTo("pick_up_check_flag", "0").get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {

                            if (document.data.get("uid") == auth.currentUser!!.uid) {
                                val start_addr: String =
                                    document.data["pick_up_item_addr_start"].toString()
                                val end_addr: String =
                                    document.data["pick_up_item_addr_end"].toString()

//                                val start = start_addr.substring(8, 14)
//                                val end = end_addr.substring(8, 14)

                                val request_cost: String =
                                    document.data["pick_up_item_cost"].toString()
                                val document_id: String = document.id
                                val pick_up_flag: String =
                                    document.data["pick_up_check_flag"].toString()
                                val item_name: String =
                                    document.data["pick_up_item_name"].toString()

                                pickList.apply {

                                    add(
                                        pick_list2(
                                            item_name,
                                            start_addr,
                                            end_addr,
                                            request_cost,
                                            document_id,
                                            pick_up_flag
                                        )
                                    )

                                }//apply
                            } //if
                        }//for
                        Log.d("잘왔어요~0", "잘왔네요~0")
                        val adapter = UseListAdapter(pickList)
                        list.adapter = adapter

                        adapter.setOnItemClickListener(object : UseListAdapter.OnItemClickListener {
                            override fun onItemClick(data: pick_list2, pos: Int) {
                                val intent = Intent(context, UsingCheckActivity::class.java)
                                intent.putExtra("data", data.document_id)
                                startActivity(intent)
                            }
                        })
                    }.addOnFailureListener { exception ->
                        Log.d("오류다 오류~0",exception.toString())
                    }


                docRef.whereEqualTo("pick_up_check_flag", "1").get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {

                            if (document.data.get("uid") == auth.currentUser!!.uid) {
                                val start_addr: String =
                                    document.data["pick_up_item_addr_start"].toString()
                                val end_addr: String =
                                    document.data["pick_up_item_addr_end"].toString()

//                                        val start = start_addr.substring(8, 14)
//                                        val end = end_addr.substring(8, 14)

                                val request_cost: String =
                                    document.data["pick_up_item_cost"].toString()
                                val document_id: String = document.id
                                val pick_up_flag: String =
                                    document.data["pick_up_check_flag"].toString()
                                val item_name: String =
                                    document.data["pick_up_item_name"].toString()
                                pickList.apply {

                                    add(
                                        pick_list2(
                                            item_name,
                                            start_addr,
                                            end_addr,
                                            request_cost,
                                            document_id,
                                            pick_up_flag
                                        )
                                    )

                                }//apply
                            } //if
                        }//for
                        Log.d("잘왔어요~1", "잘왔네요~1")
                        val adapter = UseListAdapter(pickList)
                        list.adapter = adapter
                        adapter.setOnItemClickListener(object : UseListAdapter.OnItemClickListener {
                            override fun onItemClick(data: pick_list2, pos: Int) {
                                val intent = Intent(context, UsingCheckActivity::class.java)
                                intent.putExtra("data", data.document_id)
                                startActivity(intent)
                            }
                        })
                    }.addOnFailureListener { exception ->
                        Log.d("오류다 오류~1",exception.toString())
                    }
                docRef.whereEqualTo("pick_up_check_flag", "2").get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {

                            if (document.data.get("uid") == auth.currentUser!!.uid) {
                                val start_addr: String =
                                    document.data["pick_up_item_addr_start"].toString()
                                val end_addr: String =
                                    document.data["pick_up_item_addr_end"].toString()

//                                        val start = start_addr.substring(8, 14)
//                                        val end = end_addr.substring(8, 14)

                                val request_cost: String =
                                    document.data["pick_up_item_cost"].toString()
                                val document_id: String = document.id
                                val pick_up_flag: String =
                                    document.data["pick_up_check_flag"].toString()
                                val item_name: String =
                                    document.data["pick_up_item_name"].toString()
                                pickList.apply {

                                    add(
                                        pick_list2(
                                            item_name,
                                            start_addr,
                                            end_addr,
                                            request_cost,
                                            document_id,
                                            pick_up_flag
                                        )
                                    )

                                }//apply
                            } //if
                        }//for
                        Log.d("잘왔어요~3", "잘왔네요~3")
                        val adapter = UseListAdapter(pickList)
                        list.adapter = adapter
                        adapter.setOnItemClickListener(object : UseListAdapter.OnItemClickListener {
                            override fun onItemClick(data: pick_list2, pos: Int) {
                                val intent = Intent(context, UsingCheckActivity_2::class.java)
                                intent.putExtra("data", data.document_id)
                                startActivity(intent)
                            }
                        })
                    }.addOnFailureListener { exception ->
                        Log.d("오류다 오류~2",exception.toString())
                    }

            } else {

                docRef.whereEqualTo("pick_up_check_flag", "1").get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {

                            if (document.data.get("uid_2") == auth.currentUser!!.uid) {
                                val start_addr: String =
                                    document.data["pick_up_item_addr_start"].toString()
                                val end_addr: String =
                                    document.data["pick_up_item_addr_end"].toString()

//                                val start = start_addr.substring(8, 14)
//                                val end = end_addr.substring(8, 14)

                                val request_cost: String =
                                    document.data["pick_up_item_cost"].toString()
                                val document_id: String = document.id
                                val pick_up_flag: String =
                                    document.data["pick_up_check_flag"].toString()
                                val item_name: String =
                                    document.data["pick_up_item_name"].toString()
                                pickList.apply {

                                    add(
                                        pick_list2(
                                            item_name,
                                            start_addr,
                                            end_addr,
                                            request_cost,
                                            document_id,
                                            pick_up_flag
                                        )
                                    )

                                }
                            }

                        }
                        Log.d("잘왔어요~4", "잘왔네요~4")
                        val adapter = UseListAdapter(pickList)
                        list.adapter = adapter

                        adapter.setOnItemClickListener(object : UseListAdapter.OnItemClickListener {
                            override fun onItemClick(data: pick_list2, pos: Int) {


                                val intent =
                                    Intent(context, BeforePickUpActivity::class.java)
                                intent.putExtra("data", data.document_id)
                                intent.putExtra("MyLocation_lat2",
                                    tmap!!.location.latitude.toString())
                                intent.putExtra(
                                    "MyLocation_lon2",
                                    tmap!!.location.longitude.toString()
                                )

                                startActivity(intent)
                            }


                        })
                    }.addOnFailureListener { exception ->
                        Log.d("오류다 오류~3",exception.toString())
                    }

                docRef.whereEqualTo("pick_up_check_flag", "2").get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {

                            if (document.data.get("uid_2") == auth.currentUser!!.uid) {
                                val start_addr: String =
                                    document.data["pick_up_item_addr_start"].toString()
                                val end_addr: String =
                                    document.data["pick_up_item_addr_end"].toString()

//                                val start = start_addr.substring(8, 14)
//                                val end = end_addr.substring(8, 14)

                                val request_cost: String =
                                    document.data["pick_up_item_cost"].toString()
                                val document_id: String = document.id
                                val pick_up_flag: String =
                                    document.data["pick_up_check_flag"].toString()
                                val item_name: String =
                                    document.data["pick_up_item_name"].toString()
                                pickList.apply {

                                    add(
                                        pick_list2(
                                            item_name,
                                            start_addr,
                                            end_addr,
                                            request_cost,
                                            document_id,
                                            pick_up_flag
                                        )
                                    )

                                }
                            }

                        }
                        Log.d("잘왔어요~5", "잘왔네요~5")
                        val adapter = UseListAdapter(pickList)
                        list.adapter = adapter

                        adapter.setOnItemClickListener(object : UseListAdapter.OnItemClickListener {
                            override fun onItemClick(data: pick_list2, pos: Int) {


                                val intent =
                                    Intent(context, DoingPickUpActivity::class.java)
                                intent.putExtra("data", data.document_id)


                                startActivity(intent)
                            }


                        })
                    }.addOnFailureListener { exception ->
                        Log.d("오류다 오류~4",exception.toString())
                    }

            }

        }

        //현재 실시간 위치
        fun foo() {
            Log.d("realtime", "wow3")
            Log.d("check!@#33 : ", tmap!!.location.latitude.toString())
            if (tmap!!.location.latitude !== 0.0) {
                timer.cancel()
                dialog.dismiss()

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


        // Inflate the layout for this fragment
        return v
    }

    override fun onDetach() {
        super.onDetach()
        tmap!!.CloseGps()
        timer.cancel()
    }


}