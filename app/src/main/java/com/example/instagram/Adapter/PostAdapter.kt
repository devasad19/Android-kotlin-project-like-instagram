package com.example.instagram.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.CommentActivity
import com.example.instagram.MainActivity
import com.example.instagram.Model.Post
import com.example.instagram.Model.User
import com.example.instagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.post_layout.view.*


class PostAdapter(private val mContext : Context, private val mPost: List<Post>):RecyclerView.Adapter<PostAdapter.ViewHolder> (){

    private var firebaseUser :FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]

        if(post.getDescription().equals(""))
        {
            holder.description.visibility = View.GONE
        }
        else
        {
            holder.description.visibility = View.VISIBLE
            holder.description.setText(post.getDescription())
        }

        Picasso.get().load(post.getPostImage()).placeholder(R.drawable.add_image_icon).into(holder.postImage)

        getPublisherInfo(holder.profileImage, holder.publisher, post.getPublisher())



        // post like operations
        isLikeOfPost(holder.likeButton, post.getPostId())
        getTotalLikes(holder.likes, post.getPostId())
        holder.likeButton.setOnClickListener {

            if(holder.likeButton.tag == "Like")
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostId())
                    .child(firebaseUser!!.uid)
                    .setValue(true)
            }
            else
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostId())
                    .child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }
        }

        // comments operations
        getTotalCommets(holder.comments, post.getPostId())

        holder.commentButton.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java)
            intentComment.putExtra("postId", post.getPostId())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)


        }

        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java)
            intentComment.putExtra("postId", post.getPostId())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)


        }

        // save post images operation
        savePostImages(post.getPostId(), holder.saveButton)

        holder.saveButton.setOnClickListener {
            if(holder.saveButton.tag == "Save")
            {
                FirebaseDatabase.getInstance().reference
                    .child("SavePostImages").child(firebaseUser!!.uid)
                    .child(post.getPostId())
                    .setValue(true)
            }
            else
            {
                FirebaseDatabase.getInstance().reference
                    .child("SavePostImages").child(firebaseUser!!.uid)
                    .child(post.getPostId())
                    .removeValue()
            }

        }

    }

    private fun getTotalCommets(comments: TextView, postId: String) {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    comments.text = "view all "+p0.childrenCount.toString() + " comments"
                }
                else
                {
                    comments.text = "No comments"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun getTotalLikes(likes: TextView, postId: String) {
        val likeRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)

        likeRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    likes.text = p0.childrenCount.toString() + " likes"
                }
                else
                {
                    likes.text = "0 like"
                }
            }

        })
    }

    private fun isLikeOfPost(likeButton: ImageView, postId: String) {
        val authUser = FirebaseAuth.getInstance().currentUser
        val likeBtnRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)

        likeBtnRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.child(firebaseUser!!.uid).exists())
                {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                }
                else
                {
                    likeButton.setImageResource(R.drawable.heart)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })


    }


    inner class ViewHolder(@NonNull itemView : View): RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var postImage : ImageView
        var likeButton : ImageView
        var commentButton : ImageView
        var saveButton : ImageView
        var likes : TextView
        var comments : TextView
        var publisher : TextView
        var description : TextView


        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)
        }



    }

    private fun getPublisherInfo(profileImage: CircleImageView, publisher: TextView, publisherID: String) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val user = p0.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    publisher.setText(user!!.getFullname())
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }


    private fun savePostImages(postId: String, postImage: ImageView)
    {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("SavePostImages").child(firebaseUser!!.uid)
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.child(postId).exists())
                {
                    postImage.setImageResource(R.drawable.save_large_icon)
                    postImage.tag = "Saved"
                }
                else
                {
                    postImage.setImageResource(R.drawable.save_unfilled_large_icon)
                    postImage.tag = "Save"
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}