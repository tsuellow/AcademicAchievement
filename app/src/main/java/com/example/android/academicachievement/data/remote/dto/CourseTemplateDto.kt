package com.example.android.academicachievement.data.remote.dto

data class CourseTemplateDto(val name:String="", val parts:HashMap<String,PartTemplateDto> = HashMap()){

    fun toCourseDto(key:String):CourseDto{
        return CourseDto(key = key, name = name, parts = HashMap(parts.mapValues { it.value.toPartDto(it.key) }))
    }
}
