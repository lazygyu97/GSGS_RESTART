package com.example.gsgs_plus_final.login

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.databinding.ActivityMainBinding
import com.example.gsgs_plus_final.vo.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.auth.FirebaseAppCheckTokenProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class JoinActivity : AppCompatActivity()  {

    //실시간 디비 받기위한 디비 초기화
//    private lateinit var mDbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    var double_check_confirm = "no"
    private var phone_auth  = false
    var verificationIdAll : String =""

    override fun onCreate(savedInstanceState: Bundle?) {




        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)


        val db = Firebase.firestore
        val docRef = db.collection("users")

        auth = Firebase.auth
        val double_check = findViewById<Button>(R.id.btn_doubleCheck)
        val join_button = findViewById<Button>(R.id.btn_join)

        textWatcher()

        //SMS발송 버튼 클릭

        val p_num_send = findViewById<Button>(R.id.join_pNum_sendBtn).setOnClickListener {


            val join_pNum_insert_one = findViewById<EditText>(R.id.join_pNum_insert_one)
            val join_pNum_insert_two = findViewById<EditText>(R.id.join_pNum_insert_two)
            val join_pNum_insert_three = findViewById<EditText>(R.id.join_pNum_insert_three)

            val join_pNum_insert = join_pNum_insert_one.text.toString()+"-"+join_pNum_insert_two.text.toString()+"-"+join_pNum_insert_three.text.toString()

            if(join_pNum_insert.isNullOrBlank()){

                Toast.makeText(this,"핸드폰 번호를 형식에 맞게 기입하세요!",Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }

            val callbacks = object :  PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG,"call onVerificationCompleted")

                }

                //인증번호 발송 실패
                override fun onVerificationFailed(e: FirebaseException) {
                    Log.w(TAG, "onVerificationFailed", e)

                    if (e is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this@JoinActivity,"인증번호 전송에 실패했습니다.",Toast.LENGTH_LONG).show()
                        Log.w(TAG, "FirebaseAuthInvalidCredentialsException", e)
                        // Invalid request
                    } else if (e is FirebaseTooManyRequestsException) {
                        Toast.makeText(this@JoinActivity,"인증번호 전송에 실패했습니다.",Toast.LENGTH_LONG).show()
                        Log.w(TAG, "FirebaseTooManyRequestsException", e)
                        // The SMS quota for the project has been exceeded
                    }
                }
                //인증번호 발송 후
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {

                    Toast.makeText(this@JoinActivity,"인증번호가 전송되었습니다.",Toast.LENGTH_LONG).show()
                    Log.d(TAG, "onCodeSent:$verificationId")
                    verificationIdAll = verificationId


                }

            }

            Log.d("Phone!!",join_pNum_insert)
            //인증번호를 위한 객체들
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+82"+join_pNum_insert)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)

        }


        //인증 절차 로직
        fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

            auth.signInWithCredential(credential).addOnCompleteListener(this){
                    task -> if(task.isSuccessful) {
                phone_auth = true
                Log.d("PhoneSuccess:", "Confirm")
                Toast.makeText(this,"인증되었습니다.",Toast.LENGTH_LONG).show()

                auth.currentUser!!.delete().addOnCompleteListener {
                        task -> if(task.isSuccessful){
                    Log.d("PhoneUserDelete","Complete")
                    auth.signOut()

                }

                }

            }else{
                Toast.makeText(this,"인증번호가 다릅니다!",Toast.LENGTH_LONG).show()
                Log.d("PhoneFail:","Fail!!")

            }
            }
        }

        //인증번호 확인 버튼 클릭
        val join_sendMsg_confirmBtn = findViewById<Button>(R.id.join_pNum_sendNum_confirmBtn).setOnClickListener {
            val join_pNum_sendNum_confirm = findViewById<EditText>(R.id.join_pNum_sendNum_confirm)

            if(join_pNum_sendNum_confirm.text.toString().isNullOrBlank()){
                Toast.makeText(this,"인증번호를 입력하세요!",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.d("confirmValue:",join_pNum_sendNum_confirm.text.toString())


            val credential = PhoneAuthProvider.getCredential(verificationIdAll, join_pNum_sendNum_confirm.text.toString())
            Log.d("verificationIdAll:",verificationIdAll)
            signInWithPhoneAuthCredential(credential)
        }

        //중복확인
        double_check.setOnClickListener {

            val join_id = findViewById<EditText>(R.id.join_id)

            if(join_id.text.toString().isNullOrBlank()){
                Toast.makeText(this,"아이디를 입력하세요!!",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val query = docRef.document(join_id.text.toString()).get()

            query.addOnSuccessListener {
                    document -> if(!document.exists()){
                this.double_check_confirm = "yes"
                Toast.makeText(this,"사용 가능한 ID입니다.",Toast.LENGTH_LONG).show()
            }else{
                this.double_check_confirm = "no"
                Toast.makeText(this,"중복된 ID입니다.",Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            }.addOnFailureListener{
                    exception ->
                Log.d(TAG,"중복체크 로직 오류 ",exception)
            }
        }

        join_button.setOnClickListener {

            try {

                val join_check = findViewById<CheckBox>(R.id.join_check)
                val join_name = findViewById<EditText>(R.id.join_name)
                val join_sub_name = findViewById<EditText>(R.id.join_sub_name)
                val join_id = findViewById<EditText>(R.id.join_id)
                val join_pwd = findViewById<EditText>(R.id.join_pwd)
                val join_confirm_pwd = findViewById<EditText>(R.id.join_confirm_pwd)
                val join_pNum_insert_one = findViewById<EditText>(R.id.join_pNum_insert_one)
                val join_pNum_insert_two = findViewById<EditText>(R.id.join_pNum_insert_two)
                val join_pNum_insert_three = findViewById<EditText>(R.id.join_pNum_insert_three)

                val join_pNum_insert = join_pNum_insert_one.text.toString()+"-"+join_pNum_insert_two.text.toString()+"-"+join_pNum_insert_three.text.toString()

                join_check.setOnCheckedChangeListener { buttonView, isChecked ->

                    if(isChecked){
                        join_check.text = " 동의"
                    }else{
                        join_check.text = "반대"
                    }
                }


                if(join_name.text.toString().isNullOrBlank()){
                    //|| !(Pattern.matches("^[가-힣]*$",user.name)) 한글 입력 조건
                    Toast.makeText(this,"이름이 공백입니다!",Toast.LENGTH_LONG).show()
                    return@setOnClickListener

                }

                if(1 >= join_sub_name.text.toString().length || join_sub_name.text.toString().length >= 9){
                    Toast.makeText(this,"닉네임의 길이는 최소2글자 최대 8글자 입니다!",Toast.LENGTH_LONG).show()
                    return@setOnClickListener

                }

                if(join_id.text.toString().length > 20 ){
                    Toast.makeText(this,"아이디가 20글자를 초과했어요!",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                //비밀번호 확인
                if(join_pwd.text.toString() != join_confirm_pwd.text.toString()){
                    Toast.makeText(this,"비밀번호가 맞지 않습니다!",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if(!(join_check.isChecked)){
                    Toast.makeText(this,"이용약관에 동의해주세요!",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if(!phone_auth){
                    Toast.makeText(this,"휴대폰 인증을 해주세요!",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if(double_check_confirm == "no") {
                    Toast.makeText(this,"중복체크 해주세요!",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }else{

                    auth.createUserWithEmailAndPassword(join_id.text.toString(),join_pwd.text.toString())
                        .addOnCompleteListener(this){
                                task -> if(task.isSuccessful){

                            val user = User(join_name.text.toString(),join_sub_name.text.toString(),join_id.text.toString(),
                                join_pwd.text.toString(),join_pNum_insert,auth.currentUser!!.uid,"0","0",
                                listOf(""))
                            docRef.document(user.id).set(user)
                            val updates = hashMapOf<String,Any>(
                                "pick_up_list" to FieldValue.delete()
                            )
                            docRef.document(user.id).update(updates)


                            Toast.makeText(this,"회원가입 성공!",Toast.LENGTH_LONG).show()
//                            //실시간 디비에 이름하고 uid 추가
//                            addUserToDatabase(user.name,auth.currentUser?.uid!!)
                            val intent = Intent(this, JoinFinshActivity::class.java)
                            intent.putExtra("user_name",user.name)
                            startActivity(intent)

                        }else{

                            Toast.makeText(this, "회원가입 실패!", Toast.LENGTH_LONG).show()
                            Log.d(TAG, "가입 실패 오류:", task.exception)


                        }

                        }

                }



            }catch (exception: Exception){

                val join_id = findViewById<EditText>(R.id.join_id)

                if(!(Pattern.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9]+$", join_id.text.toString()))){
                    Toast.makeText(this,"아이디가 이메일 형식이 아닙니다!",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                Toast.makeText(this, "필수사항을 모두 입력하세요!", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }
        }

        val btn_back = findViewById<Button>(R.id.btn_back)
        btn_back.setOnClickListener {
            //뒤로가기
            var intent1 = Intent(this, LoginActivity::class.java)
            startActivity(intent1)
        }


    }

    //비밀번호 확인란을 위한 함수

    fun textWatcher(){
        val pwd1 = findViewById<EditText>(R.id.join_pwd)
        val pwd2 = findViewById<EditText>(R.id.join_confirm_pwd)

        pwd1

        pwd2.addTextChangedListener(object : TextWatcher{


            //입력 전 동작
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                pwd2.error = "비밀번호 확인을 입력하세요!"
            }

            //입력난에 변화가 있을 때 동작
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                pwd2.error = "비밀번호 확인이 일치하지 않습니다!"
            }

            //입력이 끝났을 때 동작
            override fun afterTextChanged(p0: Editable?) {
                if(pwd1.text.toString()!=pwd2.text.toString()){
                    pwd2.error = "비밀번호 확인이 일치하지 않습니다!"
                }
                else if(pwd1.text.toString()==pwd2.text.toString()){
                    pwd2.error = null
                }
            }

        })








    }

//    private fun addUserToDatabase(name: String,uid: String){
//        mDbRef= FirebaseDatabase.getInstance().getReference()
//        mDbRef.child("user").child(uid).setValue(ChatUser(name,uid))
//
//    }
}