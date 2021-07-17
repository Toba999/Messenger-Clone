package com.example.android.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.chatapp.R
import com.example.android.chatapp.adapter.LatestMessagesAdapter
import com.example.android.chatapp.databinding.ActivityLatestMessagesBinding
import com.example.android.chatapp.fireStore.FireStoreClass
import com.example.android.chatapp.models.User
import com.example.android.chatapp.models.UserState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot

class LatestMessagesActivity : BaseActivity(),View.OnClickListener {
    private lateinit var binding : ActivityLatestMessagesBinding
    private lateinit var mUserStateList: ArrayList<UserState>
    private lateinit var mUsersList: ArrayList<User>
    var adapterUsers :LatestMessagesAdapter?=null


    private var currentUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getUserStateList()

        getUsersList()


        binding.icNewMessage.setOnClickListener(this)

        binding.icSignOut.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.ic_new_message -> {
                    val intent = Intent(this, NewMessagesActivity::class.java)
                    startActivity(intent)
                }

                R.id.ic_sign_out -> {
                    if (currentUser!=null){
                        FireStoreClass().updateUserStatus("offline")
                    }
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        FireStoreClass().updateUserStatus("online")
    }

    override fun onPause() {
        super.onPause()
        if (currentUser!=null){
            FireStoreClass().updateUserStatus("offline")
        }
    }

    private fun getUserStateList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().listenForAllUserState(this)
    }

    fun viewUserStateSuccessfully(userStateList: ArrayList<UserState>){
        getCurrentUserDetails()
        adapterUsers?.setMessagesList(userStateList)
    }


    private fun getCurrentUserDetails(){
        FireStoreClass().getUserDetails(this)
    }

    fun getCurrentUserDetailsSuccess(user: User){
        currentUser = user
    }

    private fun getUsersList() {
        FireStoreClass().getAllUsersList(this)
    }


    fun successUsersListFromFireStore(usersList: ArrayList<User>) {
        mUsersList = usersList
        hideProgressDialog()


        if (mUsersList.size > 0) {
            binding.rvMyMessages.visibility = View.VISIBLE
            binding.tvNoMessagesUsersFound.visibility = View.GONE

            binding.rvMyMessages.layoutManager = LinearLayoutManager(this)
            binding.rvMyMessages.setHasFixedSize(true)

            // START
            adapterUsers =LatestMessagesAdapter(this, mUsersList)
            // END
            binding.rvMyMessages.adapter = adapterUsers
        } else {
            binding.rvMyMessages.visibility = View.GONE
            binding.tvNoMessagesUsersFound.visibility = View.VISIBLE
        }
    }
}