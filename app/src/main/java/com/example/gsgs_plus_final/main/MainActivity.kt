package com.example.gsgs_plus_final.main

import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.login.PickerJoinActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skt.Tmap.TMapGpsManager
import com.skt.Tmap.TMapView


class MainActivity : AppCompatActivity(){

    private lateinit var auth: FirebaseAuth

    // 픽업 요청서 클릭시 하단바 없애는 함수 ( 다른 프래그먼트에서 호출하여 사용한다 )
    fun HideBottomNavi(state: Boolean) {
        if (state) bottomNavigationView.visibility =
            View.GONE else bottomNavigationView.visibility = View.VISIBLE
        if (state) top.visibility = View.VISIBLE else top.visibility = View.INVISIBLE

    }

    // 상단 메뉴바 강조 효과 함수
    fun changeTop(state: Boolean) {
        if (state) {
            switch.visibility = View.INVISIBLE
            logo.visibility = View.VISIBLE
        } else {
            switch.visibility = View.VISIBLE
            logo.visibility = View.INVISIBLE
        }
    }

    // 하단바에 요소 클릭시 프래그먼트를 바꿔주는 함수
    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(frame.id, fragment)
            .commit()
    }

    // 픽업활동 스위치 작동시 픽커여부를 판단하여 배송회원 가입을 권유하는 알림창 함수
    private fun ask_picker() {
        val ask_pick = AlertDialog.Builder(this)
        ask_pick
            .setMessage("아직 배송회원이 아니시군요?\n가입하시겠습니까?")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val intent = Intent(this, PickerJoinActivity::class.java)
                    startActivity(intent)
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id ->
                    pick_up.setChecked(false)
                })
        // 다이얼로그를 띄워주기
        ask_pick.show()
    }

    // 홈 프래그먼트로 전환시 카카오 지도 api가 두번 호출되면서 에러 발생을 막는 함수
    private fun homeReplace() {
        Log.d("현재", supportFragmentManager.findFragmentById(frame.id).toString())
        val res = supportFragmentManager.findFragmentById(frame.id).toString()
        val str = res.chunked(3)

        if (str[0] == "Use") {
            replaceFragment(HomeFragment())
        } else if (str[0] == "Cha") {
            replaceFragment(HomeFragment())
        } else if (str[0] == "Not") {
            replaceFragment(HomeFragment())
        } else {
            false
        }
    }


    private val top: FrameLayout by lazy {
        findViewById(R.id.topPanel)
    }
    private val pick_up: Switch by lazy {
        findViewById(R.id.sw_activtiy_pick)
    }
    private val frame: FrameLayout by lazy {
        findViewById(R.id.frame)
    }
    private val switch: FrameLayout by lazy {
        findViewById(R.id.sw_pick_up)
    }
    private val logo: FrameLayout by lazy {
        findViewById(R.id.logo)
    }
    private val bottomNavigationView: BottomNavigationView by lazy {
        findViewById(R.id.navigationView)
    }
    private val user: ImageButton by lazy {
        findViewById(R.id.btn_user)
    }
    private val notice: ImageButton by lazy {
        findViewById(R.id.btn_notice)
    }





    var tmapView: TMapView? = null
    var tmap: TMapGpsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Firebase.firestore
        val docRef = db.collection("users")

        auth = Firebase.auth
        val currentUser_email_addr = auth.currentUser!!.email.toString()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(frame.id, HomeFragment())
            .commit()



        pick_up.setOnCheckedChangeListener { _, onSwitch ->
            if (onSwitch) {
                docRef.document(currentUser_email_addr).get().addOnSuccessListener { document ->
                    if (document.data!!.get("picker_flag") != "1") {
                        ask_picker()
                    }
                }
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    changeTop(false)
                    homeReplace()
                    true
                }
                R.id.action_chat -> {
                    changeTop(false)
                    replaceFragment(ChatFragment())
                    true
                }
                R.id.action_use_list -> {
                    changeTop(false)
                    replaceFragment(UseListFragment())
                    true
                }
                else -> false

            }
        }
        user.setOnClickListener {
            replaceFragment(UserFragment())
            changeTop(true)
        }
        notice.setOnClickListener {
            replaceFragment(NoticeFragment())
            changeTop(true)
        }


    }



}