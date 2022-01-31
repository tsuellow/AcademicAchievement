package com.example.android.academicachievement.presentation.validate_scan

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.presentation.enroll_scan.EnrollScanViewModel
import com.example.android.academicachievement.presentation.ui.composables.ScannerScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@ExperimentalComposeUiApi
@Composable
fun ValidateScanner (
    viewModel: ValidateScannerViewModel= hiltViewModel(),
    navController: NavController
) {
    val state=viewModel.state

    fun onScan(id:Int){
        navController.navigate(Screen.ValidateCourse.withId(id))
    }

    Box(modifier = Modifier.fillMaxSize()){
        ScannerScreen(state, onScan = {id->onScan(id)})
    }
}