package com.example.android.academicachievement.data.remote.dto

import com.example.android.academicachievement.domain.model.Part

data class PartDto(val key:String="", val name:String="", var completed:Boolean=false, val milestones:HashMap<String,MilestoneDto> = hashMapOf()){

    fun toPart():Part{
        return Part(key,name, completed, HashMap(milestones.mapValues { it.value.toMilestone() }))
    }
}
