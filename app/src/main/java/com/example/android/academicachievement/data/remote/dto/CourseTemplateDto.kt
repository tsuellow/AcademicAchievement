package com.example.android.academicachievement.data.remote.dto

data class CourseTemplateDto(var name:String="", val parts:HashMap<String,PartTemplateDto> = HashMap()){

    fun toCourseDto(key:String):CourseDto{
        return CourseDto(key = key, name = name, parts = HashMap(parts.mapValues { it.value.toPartDto(it.key) }))
    }

    fun reKeyParts(){
        val partList=parts.toSortedMap().map { it.value }
        val newParts:HashMap<String,PartTemplateDto> = hashMapOf()
        for (i in partList.indices){
            newParts["P${i+1}"]=partList[i]
        }
        parts.clear()
        parts.putAll(newParts)
    }

    fun keyForNextPart():String{
        val next=parts.size+1
        return "P$next"
    }
}
