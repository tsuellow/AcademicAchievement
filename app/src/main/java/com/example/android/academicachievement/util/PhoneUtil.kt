package com.example.android.academicachievement.util

fun depuratePhone(rawPhone: String): String {
    val phone = rawPhone.replace(" ", "").replace("+", "00").replace("-", "")
    var depPhone: String? = null
    if (phone.length > 2) {
        depPhone = if (phone.substring(0, 2).contentEquals("00")) {
            phone
        } else {
            "00505$phone"
        }
    }
    return depPhone?:""
}