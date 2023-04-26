package com.example.android.academicachievement.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun sendWhatsAppMsg(phone: String, text: String, context:Context) {
    try {
        var toNumber: String = depuratePhone(phone)
        toNumber = toNumber.replaceFirst("^0+(?!$)".toRegex(), "")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$toNumber&text=$text")
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}