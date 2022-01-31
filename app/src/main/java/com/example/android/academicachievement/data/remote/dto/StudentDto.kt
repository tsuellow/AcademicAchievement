package com.example.android.academicachievement.data.remote.dto

import com.example.android.academicachievement.domain.model.Student

data class StudentDto(val key:String="", val firstName:String="", val lastName:String="", val dob:String="", val gender:String="", val occupation:String="", val phone:String="", val photoPath:String=""){
    fun toStudent():Student{
        return Student(key, firstName, lastName,dob,gender,occupation,phone,photoPath)
    }
}
