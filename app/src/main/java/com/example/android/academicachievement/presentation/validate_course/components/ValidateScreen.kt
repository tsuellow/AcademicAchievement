package com.example.android.academicachievement.presentation.validate_course.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.presentation.validate_course.ValidateCourseViewModel
import com.example.android.academicachievement.presentation.validate_course.ValidateDialogState

@Composable
fun ValidateScreen(
    navController: NavController,
    viewModel: ValidateCourseViewModel = hiltViewModel()
){

    var showDialog by remember {
        mutableStateOf(false)
    }

    Box(Modifier.fillMaxSize()) {

        ValidateCourseScreen(viewModel = viewModel, navController = navController) {
                path ->
            viewModel.setDialogState(path)
            (viewModel.validateDialogState.value as ValidateDialogState.Success).course?.getMilestoneByPath(path)
                ?.let {
                    Log.d("testo",
                        it.comment+" "+it.completed+" "+it.grades["Confidence"])
                }
            showDialog=true }

        if (showDialog){
            ValidateCourseDialog(
                validateDialogState = viewModel.validateDialogState.value,
                confirmDialogState = viewModel.confirmDialogState.value ,
                onSubmitButtonClick = {milestone -> viewModel.doOnConfirm(milestone,{showDialog=false})},
                onDismissRequest = {showDialog=false},
                loginData = viewModel.loginData)
        }


    }


}