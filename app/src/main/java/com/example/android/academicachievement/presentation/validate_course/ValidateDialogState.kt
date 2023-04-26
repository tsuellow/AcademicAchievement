package com.example.android.academicachievement.presentation.validate_course

import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.model.Milestone
import com.example.android.academicachievement.domain.model.Student

sealed class ValidateDialogState(){
    object Loading:ValidateDialogState()
    data class Success(var personalData: Student?, var course: Course?, var path:String):ValidateDialogState()
    data class Failed(var error:String?=null):ValidateDialogState()
}
