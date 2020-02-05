package com.example.instagram.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.AccountSettingsActivity
import com.example.instagram.Adapter.ImageViewAdapter
import com.example.instagram.Model.Post
import com.example.instagram.Model.User

import com.example.instagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private lateinit var profileId :String
    private lateinit var firebaseUser: FirebaseUser

    var postList: List<Post>? = null
     var imageAdapter: ImageViewAdapter? = null

     var saveImageAdapter: ImageViewAdapter? = null
    var saveImageList: List<Post>? = null
    var mySaveImages: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        // show post images in profile fragment
        var recyclerViewUploadImage: RecyclerView
        recyclerViewUploadImage = view.findViewById(R.id.uploaded_post_recyclerview_images)
        recyclerViewUploadImage.setHasFixedSize(true)

        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImage.layoutManager = linearLayoutManager

        postList = ArrayList()
        imageAdapter = context?.let { ImageViewAdapter(it, postList as ArrayList<Post>) }
        recyclerViewUploadImage.adapter = imageAdapter



        // show save images in profile
        var recyclerViewSaveImages :RecyclerView
        recyclerViewSaveImages = view.findViewById(R.id.saved_post_recyclerview_images)
        recyclerViewSaveImages.setHasFixedSize(true)

        val linearLayoutManagerSave: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewSaveImages.layoutManager = linearLayoutManagerSave

        saveImageList = ArrayList()
        saveImageAdapter = context?.let { ImageViewAdapter(it, saveImageList as ArrayList<Post>) }
        recyclerViewSaveImages.adapter = saveImageAdapter



        //default show
        recyclerViewSaveImages.visibility = View.GONE
        recyclerViewUploadImage.visibility = View.VISIBLE



        // toggole show post image and save image
        var postImageBtn : ImageButton
        postImageBtn = view.findViewById(R.id.images_grid_view_btn)
        postImageBtn.setOnClickListener {
            recyclerViewSaveImages.visibility = View.GONE
            recyclerViewUploadImage.visibility = View.VISIBLE
        }

        var saveImageBtn : ImageButton
        saveImageBtn = view.findViewById(R.id.images_save_btn)
        saveImageBtn.setOnClickListener {
            recyclerViewSaveImages.visibility = View.VISIBLE
            recyclerViewUploadImage.visibility = View.GONE
        }

        
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


        getTotalNumbersPosts()
        getTotalNumbersFollowers()
        getTotalNumbersFollowing()
        userInfo()
        userUploadedImages()
        getSavePostImages()
        return view
    }

    private fun getSavePostImages(){
        mySaveImages = ArrayList()

        val saveImageRef = FirebaseDatabase.getInstance().reference
            .child("SavePostImages").child(firebaseUser!!.uid)

        saveImageRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    for (snapshot in p0.children)
                    {
                        (mySaveImages as ArrayList<String>).add(snapshot.key!!)
                    }
                    getSaveImageData()
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getSaveImageData(){
        val posRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
        posRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    (saveImageList as ArrayList<Post>).clear()

                    for (snapshot in p0.children)
                    {
                        val post = snapshot.getValue(Post::class.java)!!
                        for(key in mySaveImages!!)
                        {
                            if(post.getPostId() == key)
                            {
                                (saveImageList as ArrayList<Post>).add(post!!)
                            }
                        }
                    }
                    saveImageAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun userUploadedImages(){
        val userPostsRef = FirebaseDatabase.getInstance().reference.child("Posts")

            userPostsRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists())
                    {
                        (postList as ArrayList<Post>).clear()
                        for (snapshot in p0.children)
                        {
                            val post = snapshot.getValue(Post::class.java)
                            if(post!!.getPublisher().equals(profileId))
                            {
                                (postList as ArrayList<Post>).add(post)
                            }
                            Collections.reverse(postList)
                            imageAdapter!!.notifyDataSetChanged()
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
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

    private fun getTotalNumbersPosts(){
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    var postCount = 0
                    for (snapshot in p0.children)
                    {
                        val post = snapshot.getValue(Post::class.java)!!
                        if(post.getPublisher() == profileId)
                        {
                            postCount ++
                        }
                    }
                        total_post_count.text = " "+ postCount

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getTotalNumbersFollowers(){
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


    private fun getTotalNumbersFollowing(){
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
