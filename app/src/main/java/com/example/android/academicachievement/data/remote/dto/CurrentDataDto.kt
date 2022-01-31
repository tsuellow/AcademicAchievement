package com.example.android.academicachievement.data.remote.dto

import com.example.android.academicachievement.domain.model.Student

data class CurrentDataDto(val personalData: StudentDto?=null, val currentCourse:CourseDto?= null)
