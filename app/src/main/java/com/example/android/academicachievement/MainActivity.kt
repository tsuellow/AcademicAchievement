package com.example.android.academicachievement


import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.presentation.enroll_choose_course.components.CourseScreen
import com.example.android.academicachievement.presentation.enroll_choose_course.components.Test
import com.example.android.academicachievement.presentation.enroll_scan.components.EnrollScanner
import com.example.android.academicachievement.presentation.ui.composables.MainScreen
import com.example.android.academicachievement.presentation.ui.composables.ScannerScreen
import com.example.android.academicachievement.presentation.ui.theme.AcademicAchievementTheme
import com.example.android.academicachievement.presentation.validate_scan.ValidateScanner
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var navController: NavHostController

    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContent{
            AcademicAchievementTheme{
                navController= rememberNavController()
                SetUpNavGraph(navController = navController)
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun SetUpNavGraph(navController:NavHostController){
    NavHost(navController = navController, startDestination = Screen.Main.route){
        composable(Screen.Main.route){
            MainScreen(navController = navController)
        }
        composable(Screen.Courses.route){
            CourseScreen(navController = navController)
        }
        composable(Screen.EnrollScanner.route+"/{courseId}"){
            EnrollScanner ()
        }
        composable(Screen.ValidateScanner.route){
            ValidateScanner(navController = navController)
        }
        composable(Screen.ValidateCourse.route+"/{studentId}"){
            Test()
        }
    }
}
