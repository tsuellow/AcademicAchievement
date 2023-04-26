package com.example.android.academicachievement


import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.presentation.add_teacher.components.AddTeacher
import com.example.android.academicachievement.presentation.edit_courses.components.EditCourseList
import com.example.android.academicachievement.presentation.edit_single_course.components.EditCourse
import com.example.android.academicachievement.presentation.enroll_choose_course.components.CourseScreen
import com.example.android.academicachievement.presentation.enroll_scan.components.EnrollScanner
import com.example.android.academicachievement.presentation.login.LoginViewModel
import com.example.android.academicachievement.presentation.login.components.LoginScreen
import com.example.android.academicachievement.presentation.main.components.MainScreen
import com.example.android.academicachievement.presentation.student_profile_scan.components.StudentScanner
import com.example.android.academicachievement.presentation.student_profile_view_create.components.StudentProfile
import com.example.android.academicachievement.presentation.ui.theme.AcademicAchievementTheme
import com.example.android.academicachievement.presentation.validate_course.components.ValidateScreen
import com.example.android.academicachievement.presentation.validate_scan.ValidateScanner
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.log

@ExperimentalComposeUiApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var navController: NavHostController

    lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //createLauncher()

        setContent{
            AcademicAchievementTheme{
                navController= rememberNavController()
                loginViewModel = hiltViewModel()
                SetUpNavGraph(navController = navController, loginViewModel = loginViewModel)
            }
        }
        requestPermissions(this)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==3) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            loginViewModel.finishLogin(task)
        }
    }

    fun createLauncher(){
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent


        val signInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { res ->
            onSignInResult(res)
        }

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            setContent{
                AcademicAchievementTheme{
                    navController= rememberNavController()
                    SetUpNavGraph(navController = navController, loginViewModel = loginViewModel)
                }
            }
            requestPermissions(this)
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun SetUpNavGraph(navController:NavHostController, loginViewModel:LoginViewModel){
    NavHost(navController = navController, startDestination = Screen.Login.route){
        composable(Screen.Login.route){
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        composable(Screen.Main.route){
            MainScreen(navController = navController, loginViewModel = loginViewModel)
        }
        composable(Screen.Courses.route){
            CourseScreen(navController = navController)
        }
        composable( Screen.EnrollScanner.route+"/{courseId}"){
            EnrollScanner (navController = navController)
        }
        composable(Screen.ValidateScanner.route){
            ValidateScanner(navController = navController)
        }
        composable(Screen.ValidateCourse.route+"/{studentId}"){
            ValidateScreen(navController = navController)
        }
        composable(Screen.EditCourseList.route){
            EditCourseList(navController = navController)
        }
        composable(Screen.EditCourse.route+"/{courseId}"){
            EditCourse(navController=navController)
        }
        composable(Screen.StudentScanner.route){
            StudentScanner(navController = navController)
        }
        composable(Screen.StudentProfile.route+"/{studentId}"){
            StudentProfile()
        }
        composable(Screen.AddTeacher.route){
            AddTeacher()
        }
    }
}

fun requestPermissions(context: Activity){

    // check permissions CONSIDER MOVING
    ActivityCompat.requestPermissions(
        context,
        arrayOf(
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.CAMERA
        ),
        112
    )

}
