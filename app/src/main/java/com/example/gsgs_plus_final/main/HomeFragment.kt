package com.example.gsgs_plus_final.main

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.FragmentTransaction
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.login.PickerJoinActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_home, container, false)

        childFragmentManager.beginTransaction().add(R.id.home_layout, HomeFragment_1(), "home")
            .commit()


        val pl_btn = v.findViewById<LinearLayout>(R.id.pl_button)
        val do_btn = v.findViewById<LinearLayout>(R.id.do_button)

        val db = Firebase.firestore
        val docRef = db.collection("users")

        auth = Firebase.auth
        val currentUser_email_addr = auth.currentUser!!.email.toString()


        fun ask_picker() {
            val ask_pick = AlertDialog.Builder(context)
            ask_pick
                .setMessage("아직 배송회원이 아니시군요?\n가입하시겠습니까?")
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                        activity?.let{
                            val intent = Intent(context, PickerJoinActivity::class.java)
                            startActivity(intent)
                        }

                    })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                        childFragmentManager.beginTransaction()
                            .replace(R.id.home_layout, HomeFragment_1()).commit()
                        pl_btn.setBackgroundResource(R.drawable.button_shape_2)
                        do_btn.setBackgroundResource(R.drawable.button_shape)
                    })
            // 다이얼로그를 띄워주기
            ask_pick.show()
        }

        //픽업 해주세요
        pl_btn.setOnClickListener {

            docRef.document(auth.currentUser!!.email.toString()).update("doing_flag","0")

            val res = childFragmentManager.findFragmentById(R.id.home_layout).toString()
            Log.d("지금", res)
            val str = res.chunked(14)
            if (str[0] == "HomeFragment_2") {
                childFragmentManager.beginTransaction().replace(R.id.home_layout, HomeFragment_1())
                    .commit()
            } else {
                false
            }
            pl_btn.setBackgroundResource(R.drawable.button_shape_2)
            do_btn.setBackgroundResource(R.drawable.button_shape)



        }

        //픽업 할게요
        do_btn.setOnClickListener {

            docRef.document(currentUser_email_addr).get().addOnSuccessListener {
                    document -> if(document.data!!.get("picker_flag") != "1"){
                ask_picker()
            }else {
                docRef.document(auth.currentUser!!.email.toString()).update("doing_flag","1")
            }
            }



            pl_btn.setBackgroundResource(R.drawable.button_shape)
            do_btn.setBackgroundResource(R.drawable.button_shape_2)
            childFragmentManager.beginTransaction()
                .replace(R.id.home_layout, HomeFragment_2()).commit();




        }

        // Inflate the layout for this fragment
        return v


    }

}