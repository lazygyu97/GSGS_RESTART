package com.example.gsgs_plus_final.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.main.MainActivity
import com.example.gsgs_plus_final.vo.Picker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PickerJoinActivity : AppCompatActivity() {

    //참고: lateinit으로 나중에 변수 초기화
    private lateinit var auth: FirebaseAuth





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker_join)

        val goStudy = findViewById<Button>(R.id.picker_join_btn)
        val back = findViewById<Button>(R.id.btn_back)


        val db = Firebase.firestore
        val docRef = db.collection("users")
        val docRef2 = db.collection("pickers")


        //교육받으러가기 버튼
        goStudy.setOnClickListener {

            val picker_join_pwd_confirm = findViewById<EditText>(R.id.picker_join_confirm_pwd)
            // val picker_join_img = findViewById<ImageView>(R.id.picker_join_idCard_img)
            val picker_join_addr = findViewById<EditText>(R.id.picker_join_address)
            val picker_join_bankNum = findViewById<EditText>(R.id.picker_join_bankNum)
            val picker_join_double_check_confirm = findViewById<CheckBox>(R.id.picker_join_check)

            auth = Firebase.auth
            val currentUser_email_addr = auth.currentUser!!.email.toString()
            Log.d("CurrentUser:", currentUser_email_addr)

            if(!(picker_join_double_check_confirm.isChecked)){
                Toast.makeText(this,"이용약관에 동의해주세요!",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (picker_join_pwd_confirm.text.toString().isNullOrBlank()) {
                Toast.makeText(this, "비밀번호를 입력하세요!", Toast.LENGTH_LONG).show()
//                Log.d("id:",docRef.document(currentUser_email_addr).id)
                Log.d("sss:",
                    docRef.document(currentUser_email_addr).collection("pwd").get().toString())
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
                    var picker = Picker(document.data!!.get("name").toString(),document.data!!.get("sub_name").toString(),document.data!!.get("id").toString(),
                        document.data!!.get("pwd").toString(),document.data!!.get("p_num").toString(),document.data!!.get("uid").toString(),
                        picker_join_addr.text.toString(),picker_join_bankNum.text.toString(),
                        listOf(""))

                    docRef.document(currentUser_email_addr).update("picker_flag","1")
                    docRef2.document(auth.currentUser!!.uid).set(picker)

                    val updates = hashMapOf<String,Any>(
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

        back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}