package com.example.android.academicachievement.domain.model

import com.example.android.academicachievement.data.remote.dto.MilestoneDto

data class Milestone constructor(val key:String="0", var name:String="", var date:String="never", var completed:Boolean=false, var comment:String="",
                                 var grades:HashMap<String,Int> = hashMapOf("grade" to 7)){

    fun toMilestoneDto(): MilestoneDto {
        return MilestoneDto(key,name,date,completed,comment,grades)
    }
}

