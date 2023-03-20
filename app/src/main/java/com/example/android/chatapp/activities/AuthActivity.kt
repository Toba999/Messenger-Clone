package com.example.android.chatapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.android.chatapp.R
import com.example.android.chatapp.databinding.ActivityAuthBinding
import com.example.android.chatapp.fireStore.FireStoreClass
import com.example.android.chatapp.models.User
import com.example.android.chatapp.utils.Constants
import com.example.android.chatapp.utils.GlideLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.IOException

class AuthActivity : BaseActivity(),View.OnClickListener {
    private lateinit var binding : ActivityAuthBinding

    private var mSelectedImageFileUri: Uri? = null
    private var mUserProfileImageURL: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvHaveAccount.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRegister.setOnClickListener (this)

        // Assign the on click event to the user profile photo.
        binding.ivUserPhoto.setOnClickListener(this)

    }

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etRegisterUserName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_user_name), true)
                false
            }

            TextUtils.isEmpty(binding.etRegisterEmail.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(binding.etRegisterPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(binding.etRegisterCmPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
                false
            }

            binding.etRegisterPassword.text.toString().trim { it <= ' ' } != binding.etRegisterCmPassword.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_and_confirm_password_mismatch), true)
                false
            }

            else -> {
                true
            }
        }
    }

    private fun registerUser() {
        // Check with validate function if the entries are valid or not.
        if (validateRegisterDetails()) {

            val email: String = binding.etRegisterEmail.text.toString().trim { it <= ' ' }
            val password: String = binding.etRegisterPassword.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // If the registration is successfully done
                    if (task.isSuccessful) {
                        // Firebase registered user
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val user = User(
                            firebaseUser.uid,
                            binding.etRegisterUserName.text.toString().trim { it <= ' ' },
                            binding.etRegisterEmail.text.toString().trim { it <= ' ' },
                            mUserProfileImageURL
                        )

                        FireStoreClass().registerUser(this, user)

                    } else {
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    fun userRegistrationSuccess(){
        hideProgressDialog()
        Toast.makeText(this,
            resources.getString(R.string.register_success), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LatestMessagesActivity::class.java)
        intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.iv_user_photo -> {
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                       showImageChooser()
                    } else {

                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }
                R.id.btn_register -> {
                    showProgressDialog(resources.getString(R.string.please_wait))
                    FireStoreClass().uploadImageToCloudStorage(
                        this, mSelectedImageFileUri, Constants.USER_PROFILE_IMAGE)
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun showImageChooser() {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launches the image selection of phone storage using the constant code.

        resultLauncher.launch(galleryIntent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                try {
                    // The uri of selected image from phone storage.
                    mSelectedImageFileUri = result.data!!.data!!
                    GlideLoader(this).loadUserPicture(
                        mSelectedImageFileUri!!,
                        binding.ivUserPhoto)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        resources.getString(R.string.image_selection_failed),
                        Toast.LENGTH_SHORT).show()
                }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }


    fun imageUploadSuccess(imageURL: String) {
        mUserProfileImageURL = imageURL
        registerUser()
    }
}