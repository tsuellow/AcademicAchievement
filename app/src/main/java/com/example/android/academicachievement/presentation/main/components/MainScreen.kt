package com.example.android.academicachievement.presentation.main.components


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.presentation.common_composables.OutlinedTextFieldValidation
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.presentation.login.LoginViewModel
import com.example.android.academicachievement.presentation.main.MainViewModel


@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    loginViewModel:LoginViewModel,
    navController: NavController
) {

    if (viewModel.credentialsInvalidated.value){
        LaunchedEffect(Unit){
            loginViewModel.resetLoginState()
            navController.navigate(Screen.Login.route){
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = { MenuTopAppBar(viewModel = viewModel, navController = navController) },
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,

            modifier = Modifier
                .fillMaxSize()
                .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
                .background(color = Color.White)
                .padding(8.dp)
        ) {

            Image(
                painter = painterResource(id = com.example.android.academicachievement.R.drawable.logo),
                contentDescription = "logo"
            )

            Spacer(modifier = Modifier.padding(24.dp))

            if (viewModel.fullName.value.isNotEmpty()){
                Text(
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colors.primaryVariant,
                    text = "Welcome"
                )
                Text(
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.secondary,
                    text = viewModel.fullName.value
                )
            }
            Spacer(modifier = Modifier.padding(16.dp))

            Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(Screen.Courses.route)
                    }) {
                    Text(text = "Enroll Student")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Filled.FolderShared, contentDescription = "enroll")
                }

                Spacer(modifier = Modifier.padding(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(Screen.ValidateScanner.route)
                    }) {
                    Text(text = "Validate Milestone")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Filled.FactCheck, contentDescription = "validate")
                }

                Spacer(modifier = Modifier.padding(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(Screen.StudentScanner.route)
                    },
                ) {
                    Text(text = "View/Edit Student")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = "add student")
                }

                Spacer(modifier = Modifier.padding(32.dp))

            }
        }
    }

}

@Composable
fun MenuTopAppBar(viewModel: MainViewModel, navController: NavController) {
    var expanded by remember {
        mutableStateOf(false)
    }

    var showEditCourseDialog by remember {
        mutableStateOf(false)
    }
    var showAddTeacherDialog by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        title = { Text(text = "Academic AA - Home") },
        actions = {
            IconButton(
                onClick = {
                    expanded=true
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "menu"
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(onClick = {
                    showEditCourseDialog=true
                    expanded=false
                }) {
                    Text(text = "Edit courses")
                }
                DropdownMenuItem(onClick = {
                    showAddTeacherDialog=true
                    expanded=false
                }) {
                    Text(text = "Add teachers")
                }
                DropdownMenuItem(onClick = {
                    viewModel.forgetLoginData()
                    expanded=false

                }) {
                    Text(text = "Logout")
                }
            }
        }
    )

    if (showEditCourseDialog) {
        AdminDialog(
            viewModel = viewModel,
            hideDialog = { showEditCourseDialog = false },
            doOnConfirm = {
                navController.navigate(Screen.EditCourseList.route)
            })
    }

    if (showAddTeacherDialog) {
        AdminDialog(
            viewModel = viewModel,
            hideDialog = { showAddTeacherDialog = false },
            doOnConfirm = { navController.navigate(Screen.AddTeacher.route) })
    }


}

@Composable
fun AdminDialog(viewModel: MainViewModel, hideDialog: () -> Unit, doOnConfirm: () -> Unit) {
    val context = LocalContext.current
    var pin by remember {
        mutableStateOf("")
    }
    var error by remember {
        mutableStateOf("")
    }

    AlertDialog(onDismissRequest = { hideDialog() },

        confirmButton = {
            Button(onClick = {
                if (viewModel.verifyAuthorization(pin)) {
                    doOnConfirm()
                    hideDialog()
                } else {
                    error="wrong PIN"
                }
            }) {
                Text(text = "Continue")
            }
        },
        dismissButton = {
            Button(onClick = { hideDialog() }) {
                Text(text = "Cancel")
            }
        },
        title = null,
        text = {


            Column(modifier = Modifier.fillMaxWidth()) {

                val focusRequester = remember { FocusRequester() }

                Text(text = "Authorization required", style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextFieldValidation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = pin,
                    onValueChange = {
                        pin = it
                        error=""
                    },
                    label = { Text(text = "Admin PIN") },
                    error = error,
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )

                LaunchedEffect(Unit){
                    focusRequester.requestFocus()
                }
            }

        }
    )
}