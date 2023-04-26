package com.example.android.academicachievement.domain.model

import com.example.android.academicachievement.data.remote.dto.TeacherDto
import kotlin.random.Random

data class Teacher(val login:String, val fullName:String, val phone:String, val pin:String= generateNumberPin()){
    fun toTeacherDto():TeacherDto{
        return TeacherDto(login,fullName,phone,pin)
    }
}

fun generateNumberPin():String{
    val base="0123456789"
    var pin=""
    for (i in 0..3){
        pin += base[Random.nextInt(0, base.length)]
    }
    return pin
}