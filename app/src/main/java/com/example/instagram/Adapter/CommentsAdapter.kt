package com.example.instagram.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Model.Comment
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
import kotlinx.android.synthetic.main.comment_item_layout.view.*
import java.util.zip.Inflater

class CommentsAdapter(private val mContext:Context, private val mComment: MutableList<Comment> ):
    RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.comment_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mComment!!.size
    }

    override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val comment = mComment!![position]
        holder.userComment.text = comment!!.getComment()

        getUserInfo(holder.userImage, holder.userFullName, comment.getPublisher())
    }


    inner class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView){
         var userImage: CircleImageView
         var userFullName: TextView
         var userComment: TextView


        init {
            userImage = itemView.findViewById(R.id.user_image_in_comment)
            userFullName = itemView.findViewById(R.id.user_fullname_in_comment)
            userComment = itemView.findViewById(R.id.user_comment)
        }

    }



    private fun getUserInfo(userImage: CircleImageView, userFullName: TextView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(publisher)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val user = p0.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(userImage)
                    userFullName.text = user!!.getFullname()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}
