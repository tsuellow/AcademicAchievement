package com.example.android.academicachievement.presentation.enroll_scan

import com.example.android.academicachievement.data.remote.dto.CourseTemplateDto
import com.example.android.academicachievement.domain.model.Course

data class CourseState(
    val isLoading:Boolean=false,
    val course: Course=Course(),
    val error:String=""
)
