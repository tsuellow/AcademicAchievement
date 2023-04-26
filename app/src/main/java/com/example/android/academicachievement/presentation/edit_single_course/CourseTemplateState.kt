package com.example.android.academicachievement.presentation.edit_single_course

import com.example.android.academicachievement.data.remote.dto.CourseTemplateDto

sealed class CourseTemplateState(){
    object Loading:CourseTemplateState()
    data class Failed(val error:String):CourseTemplateState()
    data class Success(val courseTemplate:CourseTemplateDto):CourseTemplateState()
}
