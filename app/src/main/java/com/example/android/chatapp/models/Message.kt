package com.example.android.chatapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Message (
        var id:String="",
        val sender:String="",
        val receiver:String="",
        val text:String="",
        val date : String="",
        var seen :Boolean = false): Parcelable
