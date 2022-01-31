package com.example.android.academicachievement.presentation.enroll_scan


sealed class ConfirmDialogState(){
    object Unpressed:ConfirmDialogState()
    object Loading:ConfirmDialogState()
    object Success:ConfirmDialogState()
    object Failed:ConfirmDialogState()
}
