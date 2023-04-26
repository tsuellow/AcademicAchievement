package com.example.android.academicachievement.domain.model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.example.android.academicachievement.data.remote.dto.StudentDto
import com.example.android.annotations.AsState
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


@AsState
data class Student(
    val key: String,
    val firstName: String = "",
    val lastName: String = "",
    val dob: String = "",
    val gender: String = "",
    val occupation: String = "",
    val city: String = "",
    val pin: String = generatePin(),
    val phone: String = "",
    var photoPath: String = ""
) {
    fun toStudentDto(): StudentDto {
        return StudentDto(
            key,
            firstName,
            lastName,
            dob,
            gender,
            occupation,
            city,
            pin,
            phone,
            photoPath
        )
    }

    companion object{
        fun generatePin():String{
            val base="ABCDEFGHIJKLMNPQRSTUVWXYZ123456789"
            var pin=""
            for (i in 0..3){
                pin += base[Random.nextInt(0, base.length)]
            }
            return pin
        }
    }
}



