package com.example.android.academicachievement.domain.model

import com.example.android.academicachievement.data.remote.dto.StudentDto

data class Student(val key:String, val firstName:String, val lastName:String, val dob:String, val gender:String, val occupation:String, val phone:String, val photoPath:String){
    fun toStudentDto(): StudentDto {
        return StudentDto(key, firstName, lastName,dob,gender,occupation, phone, photoPath)
    }
}
