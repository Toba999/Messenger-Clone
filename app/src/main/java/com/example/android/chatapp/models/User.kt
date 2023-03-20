package com.example.android.chatapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class User (
        var id:String="",
        val userName:String="",
        val email:String="",
        val image:String="",
        var state :String = "offline"): Parcelable
