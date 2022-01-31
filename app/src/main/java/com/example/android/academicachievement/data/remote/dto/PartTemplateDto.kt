package com.example.android.academicachievement.data.remote.dto

data class PartTemplateDto(var name:String="", val milestones:HashMap<String,String> = HashMap(),
                           val grades:HashMap<String,Int> = hashMapOf("grade" to 7)){

    fun toPartDto(key:String):PartDto{
        return PartDto(key = key, name = name, milestones = HashMap(milestones.mapValues { MilestoneDto(key=it.key,name=it.value, grades = grades) }))
    }
}
