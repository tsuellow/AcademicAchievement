package com.example.android.academicachievement.presentation.enroll_scan.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.presentation.enroll_scan.EnrollScanViewModel
import com.example.android.academicachievement.presentation.common_composables.ScannerScreen


@ExperimentalComposeUiApi
@Composable
fun EnrollScanner(
    navController:NavController,
    viewModel: EnrollScanViewModel = hiltViewModel()
) {
    var currentId by remember {
        mutableStateOf(0)
    }
    val scannerState = viewModel.scannerState

    var showDialog by remember { mutableStateOf(false) }

    fun onScan(id: Int) {
        currentId = id
        viewModel.pauseScanner(true)
        viewModel.getCurrentStudentData("S$id")
        showDialog = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ScannerScreen(
            scannerState,
            onScanIdOnly = { id -> onScan(id) },
            onScanMilestone = { id, _path -> onScan(id) })
        if (showDialog) {
            if (viewModel.dialogState.value.inexistent){
                StudentInexistentDialog(
                    studentId = currentId,
                    hideDialog = {
                        showDialog=false
                        viewModel.pauseScanner(false)
                    },
                    doOnConfirm = {
                        showDialog=false
                        viewModel.pauseScanner(false)
                        navController.navigate(Screen.StudentProfile.withId(currentId))
                    }
                )
            }else{
                EnrollDialog(
                    dialogState = viewModel.dialogState,
                    confirmDialogState = viewModel.confirmDialogState.value,
                    onSubmitButtonClick = { viewModel.doOnConfirm { showDialog = false } },
                    onDismissRequest = {
                        showDialog = false
                        viewModel.pauseScanner(false)
                    },
                    authorizationPin = viewModel.loginData.pin
                )
            }
        }
    }
}

@Composable
fun StudentInexistentDialog(studentId: Int, hideDialog: () -> Unit, doOnConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = { hideDialog() },
        confirmButton = {
            Button(onClick = {
                hideDialog()
                doOnConfirm()
            }) {
                Text(text = "create profile S$studentId")
            }
        },
        dismissButton = {
            Button(onClick = { hideDialog() }) {
                Text(text = "close")
            }
        },
        title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Student S$studentId doesn't exist!",
                    style = MaterialTheme.typography.h5
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No student profile with this ID was found. Please make sure that the student ID is correct. " +
                        "If this student has not yet been registered you can do it by pressing create profile")
            }

        }
    )
}