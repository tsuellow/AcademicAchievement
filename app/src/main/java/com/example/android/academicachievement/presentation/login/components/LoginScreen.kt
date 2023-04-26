package com.example.android.academicachievement.presentation.login.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.MainActivity
import com.example.android.academicachievement.R
import com.example.android.academicachievement.presentation.common_composables.OutlinedTextFieldValidation
import com.example.android.academicachievement.common.PreferenceManager
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.presentation.common_composables.MyTopBar
import com.example.android.academicachievement.presentation.login.LoginState
import com.example.android.academicachievement.presentation.login.LoginViewModel
import com.example.android.academicachievement.presentation.ui.theme.MyRed

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navController: NavController
) {
    val state=viewModel.loginState.value
    if(state is LoginState.LoginSuccess){
        LaunchedEffect( Unit ){
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            viewModel.resetLoginState()
        }
    }
    if(state is LoginState.Preparing){
        LaunchedEffect( Unit ){
            viewModel.getPrefs()
        }
    }

    Scaffold(topBar = { MyTopBar(title = "Login") }) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo"
            )

            if(state is LoginState.Preparing || state is LoginState.LoggingInAs || state is LoginState.LoggingIntoFirebase){

                if( state is LoginState.LoggingIntoFirebase){
                    Text(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colors.primaryVariant,
                        text = "Firebase Authentication"
                    )
                    val activity=LocalContext.current as MainActivity
                    LaunchedEffect(Unit){
                        viewModel.logInToFirebase(activity = activity)
                    }

                }


                if( state is LoginState.LoggingInAs){
                    Text(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colors.primaryVariant,
                        text = "Logging in as:"
                    )
                    Text(
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.secondary,
                        text = state.login
                    )
                }

                Spacer(modifier = Modifier.height(64.dp))

                CircularProgressIndicator(modifier = Modifier.size(100.dp))

            }

            if( state is LoginState.LoginFailed){
                Text(
                    style = MaterialTheme.typography.h6,
                    color = MyRed,
                    text = "Error:\n${state.error}"
                )
                Spacer(modifier = Modifier.height(64.dp))
                InputFields(onSendLoginData = {loginData-> viewModel.attemptLogin(loginData)}, credentials = state.credentials, isRetry = true)
            }

            if(state is LoginState.IsNew ){
                Spacer(modifier = Modifier.height(64.dp))

                InputFields(onSendLoginData = {loginData-> viewModel.attemptLogin(loginData)})

            }
        }
    }

}

@Composable
fun InputFields(onSendLoginData: (PreferenceManager.LoginData) -> Unit, credentials:PreferenceManager.LoginData?=null, isRetry:Boolean=false) {

    var login by remember { mutableStateOf(credentials?.login?:"") }
    var loginError by remember { mutableStateOf("") }
    OutlinedTextFieldValidation(
        value = login,
        onValueChange = { login = it },
        label = { Text(text = "Login") },
        error = loginError
    )

    var pin by remember { mutableStateOf(credentials?.pin?:"") }
    var pinError by remember { mutableStateOf("") }
    OutlinedTextFieldValidation(
        value = pin,
        onValueChange = { pin = it },
        label = { Text(text = "PIN") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
        error = pinError,
        visualTransformation = PasswordVisualTransformation()
    )

    Button(
        modifier= Modifier
            .fillMaxWidth(0.8f)
            .padding(8.dp),
        onClick = {
            fun check(): Boolean {
                if (login.isEmpty())
                    loginError = "cannot be empty"

                if (pin.isEmpty())
                    pinError = "cannot be empty"

                return login.isNotEmpty() && pin.isNotEmpty()
            }
            if (check()) {
                onSendLoginData(PreferenceManager.LoginData(login = login.trim(), pin = pin))
            }
        }
    ) {
        Text(text = if (isRetry) "Retry" else "Continue")
    }


}