package com.example.android.academicachievement.domain.model

import com.example.android.academicachievement.data.remote.dto.PartDto

data class Part(val key:String, val name:String, var completed:Boolean=false, val milestones:HashMap<String,Milestone>){

    fun toPartDto(): PartDto {
        return PartDto(key,name, completed, HashMap(milestones.mapValues { it.value.toMilestoneDto()}))
    }

    fun copy():Part{
        return Part(key,name, completed, HashMap(milestones.mapValues { it.value.copy()}))
    }
}
