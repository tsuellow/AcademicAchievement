package com.example.android.academicachievement.domain.model

import com.example.android.academicachievement.data.remote.dto.CourseDto

data class Course(val key:String="", val name:String="", var completed:Boolean=false, val parts:HashMap<String,Part> = hashMapOf()){

    fun toCourseDto():CourseDto{
        return CourseDto(key,name, completed, HashMap(parts.mapValues { it.value.toPartDto() }))
    }
}
