package com.example.gsgs_plus_final.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.adapter.ChatUserAdapter
import com.example.gsgs_plus_final.chat.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var ChatUserList: ArrayList<ChatUser>
    private lateinit var adapter: ChatUserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v=inflater.inflate(R.layout.fragment_chat, container, false)
        // Inflate the layout for this fragment
        //토큰 받아오는 로직

        mAuth= FirebaseAuth.getInstance()
        mDbRef= FirebaseDatabase.getInstance().getReference()

        //adapter
        ChatUserList= ArrayList()
        adapter= ChatUserAdapter(context,ChatUserList)


        userRecyclerView=v.findViewById(R.id.ChatUserRecyclerView)

        userRecyclerView.layoutManager= LinearLayoutManager(context)
        userRecyclerView.adapter=adapter


        mDbRef.child("ChatList").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                ChatUserList.clear()

                // Log.d("여기봐 :::", snapshot.child"ChatList".value)

                for(postSnapshot in snapshot.children){

                    val currentUser= postSnapshot.getValue(ChatUser::class.java)

                    if(mAuth.currentUser?.uid != currentUser?.uid){
                        ChatUserList.add(currentUser!!)
                        //자신의 아이디는 목록에서 제거
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        return v
    }

}