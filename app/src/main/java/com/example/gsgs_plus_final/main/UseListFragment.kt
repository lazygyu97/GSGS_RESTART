package com.example.gsgs_plus_final.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.gsgs_plus_final.R
import org.w3c.dom.Text


class UseListFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v =inflater.inflate(R.layout.fragment_use_list, container, false)

        childFragmentManager.beginTransaction().add(R.id.user_list_layout,UseListFragment_1(),"uselist").commit()

        val mainAct = activity as MainActivity
        mainAct.changeTop(false)

        val ing_btn = v.findViewById<LinearLayout>(R.id.ing_button)
        val ed_btn = v.findViewById<LinearLayout>(R.id.ed_button)

        //이용중
        ing_btn.setOnClickListener {

                val res = childFragmentManager.findFragmentById(R.id.user_list_layout).toString()

                val str = res.chunked(17)
                Log.d("지금", res)

                if(str[0] == "UseListFragment_2"){
                    childFragmentManager.beginTransaction().replace(R.id.user_list_layout,UseListFragment_1())
                    .commit()
            }else{
                false
            }
            ing_btn.setBackgroundResource(R.drawable.button_shape_2)
            ed_btn.setBackgroundResource(R.drawable.button_shape)

        }
        //이용완료
        ed_btn.setOnClickListener {
            ing_btn.setBackgroundResource(R.drawable.button_shape)
            ed_btn.setBackgroundResource(R.drawable.button_shape_2)
            childFragmentManager.beginTransaction().replace(R.id.user_list_layout,UseListFragment_2()).commit()
        }

        // Inflate the layout for this fragment
        return v
    }


}