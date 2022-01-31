package com.example.android.academicachievement.presentation.validate_course

import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.model.Student

sealed class CurrentDataState(){
    object Loading:CurrentDataState()
    data class Success(var personalData: Student?=null,
                       var currentCourse: Course?=null):CurrentDataState()
    data class Failed(var error:String?=null):CurrentDataState()
}