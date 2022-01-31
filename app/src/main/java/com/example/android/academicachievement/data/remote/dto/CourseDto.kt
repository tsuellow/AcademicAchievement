package com.example.android.academicachievement.data.remote.dto

import com.example.android.academicachievement.domain.model.Course

data class CourseDto(val key:String="", val name:String="", var completed:Boolean=false, val parts:HashMap<String,PartDto> =hashMapOf()){

    fun toCourse():Course{
        return Course(key,name, completed, HashMap(parts.mapValues { it.value.toPart() }))
    }
}
