package com.example.gsgs_plus_final.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gsgs_plus_final.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sentButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    var receiverRoom: String?= null
    var senderRoom: String?= null
    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.gsgs_plus_final.R.layout.activity_chat)

        val receiverUid = intent. getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().reference


        senderRoom=receiverUid + senderUid
        receiverRoom =senderUid + receiverUid



        chatRecyclerView = findViewById(com.example.gsgs_plus_final.R.id.chatRecyclerView)
        messageBox=findViewById(com.example.gsgs_plus_final.R.id.messageBox)
        sentButton= findViewById(com.example.gsgs_plus_final.R.id.sentButton)

        messageList = ArrayList()
        messageAdapter= MessageAdapter(this,messageList)

        chatRecyclerView.layoutManager= LinearLayoutManager(this)
        chatRecyclerView.adapter=messageAdapter

        //리사이클러뷰로 데이터를 추가하는 로직
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(datasnapshot: DataSnapshot) {
                    messageList.clear()

                    for(postSnapshot in datasnapshot.children){
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        // 데이터베이스에 메세지 추가하기
        sentButton.setOnClickListener{
            if (messageBox.text.toString().isEmpty() || messageBox.text.toString().isBlank())
            {
                messageBox.setText("")
            }
            else {
                val message = messageBox.text.toString()
                val messageObject = Message(message, senderUid)
                mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                            .setValue(messageObject)
                    }
                messageBox.setText("")
            }
        }
    }
}