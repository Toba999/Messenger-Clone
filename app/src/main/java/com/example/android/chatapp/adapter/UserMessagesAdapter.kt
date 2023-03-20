package com.example.android.chatapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.chatapp.databinding.ItemUserChatRowBinding
import com.example.android.chatapp.models.Message
import com.example.android.chatapp.models.User
import com.example.android.chatapp.utils.GlideLoader

open class UserMessagesAdapter (private val context: Context, private val list: ArrayList<Message>,
                           private val currentUser: User, private val otherUser: User)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemUserChatRowBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder) {

            if (currentUser.id == model.sender) {
                holder.viewBinding.llCurrentUser.visibility = View.VISIBLE
                holder.viewBinding.llOtherUser.visibility = View.GONE

                if (position==list.size-1){
                    if (model.seen){
                        holder.viewBinding.tvCurrentUserSeen.visibility=View.VISIBLE
                    }else{
                        holder.viewBinding.tvCurrentUserSeen.visibility=View.VISIBLE
                        holder.viewBinding.tvCurrentUserSeen.text = "Delivered"
                    }
                }

                GlideLoader(context).loadUserPicture(currentUser.image, holder.viewBinding.ivCurrentUserImage)
                holder.viewBinding.tvCurrentUserMessage.text = model.text
                holder.viewBinding.tvCurrentUserMessageDate.text = model.date


            } else if (otherUser.id == model.sender) {
                holder.viewBinding.llCurrentUser.visibility = View.GONE
                holder.viewBinding.llOtherUser.visibility = View.VISIBLE

                GlideLoader(context).loadUserPicture(otherUser.image, holder.viewBinding.ivOtherUserImage)
                holder.viewBinding.tvOtherUserMessage.text = model.text
                holder.viewBinding.tvOtherUserMessageDate.text = model.date
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(var viewBinding: ItemUserChatRowBinding) : RecyclerView.ViewHolder(viewBinding.root)

}