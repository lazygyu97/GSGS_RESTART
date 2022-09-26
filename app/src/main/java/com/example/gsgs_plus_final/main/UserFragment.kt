package com.example.gsgs_plus_final.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.login.LoginActivity
import com.example.gsgs_plus_final.login.MySharedPreferences
import com.example.gsgs_plus_final.request.DoingRequestActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_user, container, false)
        // Inflate the layout for this fragment


        //현재 로그인한 회원과 DB에 있는 정보를 비교하여 참이면 데이터를 마이페이지에 출력
        auth = Firebase.auth
        val db = Firebase.firestore
        val docRef = db.collection("users")
        val currentUser_email_addr = auth.currentUser!!.email.toString()
        docRef.document(currentUser_email_addr).get().addOnSuccessListener { document ->
            if (document.data!!.containsValue(auth.currentUser!!.email.toString())) {
                Log.d("data:", document.data.toString())
                var my_name = document.data!!.get("name").toString()
                var my_sub_name = document.data!!.get("sub_name").toString()
                var my_id = document.data!!.get("id").toString()
                var my_p_num = document.data!!.get("p_num").toString()
                var my_uid = document.data!!.get("uid").toString()
                v.findViewById<TextView>(R.id.My_uid).setText(my_uid)
                v.findViewById<TextView>(R.id.My_id).setText(my_id)
                v.findViewById<TextView>(R.id.My_name).setText(my_name)
                v.findViewById<TextView>(R.id.My_pnum).setText(my_p_num)
                v.findViewById<TextView>(R.id.My_sub_name).setText(my_sub_name)

            }
        }

        //고유코드 복사
        val copy= auth.currentUser!!.uid
        val clipboard: ClipboardManager = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("message",copy)
        val copy_btn = v.findViewById<Button>(R.id.btn_copy)
        val logout_btn = v.findViewById<Button>(R.id.btn_log_out)

        copy_btn.setOnClickListener {
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }
        //로그아웃 버튼눌렀을 때 저장된 아이디, 비밀번호를 삭제 후 로그인 창으로 이동
        logout_btn.setOnClickListener {
            activity?.let {
                MySharedPreferences.clearUser(requireContext())
                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        return v
    }

}