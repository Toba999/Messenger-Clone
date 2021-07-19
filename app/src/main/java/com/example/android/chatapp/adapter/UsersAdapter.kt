package com.example.android.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.chatapp.activities.ChatActivity
import com.example.android.chatapp.databinding.UserListLayoutBinding
import com.example.android.chatapp.models.User
import com.example.android.chatapp.utils.Constants
import com.example.android.chatapp.utils.GlideLoader

open class UsersAdapter(
        private val context: Context,
        private var list: ArrayList<User>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = UserListLayoutBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

            return MyViewHolder(binding)
        }



        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val model = list[position]

            if (holder is MyViewHolder) {

                GlideLoader(context).loadProductPicture(model.image, holder.viewBinding.ivUserPhoto)
                holder.viewBinding.tvUserName.text = model.userName

                holder.itemView.setOnClickListener {
                    val intent= Intent(context, ChatActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, model)
                    context.startActivity(intent)
                }
            }
        }


        override fun getItemCount(): Int {
            return list.size
        }

    open fun filterList(filteredList: ArrayList<User>) {
        list = filteredList
        notifyDataSetChanged()
    }

        class MyViewHolder(var viewBinding: UserListLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root)

    }