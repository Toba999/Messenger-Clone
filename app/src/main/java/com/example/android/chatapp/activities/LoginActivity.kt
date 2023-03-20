package com.example.android.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.example.android.chatapp.R
import com.example.android.chatapp.databinding.ActivityLoginBinding
import com.example.android.chatapp.fireStore.FireStoreClass
import com.example.android.chatapp.models.User
import com.example.android.chatapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity(),View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRegister.setOnClickListener (this)
        binding.btnLogin.setOnClickListener (this)
        FirebaseAuth.getInstance().signOut()

    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.btn_login -> {
                    logInRegisteredUser()
                }
                R.id.tv_register -> {
                    // Launch the register screen when the user clicks on the text.
                    val intent = Intent(this@LoginActivity, AuthActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etLoginEmail.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(binding.etLoginPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun logInRegisteredUser() {

        if (validateLoginDetails()) {

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            // Get the text from editText and trim the space
            val email = binding.etLoginEmail.text.toString().trim { it <= ' ' }
            val password = binding.etLoginPassword.text.toString().trim { it <= ' ' }

            // Log-In using FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // Hide the progress dialog
                    if (task.isSuccessful) {
                        FireStoreClass().getUserDetails(this)
                    } else {
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    fun userLoggedInSuccess(user: User){
        hideProgressDialog()
         // If the user profile is incomplete then launch the UserProfileActivity.
        val intent = Intent(this@LoginActivity, LatestMessagesActivity::class.java)
        intent.putExtra(Constants.EXTRA_USER_DETAILS,user)
        intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}