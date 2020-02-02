package com.example.instagram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        sign_up_btn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        login_btn.setOnClickListener {
            userLoginFunction()
        }

    }

    private fun userLoginFunction() {
        val email = login_email.text.toString()
        val passsword = login_password.text.toString()

        when{
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Username is required. ", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(passsword) -> Toast.makeText(this, "Password is required. ", Toast.LENGTH_LONG).show()

        else -> {
            val progressDialog = ProgressDialog(this@SignInActivity)
                progressDialog.setTitle("LOGIN")
                progressDialog.setMessage("Please wait a few seconds. Login progress...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

            val mAuth = FirebaseAuth.getInstance()
            mAuth.signInWithEmailAndPassword(email, passsword).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    progressDialog.dismiss()

                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    val message = task.exception?.toString()
                    Toast.makeText(this, "Login Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
        }
      }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }


}
