package com.example.instagram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import androidx.core.os.postDelayed
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sign_in_page_btn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        register_btn.setOnClickListener({
            CreateAccount()
        })

    }

    private fun CreateAccount() {
        val fullname = register_full_name.text.toString()
        val username = register_username.text.toString()
        val email = register_email.text.toString()
        val password = register_password.text.toString()

        when{
            TextUtils.isEmpty(fullname) -> Toast.makeText(this, "Full Name is Required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(username) -> Toast.makeText(this, "User Name is Required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is Required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is Required.", Toast.LENGTH_LONG).show()

            else ->{
                val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Registration Progress")
                    progressDialog.setCancelable(false)
                    progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            saveUserInfo(fullname, username, email, progressDialog)
                        }else{
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }

            }
        }
    }

    private fun saveUserInfo(fullname: String, username: String, email: String, progressDialog:ProgressDialog) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef :DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullname.toLowerCase()
        userMap["username"] = username.toLowerCase()
        userMap["email"] = email

        userMap["bio"] = "Here is sitted my bio about myself."
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/kotlin-instagram-app.appspot.com/o/d_Images%2Fprofile.png?alt=media&token=a5833989-cb32-4363-ba7d-c548ce2023a1"

        userRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account has been created successfully.", Toast.LENGTH_LONG).show()

                    // following to myself
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId)
                        .setValue(true)


                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else{
                    val message= task.exception!!.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }

    }
}
