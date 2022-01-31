package com.example.android.academicachievement.presentation.enroll_scan.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.academicachievement.presentation.enroll_scan.EnrollScanViewModel
import com.example.android.academicachievement.presentation.ui.composables.ScannerScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@ExperimentalComposeUiApi
@Composable
fun EnrollScanner (
    viewModel: EnrollScanViewModel= hiltViewModel()
) {
    val context=LocalContext.current
    val state=viewModel.courseState
    val scannerState=viewModel.scannerState

    var showDialog by remember { mutableStateOf(false)}

    fun onScan(id:Int){
        viewModel.pauseScanner(true)
        viewModel.getCurrentStudentData("S$id")
        showDialog=true
    }

    Box(modifier = Modifier.fillMaxSize()){
        ScannerScreen(scannerState, onScan = {id->onScan(id)})
        if (showDialog){
            EnrollDialog(
                dialogState = viewModel.dialogState,
                confirmDialogState = viewModel.confirmDialogState.value,
                onSubmitButtonClick = { viewModel.doOnConfirm { showDialog=false }},
                onDismissRequest = { showDialog=false
                    viewModel.pauseScanner(false)})
        }

    }




}