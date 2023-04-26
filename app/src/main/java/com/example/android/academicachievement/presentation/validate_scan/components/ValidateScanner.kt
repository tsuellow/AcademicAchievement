package com.example.android.academicachievement.presentation.validate_scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.presentation.common_composables.ScannerScreen
import com.example.android.academicachievement.presentation.validate_course.components.ValidateCourseDialog


@ExperimentalComposeUiApi
@Composable
fun ValidateScanner (
    viewModel: ValidateScannerViewModel = hiltViewModel(),
    navController: NavController
) {
    val state=viewModel.state
    var showDialog by remember {
        mutableStateOf(false)
    }

    fun onScan(id:Int){
        navController.navigate(Screen.ValidateCourse.withId(id))
    }

    fun onMilestoneScan(id:Int,path:String){
        viewModel.setDialogState(path,"S$id")
        showDialog=true
        viewModel.pauseScanner(true)
    }
    fun onDismiss(){
        showDialog=false
        viewModel.pauseScanner(false)
    }

//    DisposableEffect(key1 = viewModel){
//        viewModel.pauseScanner(false)
//        onDispose {
//            //viewModel.pauseScanner(true)
//        }
//    }

    //example
    //{"aaid":27,"obj":"v","path":"C1/P1/m1"}

    Box(modifier = Modifier.fillMaxSize()){
        ScannerScreen(state, onScanIdOnly = { id->onScan(id)}, onScanMilestone = {id,path->onMilestoneScan(id,path)})
        if (showDialog){
            ValidateCourseDialog(
                validateDialogState = viewModel.validateDialogState.value,
                confirmDialogState = viewModel.confirmDialogState.value ,
                onSubmitButtonClick = {milestone -> viewModel.doOnConfirm(milestone,{onDismiss()})},
                onDismissRequest = {onDismiss()},
                loginData = viewModel.loginData)
        }
    }
}