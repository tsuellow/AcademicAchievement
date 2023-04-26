package com.example.android.academicachievement.presentation.student_profile_scan.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.presentation.student_profile_scan.ScanStudentViewModel
import com.example.android.academicachievement.presentation.common_composables.ScannerScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StudentScanner(
    viewModel:ScanStudentViewModel= hiltViewModel(),
    navController:NavController
){
    val state=viewModel.state

    fun onScanIdOnly(id:Int){
        navController.navigate(Screen.StudentProfile.withId(id))
    }

    fun onMilestoneScan(id:Int,path:String){
        navController.navigate(Screen.StudentProfile.withId(id))
    }

    DisposableEffect(key1 = viewModel){
        viewModel.pauseScanner(false)
        onDispose {
            viewModel.pauseScanner(true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        ScannerScreen(state, onScanIdOnly = { id->onScanIdOnly(id)}, onScanMilestone = {id,_path->onMilestoneScan(id,_path)})
    }
}