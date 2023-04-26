package com.example.android.academicachievement.domain.model

import com.example.android.annotations.AsState

@AsState
data class Test(val name:String, val age:Int, val map:HashMap<String,Int>, val list:ArrayList<String>)
