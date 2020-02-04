package com.example.instagram.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Model.User
import com.example.instagram.R
import com.example.instagram.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.user_item_layout.view.*

class UserAdapter(private var mContext:Context,
                  private var mUser:List<User>,
                  private var isFragment: Boolean = false):RecyclerView.Adapter<UserAdapter.ViewHolder> (){

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
       val user = mUser[position]

        viewHolder.userNameText.text = user.getUsername()
        viewHolder.userFullNameText.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(viewHolder.userImage)


        checkFollowingStatus(user.getUID(), viewHolder.followBtn)

        viewHolder.itemView.setOnClickListener ( View.OnClickListener {
            val preference = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            preference.putString("profileId", user.getUID())
            preference.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()

        })

        viewHolder.followBtn.setOnClickListener {
            if(viewHolder.followBtn.text.toString() == "Follow")
            {
                firebaseUser?.uid.let { it1->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .setValue(true).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
            else{
                firebaseUser?.uid.let { it1->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .removeValue().addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }

    }



    class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView)
    {
        var userNameText : TextView = itemView.findViewById(R.id.username_search)
        var userFullNameText : TextView = itemView.findViewById(R.id.user_fullname_search)
        var userImage : CircleImageView = itemView.findViewById(R.id.user_profile_picture_id)
        var followBtn : Button = itemView.findViewById(R.id.follow_btn_search)
    }


    private fun checkFollowingStatus(uid: String, followBtn: Button) {
        val followingRef = firebaseUser?.uid.let { it1->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")

         }
        followingRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child(uid).exists())
                {
                    followBtn.text = "Following"
                }
                else
                {
                    followBtn.text = "Follow"

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}