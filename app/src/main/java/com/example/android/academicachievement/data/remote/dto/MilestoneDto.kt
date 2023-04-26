package com.example.android.academicachievement.data.remote.dto

import com.example.android.academicachievement.domain.model.Milestone

data class MilestoneDto constructor(val key:String="0", var name:String="", var date:String="never", var validatedBy:String="none", var completed:Boolean=false, var comment:String="",
                        val grades:HashMap<String,Int> = hashMapOf("grade" to 7)){

    fun toMilestone(): Milestone{
        return Milestone(key,name,date,validatedBy,completed,comment,grades)
    }
}

