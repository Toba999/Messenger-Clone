package com.example.android.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.chatapp.activities.ChatActivity
import com.example.android.chatapp.databinding.MessageListLayoutBinding
import com.example.android.chatapp.fireStore.FireStoreClass
import com.example.android.chatapp.models.Message
import com.example.android.chatapp.models.User
import com.example.android.chatapp.models.UserState
import com.example.android.chatapp.utils.Constants
import com.example.android.chatapp.utils.GlideLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

open class LatestMessagesAdapter(
        private val context: Context,
        private var list: ArrayList<User>,
        ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val binding = MessageListLayoutBinding
                        .inflate(LayoutInflater.from(parent.context), parent, false)
                return MyViewHolder(binding)
        }

        fun setMessagesList(stateList: ArrayList<UserState>){

                stateList.forEach { userState ->
                        list.forEach {
                                if (userState.id == it.id) {
                                        it.state = userState.state
                                }
                        }
                }
                notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val model = list[position]

                if (holder is MyViewHolder) {

                        GlideLoader(context).loadUserPicture(model.image, holder.viewBinding.ivUserPhoto)
                        holder.viewBinding.tvUserName.text = model.userName
                        lastMessage(model.id, holder.viewBinding.tvLastMessage)

                        if (model.state=="online"){
                                holder.viewBinding.icUserState.visibility=View.VISIBLE
                        }else{
                                holder.viewBinding.icUserState.visibility=View.GONE
                        }

                        holder.itemView.setOnClickListener {
                                val intent= Intent(context, ChatActivity::class.java)
                                intent.putExtra(Constants.EXTRA_USER_DETAILS,model)
                                context.startActivity(intent)
                        }
                }
        }

        private fun lastMessage(otherUserId: String, tv_last_message: TextView){
                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                FirebaseDatabase.getInstance().getReference(Constants.MESSAGES).addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                                for (i in snapshot.children){
                                        val message: Message = i.getValue(Message::class.java) as Message

                                        if (message.receiver == currentUserId && message.sender == otherUserId ||
                                                message.receiver == otherUserId && message.sender == currentUserId){
                                                val lastMsg = message.text
                                                tv_last_message.text = lastMsg
                                        }

                                        if (!message.seen && currentUserId == message.receiver){
                                                tv_last_message.setTypeface(null, Typeface.BOLD);
                                        }
                                        else{
                                                tv_last_message.setTypeface(null, Typeface.NORMAL);
                                        }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                })
        }


        override fun getItemCount(): Int {
                return list.size
        }

        class MyViewHolder(var viewBinding: MessageListLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root)

}