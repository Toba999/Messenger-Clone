package com.example.android.chatapp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val USERNAME: String="userName"
    const val ID: String = "id"
    const val MESSAGES: String = "messages"
    const val EXTRA_User_Name: String = "extra_user_name"
    const val EXTRA_USER_ID: String = "extra_user_id"
    const val EXTRA_USER_DETAILS: String ="extra_user_details"

    // Firebase Constants
    // This is used for the collection name for USERS.
    const val USERS: String = "users"
    const val TiME :String = "time"
    const val DATE :String = "date"
    const val STATE :String = "state"


    const val CHAT_APP_PREFERENCES: String = "ChatAppPrefs"
    const val LOGGED_IN_USERNAME: String = "logged_in_username"
    //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult in the Base Activity.
    const val READ_STORAGE_PERMISSION_CODE = 2

    // A unique code of image selection from Phone Storage.
    const val PICK_IMAGE_REQUEST_CODE = 2

    //Declare a global constant variable to notify the add address.
    const val ADD_ADDRESS_REQUEST_CODE: Int = 121

    // Firebase database field names
    const val MOBILE: String = "mobile"
    const val GENDER: String = "gender"
    const val IMAGE: String = "image"
    const val COMPLETE_PROFILE: String = "profileCompleted"

    const val USER_ID: String = "user_id"
    const val USER_PROFILE_IMAGE: String = "User_Profile_Image"



    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}