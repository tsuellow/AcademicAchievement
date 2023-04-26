package com.example.android.academicachievement.util

import java.text.SimpleDateFormat
import java.util.*

fun Date.toDateString():String{
    return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(this)
}

fun Date.toDateTimeString():String{
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(this)
}

fun Date.toReadableDateString():String{
    return SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(this)
}

fun String.toDate():Date?{
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(this)
    }catch (e:Exception){
        null
    }
}