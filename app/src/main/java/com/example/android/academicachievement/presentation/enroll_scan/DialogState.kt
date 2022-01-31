package com.example.android.academicachievement.presentation.enroll_scan

import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.model.Student

data class DialogState(var isLoading:Boolean=false,
                       var isApproved:Boolean=false,
                       var personalData:Student?=null,
                       var currentCourse: Course?=null,
                       var observations:ArrayList<String> = ArrayList(),
                       var error:String?=null)
