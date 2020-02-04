package com.example.instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Adapter.CommentsAdapter
import com.example.instagram.Model.Comment
import com.example.instagram.Model.Post
import com.example.instagram.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentsAdapter? = null
    private var commentList: MutableList<Comment>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")

        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.comments_recycler_view)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList as ArrayList<Comment>)
        recyclerView.adapter = commentAdapter

        getPostImage(postId)

        getUserInfo()
        getComments()

        comment_publish_btn.setOnClickListener {
            if(comment_input_feild.text.toString() == "")
            {
                Toast.makeText(this, "Comment field must not be empty.", Toast.LENGTH_LONG).show()
            }
            else
            {
                addCommentFunction()
            }
        }

    }

    private fun getPostImage(postId: String?) {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts").child(postId!!)

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    val post = p0.getValue<Post>(Post::class.java)
                    Picasso.get().load(post!!.getPostImage()).placeholder(R.drawable.profile)
                        .into(comment_post_image)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun addCommentFunction() {
        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId!!)

        val commentsMap = HashMap<String, Any>()

        commentsMap["comment"] = comment_input_feild!!.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentRef.push().setValue(commentsMap)

        comment_input_feild!!.text.clear()
    }


    private fun getUserInfo()
    {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(commenter_image)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun getComments(){
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId!!)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    commentList!!.clear()

                    for (snapshot in p0.children)
                    {
                        val comment = snapshot.getValue(Comment::class.java)
                        commentList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}
