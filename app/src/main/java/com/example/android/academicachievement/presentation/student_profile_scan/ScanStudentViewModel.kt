package com.example.android.academicachievement.presentation.student_profile_scan

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.example.android.academicachievement.presentation.enroll_scan.ScannerState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel


@HiltViewModel
class ScanStudentViewModel @Inject constructor(): ViewModel() {
    private val _state = mutableStateOf<ScannerState>(
        ScannerState(
            title = "Add/View Student Profile",
            subTitle = "Scan student badge",
            pauseScan = false
        )
    )
    val state: State<ScannerState> = _state

    fun pauseScanner(yes: Boolean) {
        _state.value.pauseScan = yes
    }
}