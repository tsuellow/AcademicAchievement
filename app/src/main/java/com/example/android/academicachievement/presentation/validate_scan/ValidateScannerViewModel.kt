package com.example.android.academicachievement.presentation.validate_scan

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.android.academicachievement.presentation.enroll_scan.ScannerState
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ValidateScannerViewModel(): ViewModel() {
    private val _state = mutableStateOf<ScannerState>(ScannerState(title = "Validate Milestones", subTitle = "Scan ID or milestone code", pauseScan = false))
    val state: State<ScannerState> = _state


}