package com.example.android.academicachievement.presentation.validate_course

import com.example.android.academicachievement.domain.model.Course

sealed class CurrentCourseState(){
    object Loading:CurrentCourseState()
    data class Success(var course: Course?=null):CurrentCourseState()
    object NoCourse:CurrentCourseState()
    data class Failed(var error:String?=null):CurrentCourseState()
}