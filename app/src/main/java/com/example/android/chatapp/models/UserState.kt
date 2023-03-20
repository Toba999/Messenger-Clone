package com.example.android.chatapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserState (
        var date:String="",
        val id :String="",
        var state:String="",
        val time:String=""): Parcelable
