package com.example.gsgs_plus_final.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.gsgs_plus_final.R

class PwFindFinishActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pw_find_finish)

        //완료버튼을 누르면 비밀번호 재설정이 완료되었다고 토스트메세지가 뜨는 이벤트.
        val pwSuccess = findViewById<Button>(R.id.btn_pwSuccess)
        pwSuccess.setOnClickListener {
            Toast.makeText(this, "비밀번호 재설정이 완료되었습니다. ", Toast.LENGTH_SHORT).show()
        }

        //로그인 화면으로 버튼을 누르면 다시 로그인 화면으로 가게되는 이벤트
        val backToLogin = findViewById<Button>(R.id.btn_backToLogin)
        backToLogin.setOnClickListener {

            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}