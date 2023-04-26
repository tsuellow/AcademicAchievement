package com.example.android.academicachievement.domain.model

import com.example.android.academicachievement.data.remote.dto.MilestoneDto

data class Milestone constructor(val key:String="0", var name:String="", var date:String="never", var validatedBy:String="none", var completed:Boolean=false, var comment:String="",
                                 var grades:HashMap<String,Int> = hashMapOf("grade" to 7)){

    fun toMilestoneDto(): MilestoneDto {
        return MilestoneDto(key,name,date,validatedBy,completed,comment,grades)
    }

    fun copy():Milestone{
        return Milestone(key,name,date,validatedBy,completed,comment,HashMap(grades))
    }

}

