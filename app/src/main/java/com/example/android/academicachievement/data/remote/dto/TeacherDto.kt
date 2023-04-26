package com.example.android.academicachievement.data.remote.dto

import com.example.android.academicachievement.domain.model.Teacher


data class TeacherDto(
    val login: String = "",
    val fullName: String = "",
    val phone: String = "",
    val pin: String = ""
) {

    fun toTeacher(): Teacher {
        return Teacher(login, fullName, phone, pin)
    }
}