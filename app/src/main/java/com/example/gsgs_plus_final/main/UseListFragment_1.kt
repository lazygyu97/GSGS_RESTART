package com.example.gsgs_plus_final.main

import android.os.Bundle
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
import com.example.gsgs_plus_final.vo.pick_list
import com.example.gsgs_plus_final.vo.pick_list2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class UseListFragment_1 : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_use_list1, container, false)

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
                docRef.whereEqualTo("pick_up_check_flag", "1").get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {

                            if (document.data.get("uid") == auth.currentUser!!.uid) {
                                val start_addr: String =
                                    document.data["pick_up_item_addr_start"].toString()
                                val end_addr: String =
                                    document.data["pick_up_item_addr_end"].toString()

                                val start = start_addr.substring(8, 14)
                                val end = end_addr.substring(8, 14)

                                val request_cost: String =
                                    document.data["pick_up_item_cost"].toString()
                                val document_id: String = document.id
                                val pick_up_flag: String =
                                    document.data["pick_up_check_flag"].toString()

                                pickList.apply {

                                    add(pick_list2(start,
                                        end,
                                        request_cost,
                                        document_id,
                                        pick_up_flag))

                                }
                            }
                        }
                        val adapter = UseListAdapter(pickList)
                        list.adapter = adapter
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

                                val start = start_addr.substring(8, 14)
                                val end = end_addr.substring(8, 14)

                                val request_cost: String =
                                    document.data["pick_up_item_cost"].toString()
                                val document_id: String = document.id
                                val pick_up_flag: String =
                                    document.data["pick_up_check_flag"].toString()

                                pickList.apply {

                                    add(pick_list2(start,
                                        end,
                                        request_cost,
                                        document_id,
                                        pick_up_flag))

                                }
                            }

                        }
                        val adapter = UseListAdapter(pickList)
                        list.adapter = adapter
                    }

            }

        }



        // Inflate the layout for this fragment
        return v
    }


}