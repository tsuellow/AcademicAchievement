package com.example.android.academicachievement.data.remote.dto

data class PartTemplateDto(var name:String="", val milestones:HashMap<String,String> = HashMap(),
                           val gradingDimensions:HashMap<String,Int> = HashMap()){

    fun toPartDto(key:String):PartDto{
        return PartDto(key = key, name = name, milestones = HashMap(milestones.mapValues { MilestoneDto(key=it.key,name=it.value, grades = gradingDimensions
         ) }))
    }

    fun reKeyMilestones(){
        val msList=milestones.toSortedMap().map { it.value }
        val newMs:HashMap<String,String> = hashMapOf()
        for (i in msList.indices){
            newMs["m${i+1}"]=msList[i]
        }
        milestones.clear()
        milestones.putAll(newMs)
    }

    fun keyForNextMilestone():String{
        val next=milestones.size+1
        return "m$next"
    }
}
