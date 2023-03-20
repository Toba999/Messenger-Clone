package com.example.android.chatapp.activities

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.chatapp.R
import com.example.android.chatapp.adapter.UserMessagesAdapter
import com.example.android.chatapp.databinding.ActivityChatBinding
import com.example.android.chatapp.fireStore.FireStoreClass
import com.example.android.chatapp.models.Message
import com.example.android.chatapp.models.User
import com.example.android.chatapp.models.UserState
import com.example.android.chatapp.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity :BaseActivity() {
    private lateinit var binding : ActivityChatBinding

    private var currentUser: User? = null
    private lateinit var otherUser: User
    private var mUsersMessagesList: ArrayList<Message>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        getCurrentUserDetails()






        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)){
            otherUser = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
            binding.tvTitleChat.text = otherUser.userName
        }

        binding.btnSendMessage.setOnClickListener {
            val text = binding.etMessageText.text.toString()
            val calendar: Calendar = Calendar.getInstance()
            val currentDate: String = SimpleDateFormat("hh:mm a").format(calendar.time)
            sendMessage(text, currentDate)
            binding.etMessageText.text.clear()
        }


        FireStoreClass().listenForUserState(this,otherUser.id)

    }

    private fun updateSeenMessage(){
        FireStoreClass().updateSeenMessage(otherUser.id)
    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarChatActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_white)
        }
        binding.toolbarChatActivity.setNavigationOnClickListener { onBackPressed() }
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


    private fun getCurrentUserDetails(){
        FireStoreClass().getUserDetails(this)
    }

    fun getCurrentUserDetailsSuccess(user: User){
        currentUser = user
        FireStoreClass().listenForMessages(this, currentUser!!.id, otherUser.id)
    }

    private fun sendMessage(text: String, date: String){
        val message = Message(
                "",
                currentUser!!.id,
                otherUser.id,
                text,
                date,
                 false)
        FireStoreClass().sendMessagesToFirebase(this, message)
    }

    fun viewMessagesSuccessfully(mChat: ArrayList<Message>){

        updateSeenMessage()

        mUsersMessagesList = mChat

        if (mUsersMessagesList != null){
            binding.tvNoMessagesFound.visibility = View.GONE
            binding.rvChatMessagesList.visibility = View.VISIBLE

            val llm = LinearLayoutManager(this)
            llm.stackFromEnd = true
            llm.reverseLayout = false
            binding.rvChatMessagesList.layoutManager=llm

            binding.rvChatMessagesList.setHasFixedSize(true)
            val myAdapter = UserMessagesAdapter(
                    this,
                    mUsersMessagesList!!,
                    currentUser!!,
                    otherUser
            )
            binding.rvChatMessagesList.adapter = myAdapter
        }
        else{
            binding.tvNoMessagesFound.visibility = View.VISIBLE
            binding.rvChatMessagesList.visibility = View.GONE
            binding.rvChatMessagesList.requestFocus(View.FOCUS_DOWN)
        }
    }

    fun viewUserStateSuccessfully(userState: UserState){
        val date:String=userState.date
        val time:String=userState.time

        if (userState.state == "online"){
            binding.tvState.visibility = View.VISIBLE
            binding.tvState.text = "online"
            binding.icUserState.visibility = View.VISIBLE

        }
        else{
            binding.tvState.visibility = View.VISIBLE
            binding.tvState.text=  "Last seen"+" "+date+" "+time
            binding.icUserState.visibility = View.GONE
        }
    }
}