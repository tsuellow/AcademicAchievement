package com.example.android.academicachievement.common

sealed class Screen(val route:String){
    object Main: Screen("main")
    object ValidateScanner: Screen("validatescanner")
    object ValidateCourse: Screen("validatecourse"){
        fun withId(id:Int):String{
            return "$route/$id"
        }
    }
    object EnrollScanner: Screen("enrollscanner")
    object Courses: Screen("courses")

    object Test: Screen("test")

}
