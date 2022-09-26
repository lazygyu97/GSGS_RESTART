package com.example.gsgs_plus_final.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.gsgs_plus_final.R


class PwFindFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v =inflater.inflate(R.layout.fragment_pw_find, container, false)

        val find = v.findViewById<Button>(R.id.btn_Pw_Find)

        find.setOnClickListener {
            activity?.let {
                var intent = Intent(context, PwFindFinishActivity::class.java)
                startActivity(intent)
            }
        }


        // Inflate the layout for this fragment
        return v
    }



}