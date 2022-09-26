package com.example.gsgs_plus_final.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isDouble = false

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val db = Firebase.firestore
        val docRef = db.collection("users")


        // SharedPreferences 안에 값이 저장되어 있지 않을 때 -> Login
        if(MySharedPreferences.getUserId(this).isNullOrBlank()
            || MySharedPreferences.getUserPass(this).isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
        else { // SharedPreferences 안에 값이 저장되어 있을 때 -> MainActivity로 이동
            Toast.makeText(this, "${MySharedPreferences.getUserId(this)}님 자동 로그인 되었습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        //로그인 버튼 눌렀을때 메인화면으로 넘어갈때 나오는 토스트 메세지
        val btn_login1 = findViewById<Button>(R.id.btn_login)
        btn_login1.setOnClickListener {

            val login_id = findViewById<EditText>(R.id.edit_id)
            val login_pwd = findViewById<EditText>(R.id.edit_pw)

            //공백 ID처리
            if (login_id.text.toString().isNullOrBlank()) {
                Toast.makeText(this, "아이디를 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //공백 PW처리
            if (login_pwd.text.toString().isNullOrBlank()) {
                Toast.makeText(this, "비밀번호를 입력해주세요! ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //로그인 로직 검증 (성공or실패)
            auth.signInWithEmailAndPassword(login_id.text.toString(), login_pwd.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //로그인 성공

                        // 로그인 성공 시 아이디와 비밀번호 저장
                        MySharedPreferences.setUserId(this, login_id.text.toString())
                        MySharedPreferences.setUserPass(this, login_pwd.text.toString())

                        Log.d(TAG, "Success")
                        val user = auth.currentUser
                        Toast.makeText(this, "메인화면으로 이동합니다. ", Toast.LENGTH_SHORT).show()
                        docRef.document(auth.currentUser!!.email.toString()).update("doing_flag","0")
                        //로그인 버튼을 누르면 메인화면으로 넘어가는 이벤트
                        var intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)


                    } else {
                        //로그인 실패
                        val login_fail_query = docRef.document(login_id.text.toString()).get()

                        login_fail_query.addOnSuccessListener {

                                document -> if(document.exists()){

                            Log.d("loginFail:",docRef.document(login_id.text.toString()).toString())
                            Toast.makeText(this, "패스워드가 틀립니다! ", Toast.LENGTH_SHORT).show()


                        }else{

                            Toast.makeText(this, "등록된 ID가 아닙니다! ", Toast.LENGTH_SHORT).show()


                        }
                        }


                    }//else

                }
        }//onclick





        //회원가입 글자를 누르면 회원가입화면으로 넘어가는 이벤트
        val start_join1 = findViewById<TextView>(R.id.start_join)
        start_join1.setOnClickListener {

            var intent2 = Intent(this, JoinActivity::class.java)
            startActivity(intent2)
        }

        //회원가입 글자를 누르면 ID/PW찾기 화면으로 넘어가는 이벤트
        val find_IdPw1 = findViewById<TextView>(R.id.text_IdPwfind)
        find_IdPw1.setOnClickListener {

            var intent2 = Intent(this, FindActivity::class.java)
            startActivity(intent2)
        }

    }

    override fun onBackPressed() {

        if(isDouble == true){
            finish()
        }

        isDouble = true
        Toast.makeText(this, "종료하시려면 더블클릭", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable{
            isDouble = false
        }, 1000)
    }

}