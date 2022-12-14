package com.example.gsgs_plus_final.login

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.gsgs_plus_final.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


class IdFindFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    var verificationIdAll: String = ""
    private var userId : String = ""
    private var find_user_id_pnum_confirm_flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v =inflater.inflate(R.layout.fragment_id_find, container, false)

        val db = Firebase.firestore
        val docRef = db.collection("users")
        auth = Firebase.auth

        val find_user_id_name = v.findViewById<EditText>(R.id.find_id_user_name)
        val find_user_id_pnum_one = v.findViewById<EditText>(R.id.find_id_pNum_one)
        val find_user_id_pnum_two = v.findViewById<EditText>(R.id.find_id_pNum_two)
        val find_user_id_pnum_three = v.findViewById<EditText>(R.id.find_id_pNum_three)


        v.findViewById<Button>(R.id.find_id_pNum_sendBtn).setOnClickListener {

            val find_pNum_insert = find_user_id_pnum_one.text.toString()+"-"+find_user_id_pnum_two.text.toString()+"-"+find_user_id_pnum_three.text.toString()


            docRef.whereEqualTo("name", find_user_id_name.text.toString())
                .get().addOnSuccessListener { documents ->
                    for (document in documents) {

                        if (document.get("p_num").toString() != find_pNum_insert) {

                            Log.d("name:",document.get("p_num").toString())
                            Log.d("name2:",find_pNum_insert)

                            Toast.makeText(context, "????????? ????????? ????????? ????????????!", Toast.LENGTH_LONG).show()
                            return@addOnSuccessListener
                        }//if

                        userId = document.get("id").toString()

                        //???????????? ?????? ??????
                        val callbacks =
                            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    Log.d(ContentValues.TAG, "call onVerificationCompleted")

                                }

                                //???????????? ?????? ??????
                                override fun onVerificationFailed(e: FirebaseException) {
                                    Log.w(ContentValues.TAG, "onVerificationFailed", e)

                                    if (e is FirebaseAuthInvalidCredentialsException) {
                                        Toast.makeText(context,"???????????? ????????? ??????????????????.", Toast.LENGTH_LONG).show()
                                        Log.w(ContentValues.TAG, "FirebaseAuthInvalidCredentialsException", e)
                                        // Invalid request
                                    } else if (e is FirebaseTooManyRequestsException) {
                                        Toast.makeText(context, "???????????? ????????? ??????????????????.", Toast.LENGTH_LONG).show()
                                        Log.w(ContentValues.TAG, "FirebaseTooManyRequestsException", e)
                                        // The SMS quota for the project has been exceeded
                                    }
                                }

                                //???????????? ?????? ???
                                override fun onCodeSent(
                                    verificationId: String,
                                    token: PhoneAuthProvider.ForceResendingToken,
                                ) {

                                    Toast.makeText(context, "??????????????? ?????????????????????.", Toast.LENGTH_LONG).show()
                                    Log.d(ContentValues.TAG, "onCodeSent:$verificationId")
                                    verificationIdAll = verificationId


                                }


                            }

                        Log.d("find_id_phone!!", find_pNum_insert)
                        //??????????????? ?????? ?????????
                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber("+82"+find_pNum_insert)
                            .setTimeout(90L, TimeUnit.SECONDS)
                            .setActivity(context as Activity)
                            .setCallbacks(callbacks)
                            .build()

                        PhoneAuthProvider.verifyPhoneNumber(options)


                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("????????? ?????? ??????", "???????????? ?????? ??????")
                    Toast.makeText(context, "???????????? ????????? ????????? ????????????!", Toast.LENGTH_LONG).show()
                    return@addOnFailureListener
                }


        }

        //?????? ?????? ??????
        fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

            auth.signInWithCredential(credential).addOnCompleteListener {
                    task -> if(task.isSuccessful) {
                find_user_id_pnum_confirm_flag = true
                Log.d("PhoneSuccess:", "Confirm")
                Toast.makeText(context,"?????????????????????.", Toast.LENGTH_LONG).show()

                auth.currentUser!!.delete().addOnCompleteListener {
                        task -> if(task.isSuccessful){
                    Log.d("PhoneUserDelete","Complete")
                    auth.signOut()

                }

                }

            }else{
                Toast.makeText(context,"??????????????? ????????????!", Toast.LENGTH_LONG).show()
                Log.d("PhoneFail:","Fail!!")

            }
            }
        }

        //???????????? ?????? ?????? ??????
        v.findViewById<Button>(R.id.find_id_pNum_confrim).setOnClickListener {
            val find_id_pNum_confrim_insert = v.findViewById<EditText>(R.id.find_id_pNum_confrim_insert)

            if(find_id_pNum_confrim_insert.text.toString().isNullOrBlank()){
                Toast.makeText(context,"??????????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.d("confirmNumber:",find_id_pNum_confrim_insert.text.toString())


            val credential = PhoneAuthProvider.getCredential(verificationIdAll, find_id_pNum_confrim_insert.text.toString())
            Log.d("verificationIdAll:",verificationIdAll)
            signInWithPhoneAuthCredential(credential)
        }



        //????????? ?????? ?????? ?????????
        v.findViewById<Button>(R.id.btn_idFind).setOnClickListener {

            if (find_user_id_name.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }

            if (find_user_id_pnum_one.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "??????????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (find_user_id_pnum_two.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "??????????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (find_user_id_pnum_three.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "??????????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!find_user_id_pnum_confirm_flag) {
                Toast.makeText(context, "??????????????? ???????????????!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            activity?.let {
                var intent = Intent(context, IdFindFinishActivity::class.java)
                intent.putExtra("name",find_user_id_name.text.toString())
                intent.putExtra("find_id",userId)
                startActivity(intent)

            }
        }//????????? ?????? ?????? ?????????

        // Inflate the layout for this fragment
        return v
    }
}