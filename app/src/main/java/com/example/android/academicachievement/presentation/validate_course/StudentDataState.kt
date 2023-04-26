package com.example.android.academicachievement.presentation.validate_course

import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.model.Student

sealed class StudentDataState(){
    object Loading:StudentDataState()
    object Inexistent:StudentDataState()
    data class Success(var personalData: Student?=null):StudentDataState()
    data class Failed(var error:String?=null):StudentDataState()
}