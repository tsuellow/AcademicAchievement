package com.example.android.academicachievement.presentation.login

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.PreferenceManager
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.domain.model.Teacher
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    val repository: CourseRepository,
    private val preferences: PreferenceManager
) : ViewModel() {

    private var auth: FirebaseAuth = Firebase.auth

    lateinit var loginData: PreferenceManager.LoginData

    private val _loginState: MutableState<LoginState> =
        mutableStateOf<LoginState>(LoginState.LoggingIntoFirebase)
    val loginState: State<LoginState> = _loginState

    init {
        val user=auth.currentUser
        //getPrefs()
        _loginState.value=LoginState.LoggingIntoFirebase
//        if (user!=null){
//            getPrefs()
//        }else{
//            _loginState.value=LoginState.LoggingIntoFirebase
//        }
    }
companion object{
    const val REQUEST_CODE=3
}


    fun logInToFirebase(activity: Activity){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("591200735231-ms0o6sk3tmlbd3ksusjbfjr8g293a1nc.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val signInClient=GoogleSignIn.getClient(activity,gso)
        val intent=signInClient.signInIntent
        activity.startActivityForResult(intent, REQUEST_CODE)
    }

    fun getPrefs() {
        viewModelScope.launch {
            loginData = preferences.getLoginData()
            if (loginData.login.isEmpty() || loginData.pin.isEmpty()) {
                _loginState.value = LoginState.IsNew
            } else {
                _loginState.value = LoginState.LoggingInAs(loginData.login)
                _loginState.value = getTeachers(loginData) {} //no persisting needed when login data was already there
            }
        }
    }

    fun attemptLogin(loginData: PreferenceManager.LoginData) {
        _loginState.value = LoginState.LoggingInAs(loginData.login)
        viewModelScope.launch {
            val result = getTeachers(loginData) { teacher ->
                if(!teacher.login.contentEquals("admin")){
                    preferences.saveLoginData(
                        PreferenceManager.LoginData(
                            teacher.login,
                            teacher.fullName,
                            teacher.pin
                        )
                    )
                }
            }
            _loginState.value = result
        }
    }

    private suspend fun getTeachers(
        loginData: PreferenceManager.LoginData,
        persistCredentials: suspend (thisTeacher: Teacher) -> Unit
    ): LoginState {
        delay(2000)
        val res = repository.getTeachers()
        return if (res is Resource.Success) {
            val list = res.data!!
            val thisTeacher = list[loginData.login]?.toTeacher()
            if (thisTeacher != null) {
                if (thisTeacher.pin.contentEquals(loginData.pin)) {
                    val adminPin = list["admin"]!!.pin
                    preferences.saveAdminPin(adminPin)//everytime a login happens admin pin gets updated in shared prefs
                    persistCredentials(thisTeacher)
                    LoginState.LoginSuccess
                } else {
                    LoginState.LoginFailed(error = "wrong credentials")
                }
            } else {
                LoginState.LoginFailed(error = "wrong credentials")
            }
        } else {
            LoginState.LoginFailed(error = res.message ?: "connection error try again",loginData)
        }
    }

    fun resetLoginState(){
        _loginState.value=LoginState.Preparing
    }

    fun finishLogin(task: Task<GoogleSignInAccount>) {
        Log.d("cagada","biut finish was called")
        try {
            val account:GoogleSignInAccount?=task.getResult(ApiException::class.java)
            account?.idToken?.let {
                Log.d("cagada","token existed")
                val credentials=GoogleAuthProvider.getCredential(it,null)
                auth.signInWithCredential(credentials).addOnCompleteListener { signInTask->
                    if (signInTask.isSuccessful){
                        getPrefs()
                        Log.d("cagada","was successful")

                    }
                }
            }
        }catch (e:ApiException){
            e.printStackTrace()
            Log.d("cagada","failed on catch")
        }
    }


}

sealed class LoginState() {
    object Preparing : LoginState()
    object LoggingIntoFirebase:LoginState()
    object IsNew : LoginState()
    data class LoggingInAs(val login: String) : LoginState()
    object LoginSuccess : LoginState()
    data class LoginFailed(val error: String, val credentials:PreferenceManager.LoginData?=null) : LoginState()
}