package com.example.android.academicachievement.presentation.enroll_choose_course

import com.example.android.academicachievement.data.remote.dto.CourseTemplateDto

data class CourseListState(
    val isLoading:Boolean=false,
    val courseList:HashMap<String,CourseTemplateDto> = HashMap(),
    val error:String=""
)
