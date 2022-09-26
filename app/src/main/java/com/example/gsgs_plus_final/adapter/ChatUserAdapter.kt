package com.example.gsgs_plus_final.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.chat.ChatActivity
import com.example.gsgs_plus_final.chat.ChatUser
import com.example.gsgs_plus_final.vo.User
import com.google.firebase.auth.FirebaseAuth



class ChatUserAdapter(val context: Context?, val ChatUserList: ArrayList<ChatUser>):
    RecyclerView.Adapter<ChatUserAdapter.UserViewHolder>() {
    private lateinit var mAuth: FirebaseAuth


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.chat_user_layout,parent,false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser=ChatUserList[position]
        mAuth= FirebaseAuth.getInstance()


        holder.textname.text = currentUser.uid

        holder.itemView.setOnClickListener{
            val intent = Intent(this.context, ChatActivity::class.java)
            intent.putExtra("uid",currentUser.uid)
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return ChatUserList.size
    }

    class UserViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val textname= itemView.findViewById<TextView>(R.id.txt_name)

    }
}