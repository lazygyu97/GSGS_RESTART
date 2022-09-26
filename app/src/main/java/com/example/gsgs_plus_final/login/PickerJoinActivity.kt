package com.example.gsgs_plus_final.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.main.MainActivity
import com.example.gsgs_plus_final.vo.Picker
import com.example.tmaptest.data.start
import com.example.tmaptest.retrofit.GeoCodingInterface
import com.example.tmaptest.retrofit.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*

class PickerJoinActivity : AppCompatActivity() {

    //참고: lateinit으로 나중에 변수 초기화
    private lateinit var auth: FirebaseAuth

    //도로명 주소 찾기 웹뷰
    var webView: WebView? = null
    val handler = Handler()
    private lateinit var supplementService: GeoCodingInterface
    var addr_x: String? = null
    var addr_y: String? = null

    private lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker_join)

        val goStudy = findViewById<Button>(R.id.picker_join_btn)
        val back = findViewById<Button>(R.id.btn_back)
        val picker_join_find_addr = findViewById<Button>(R.id.find_picker_addr)
        val picker_join_pwd_confirm = findViewById<EditText>(R.id.picker_join_confirm_pwd)
        // val picker_join_img = findViewById<ImageView>(R.id.picker_join_idCard_img)
        val picker_join_addr = findViewById<EditText>(R.id.picker_join_address)
        val picker_join_bankNum = findViewById<EditText>(R.id.picker_join_bankNum)
        val picker_join_double_check_confirm = findViewById<CheckBox>(R.id.picker_join_check)
        val webview = findViewById<WebView>(R.id.webView)
        val web_layout = findViewById<ConstraintLayout>(R.id.web_layout)
        val web_back = findViewById<LinearLayout>(R.id.web_back)

        val db = Firebase.firestore
        val docRef = db.collection("users")
        val docRef2 = db.collection("pickers")


        retrofit = RetrofitClient.getInstance() // retrofit 초기화
        supplementService = retrofit.create(GeoCodingInterface::class.java) // 서비스 가져오기

        //교육받으러가기 버튼
        goStudy.setOnClickListener {


            auth = Firebase.auth
            val currentUser_email_addr = auth.currentUser!!.email.toString()
            Log.d("CurrentUser:", currentUser_email_addr)

            if (!(picker_join_double_check_confirm.isChecked)) {
                Toast.makeText(this, "이용약관에 동의해주세요!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (picker_join_pwd_confirm.text.toString().isNullOrBlank()) {
                Toast.makeText(this, "비밀번호를 입력하세요!", Toast.LENGTH_LONG).show()
//                Log.d("id:",docRef.document(currentUser_email_addr).id)
                Log.d(
                    "sss:",
                    docRef.document(currentUser_email_addr).collection("pwd").get().toString()
                )
                return@setOnClickListener
            }

//            if(picker_join_img.toString().isNullOrBlank()){
//                Toast.makeText(this, "이미지를 업로드 해주세요!", Toast.LENGTH_LONG).show()
//                return@setOnClickListener
//
//            }

            if (picker_join_addr.text.toString().isNullOrBlank()) {
                Toast.makeText(this, "주소를 입력해주세요!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (picker_join_bankNum.text.toString().isNullOrBlank()) {
                Toast.makeText(this, "계좌번호를 입력해주세요!", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }


            //비밀번호 인증 로직
            docRef.document(currentUser_email_addr).get().addOnSuccessListener { document ->
                if (document.data!!.containsValue(picker_join_pwd_confirm.text.toString())) {


                    Log.d("data:", document.data.toString())
                    var picker = Picker(
                        document.data!!.get("name").toString(),
                        document.data!!.get("sub_name").toString(),
                        document.data!!.get("id").toString(),
                        document.data!!.get("pwd").toString(),
                        document.data!!.get("p_num").toString(),
                        document.data!!.get("uid").toString(),
                        picker_join_addr.text.toString(),
                        picker_join_bankNum.text.toString(),
                        listOf(""),
                        addr_x.toString(),
                        addr_y.toString()
                    )

                    docRef.document(currentUser_email_addr).update("picker_flag", "1")
                    docRef2.document(auth.currentUser!!.uid).set(picker)

                    val updates = hashMapOf<String, Any>(
                        "pick_up_list" to FieldValue.delete()
                    )
                    docRef2.document(auth.currentUser!!.uid).update(updates)


                    Toast.makeText(this, "픽커 가입 성공!\n메인화면으로 이동합니다. ", Toast.LENGTH_SHORT).show()
                    //로그인 버튼을 누르면 메인화면으로 넘어가는 이벤트
                    var intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)


                } else {
                    Toast.makeText(this, "비밀번호 인증실패!", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
            }


        }
        fun getSearchList_1(
            service: GeoCodingInterface,
            version: String,
            fullAddr: String,
            appKey: String
        ) {
            service.requestList(version, fullAddr, appKey).enqueue(object : Callback<start> {

                override fun onFailure(call: Call<start>, error: Throwable) {
                    Log.d("TAG", "실패 원인: {$error}")
                }

                override fun onResponse(
                    call: Call<start>,
                    response: Response<start>
                ) {
                    Log.d("TAG", "성공")
                    response.body()?.coordinateInfo?.coordinate?.get(0)?.newLat
                    response.body()?.coordinateInfo?.coordinate?.get(0)?.newLon

                    Log.d(
                        "picker_find_addr :",
                        response.body()?.coordinateInfo?.coordinate?.get(0).toString()
                    )

                    if (response.body()?.coordinateInfo?.coordinate?.get(0)?.lat.toString()
                            .isNotEmpty()
                    ) {
                        addr_x = response.body()?.coordinateInfo?.coordinate?.get(0)?.lat.toString()
                    } else if (response.body()?.coordinateInfo?.coordinate?.get(0)?.newLat.toString()
                            .isNotEmpty()
                    ) {
                        addr_x =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLat.toString()
                    } else {
                        addr_x =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLatEntr.toString()
                    }

                    if (response.body()?.coordinateInfo?.coordinate?.get(0)?.lon.toString()
                            .isNotEmpty()
                    ) {
                        addr_y = response.body()?.coordinateInfo?.coordinate?.get(0)?.lon.toString()
                    } else if (response.body()?.coordinateInfo?.coordinate?.get(0)?.newLon.toString()
                            .isNotEmpty()
                    ) {
                        addr_y =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLon.toString()
                    } else {
                        addr_x =
                            response.body()?.coordinateInfo?.coordinate?.get(0)?.newLonEntr.toString()
                    }

                    Log.d("StartX :", addr_x!!)
                    Log.d("StartY :", addr_y!!)

                }
            })

        }

        back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        class WebAppInterface {
            @JavascriptInterface
            fun setAddress(arg1: String, arg2: String, arg3: String) {
                handler.post {
                    if (picker_join_addr.text.toString().length < 3) {
                        Log.d("여기봐", picker_join_addr.text.toString().length.toString())
                        web_back.visibility = View.GONE
                        web_layout.visibility = View.GONE
                        picker_join_addr.setText(
                            String.format("(%s) %s %s", arg1, arg2, arg3)
                        )

                        val addr_1 = picker_join_addr.text.toString()
                        val addr_2 = addr_1.substring(7)

                        getSearchList_1(supplementService, "1", addr_2, "l7xx961891362ed44d06a261997b67e5ace6")


                    } else {
                        web_back.visibility = View.GONE
                        web_layout.visibility = View.GONE
                        picker_join_addr.setText(String.format("(%s) %s %s", arg1, arg2, arg3))

                        val addr_1 = picker_join_addr.text.toString()
                        val addr_2 = addr_1.substring(7)

                        getSearchList_1(supplementService, "1", addr_2, "l7xx961891362ed44d06a261997b67e5ace6")


                    }

                }

            }
        }


        picker_join_find_addr.setOnClickListener {
            web_back.visibility = View.VISIBLE
            web_layout.visibility = View.VISIBLE
            picker_join_addr.setText("")
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



    }
}