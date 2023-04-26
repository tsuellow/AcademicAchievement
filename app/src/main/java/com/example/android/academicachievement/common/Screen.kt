package com.example.android.academicachievement.common

sealed class Screen(val route:String){
    object Main: Screen("main")
    object ValidateScanner: Screen("validatescanner")
    object ValidateCourse: Screen("validatecourse"){
        fun withId(id:Int):String{
            return "$route/S$id"
        }
    }
    object EnrollScanner: Screen("enrollscanner")
    object Courses: Screen("courses")
    object EditCourseList: Screen("editcourselist")
    object EditCourse: Screen("editcourse"){
        fun withId(courseId:String):String{
            return "$route/$courseId"
        }
    }
    object StudentScanner: Screen("studentscanner")
    object StudentProfile: Screen("studentprofile"){
        fun withId(id:Int):String{
            return "$route/S$id"
        }
    }
    object AddTeacher: Screen("addteacher")
    object Login: Screen("login")

}
