package com.example.instagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.instagram.Model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage

import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlin.coroutines.Continuation

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri:Uri? = null
    private var storageProfilePicRef: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity , SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Toast.makeText(this, "Log Out Successfull.", Toast.LENGTH_LONG).show()
            finish()
        }

        change_image_text_btn.setOnClickListener{
            checker = "clicked"

            var imgUri = CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsActivity)

            Log.d("imgUri", "image link - $imgUri")
         }

        save_profile_setting_btn.setOnClickListener {

            if(checker == "clicked")
            {
                profileImageAndTextUpdate()

            }
            else
            {
                updateProfileTextOnly()
            }

        }

        userInfo()

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_frag_image_view.setImageURI(imageUri)
        }else{
            Toast.makeText(this, "does not copy img url", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateProfileTextOnly() {

        when {
            profile_sett_full_name.text.toString() == null -> Toast.makeText(this, "First type your fullname.", Toast.LENGTH_LONG).show()
            profile_sett_username.text.toString() == null -> Toast.makeText(this, "Enter your sername.", Toast.LENGTH_LONG).show()
            profile_sett_bio.text.toString() == null -> Toast.makeText(this, "Enter your bio.", Toast.LENGTH_LONG).show()
            else -> {
                val userRef = FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()

                userMap["fullname"] = profile_sett_full_name.text.toString().toLowerCase()
                userMap["username"] = profile_sett_username.text.toString().toLowerCase()
                userMap["bio"] = profile_sett_bio.text.toString().toLowerCase()
                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Your Information updated successfully.", Toast.LENGTH_LONG).show()
                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_frag_image_view)

                    profile_sett_full_name.setText(user!!.getFullname())
                    profile_sett_username.setText(user!!.getUsername())
                    profile_sett_bio.setText(user!!.getBio())
                }


            }

            override fun onCancelled(snapshot: DatabaseError) {

            }

        })
    }

    private fun profileImageAndTextUpdate() {
        when{
            profile_sett_full_name.text.toString() == null -> Toast.makeText(this, "First type your fullname.", Toast.LENGTH_LONG).show()
            profile_sett_username.text.toString() == null -> Toast.makeText(this, "Enter your sername first.", Toast.LENGTH_LONG).show()
            profile_sett_bio.text.toString() == null -> Toast.makeText(this, "Enter your bio first.", Toast.LENGTH_LONG).show()
            imageUri == null -> Toast.makeText(this, "Please select image first", Toast.LENGTH_LONG).show()

            else ->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Setting")
                progressDialog.setMessage("Please wait, we are updating your profile ...")
                progressDialog.show()

                val fileRaf = storageProfilePicRef!!.child(firebaseUser!!.uid +".jpg")

                val uploadTask: StorageTask<*>
                uploadTask = fileRaf.putFile(imageUri!!)
                uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if(!task.isSuccessful)
                    {
                        task.exception!!.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRaf.downloadUrl
                }).addOnCompleteListener( OnCompleteListener<Uri> { task ->
                    if(task.isSuccessful)
                    {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val uRef = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()

                        userMap["fullname"] = profile_sett_full_name.text.toString().toLowerCase()
                        userMap["username"] = profile_sett_username.text.toString().toLowerCase()
                        userMap["bio"] = profile_sett_bio.text.toString().toLowerCase()
                        userMap["image"] = myUrl


                        uRef.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Your account information updated successfull", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else
                    {
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }





}


