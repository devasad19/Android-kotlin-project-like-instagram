package com.example.instagram.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.instagram.AccountSettingsActivity
import com.example.instagram.Model.User

import com.example.instagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private lateinit var profileId :String
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null)
        {
            this.profileId = pref.getString("profileId", "none").toString()

        }

        if(profileId == firebaseUser.uid)
        {
            view.edit_profile_btn_id.text = "Edit Profile"
        }
        else if(profileId != firebaseUser.uid)
        {
            checkFollowAndFollowingBtnStatus()
        }


        view.edit_profile_btn_id.setOnClickListener {
            val getBtnTxt = view.edit_profile_btn_id.text.toString()

            when{
                getBtnTxt == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))
                getBtnTxt == "Follow" -> {
                    firebaseUser?.uid.let { it1->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)

                    }
                    firebaseUser?.uid.let { it1->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)

                    }

                }
                getBtnTxt == "Following" -> {
                    firebaseUser?.uid.let { it1->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()

                    }
                    firebaseUser?.uid.let { it1->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()

                    }

                }


            }

        }

        getFollowers()
        getFollowing()
        userInfo()

        return view
    }

    private fun checkFollowAndFollowingBtnStatus() {
        val followingRef = firebaseUser?.uid.let { it1->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")

        }

        if(followingRef != null)
        {
            followingRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child(profileId).exists())
                    {
                        view?.edit_profile_btn_id?.text = "Following"
                    }
                    else
                    {
                        view?.edit_profile_btn_id?.text = "Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }

    }


    private fun getFollowers(){
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")


        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    view?.total_followers_count?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun getFollowing(){
        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followingRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    view?.total_following_count?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        userRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.profile_image_id)

                    view?.profile_fullname_fragment?.text = user!!.getFullname()
                    view?.profile_top_username_fragment?.text = user!!.getUsername()
                    view?.profile_bio_fragment?.text = user!!.getBio()
                }

//                if(context != null)
//                {
//                    return
//                }
            }

            override fun onCancelled(snapshot: DatabaseError) {

            }

        })
    }

    override fun onStop() {
        super.onStop()

        val preference = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        preference?.putString("profileId", firebaseUser.uid)
        preference?.apply()
    }

    override fun onPause() {
        super.onPause()

        val preference = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        preference?.putString("profileId", firebaseUser.uid)
        preference?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val preference = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        preference?.putString("profileId", firebaseUser.uid)
        preference?.apply()
    }
}
