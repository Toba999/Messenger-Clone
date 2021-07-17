package com.example.android.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.chatapp.R
import com.example.android.chatapp.adapter.UsersAdapter
import com.example.android.chatapp.databinding.ActivityNewMessagesBinding
import com.example.android.chatapp.fireStore.FireStoreClass
import com.example.android.chatapp.models.User

class NewMessagesActivity : BaseActivity() {
    private lateinit var binding : ActivityNewMessagesBinding
    private var currentUser: User? = null
    private lateinit var mUsersList: ArrayList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getUsersList()
        getCurrentUserDetails()
        setupActionBar()
    }
    private fun getCurrentUserDetails(){
        FireStoreClass().getUserDetails(this)
    }

    fun getCurrentUserDetailsSuccess(user: User){
        currentUser = user
    }

    override fun onResume() {
        super.onResume()
            FireStoreClass().updateUserStatus("online")
    }

    override fun onPause() {
        super.onPause()
        if (currentUser!=null){
            FireStoreClass().updateUserStatus("offline")
        }
    }



    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarNewMessagesActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow)
        }

        binding.toolbarNewMessagesActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getUsersList() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAllUsersList(this)
    }

    fun successUsersListFromFireStore(usersList: ArrayList<User>) {
        mUsersList = usersList
        hideProgressDialog()


        if (mUsersList.size > 0) {
            binding.rvMyUsers.visibility = View.VISIBLE
            binding.tvNoUsersFound.visibility = View.GONE

            binding.rvMyUsers.layoutManager = LinearLayoutManager(this)
            binding.rvMyUsers.setHasFixedSize(true)

            // START
            val adapterUsers =
                UsersAdapter(this, mUsersList )
            // END
            binding.rvMyUsers.adapter = adapterUsers
        } else {
            binding.rvMyUsers.visibility = View.GONE
            binding.tvNoUsersFound.visibility = View.VISIBLE
        }
    }
}