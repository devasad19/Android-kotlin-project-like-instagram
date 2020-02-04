package com.example.instagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {

    private var imgUrl = ""
    private var imageUri:Uri? = null
    private var postStorageRef: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        postStorageRef = FirebaseStorage.getInstance().reference.child("Post Images")

        add_post_save_btn.setOnClickListener {
            uploadImageFun()
        }

        CropImage.activity()
            .setAspectRatio(2,1)
            .start(this@AddPostActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            post_image_id.setImageURI(imageUri)
        }
        else
        {
            Toast.makeText(this, "Please select valid image", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImageFun() {
        when{
            imageUri == null -> Toast.makeText(this, "Please select image first", Toast.LENGTH_LONG).show()
            post_description_id.text.toString() == null -> Toast.makeText(this, "Post description cannot be null.", Toast.LENGTH_LONG).show()

            else -> {

                val progressDialog = ProgressDialog(this@AddPostActivity)
                progressDialog.setTitle("New Post")
                progressDialog.setMessage("Please wait a few seconds. post creating...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val imgRef = postStorageRef!!.child(System.currentTimeMillis().toString() +".jpg")
                var uploadTask : StorageTask<*>
                uploadTask = imgRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if(!task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation imgRef.downloadUrl
                }).addOnCompleteListener (OnCompleteListener <Uri>{ task ->
                        if(task.isSuccessful)
                        {
                            val downloadUrl = task.result
                            imgUrl = downloadUrl.toString()

                            val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                            val postId = ref.push().key

                            var postMap = HashMap<String , Any> ()

                            postMap["postId"] = postId!!
                            postMap["description"] = post_description_id.text.toString()
                            postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                            postMap["postImage"] = imgUrl

                            ref.child(postId).updateChildren(postMap)

                            Toast.makeText(this, "Post has been inserted successfully", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@AddPostActivity, MainActivity::class.java)
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
