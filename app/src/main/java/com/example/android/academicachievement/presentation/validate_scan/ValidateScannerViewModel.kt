package com.example.android.academicachievement.presentation.validate_scan

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.PreferenceManager
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.domain.model.Milestone
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.example.android.academicachievement.presentation.enroll_scan.ConfirmDialogState
import com.example.android.academicachievement.presentation.enroll_scan.DialogState
import com.example.android.academicachievement.presentation.enroll_scan.ScannerState
import com.example.android.academicachievement.presentation.validate_course.CurrentCourseState
import com.example.android.academicachievement.presentation.validate_course.StudentDataState
import com.example.android.academicachievement.presentation.validate_course.ValidateDialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ValidateScannerViewModel @Inject constructor(
    val repository: CourseRepository,
    private val preferences:PreferenceManager
): ViewModel() {
    private val _state = mutableStateOf<ScannerState>(ScannerState(title = "Validate Milestones",
        subTitle = "Scan ID or milestone code",
        pauseScan = false))
    val state: State<ScannerState> = _state

    lateinit var loginData:PreferenceManager.LoginData

    init {
        viewModelScope.launch {
            loginData=preferences.getLoginData()
        }
    }

    fun pauseScanner(yes:Boolean){
        _state.value.pauseScan=yes
    }

    //Dialog State

    private val _validateDialogState: MutableState<ValidateDialogState> = mutableStateOf<ValidateDialogState>(
        ValidateDialogState.Loading)
    val validateDialogState :State<ValidateDialogState> = _validateDialogState

    private val _confirmDialogState: MutableState<ConfirmDialogState> = mutableStateOf<ConfirmDialogState>(
        ConfirmDialogState.Unpressed)
    val confirmDialogState :State<ConfirmDialogState> = _confirmDialogState


    fun setDialogState(path:String, id:String){
        _validateDialogState.value=ValidateDialogState.Loading
        viewModelScope.launch {
            when(val response=repository.getCurrentData(id)){
                is Resource.Success ->{
                    response.data?.let {
                        if (it.currentCourse?.toCourse()?.getMilestoneByPath(path) !=null){
                            _validateDialogState.value=
                                ValidateDialogState.Success(personalData = it.personalData?.toStudent(), course = it.currentCourse.toCourse(), path)
                        }else{
                            _validateDialogState.value= ValidateDialogState.Failed(error = "Milestone not found. It seems this student is not enrolled in this course anymore")
                        }
                    }?: run{
                        _validateDialogState.value= ValidateDialogState.Failed(error = "milestone not found")
                    }
                }
                is Resource.Error ->{
                    _validateDialogState.value= ValidateDialogState.Failed(error = response.message)
                }
                else -> {
                    _validateDialogState.value= ValidateDialogState.Failed(error = "unexpected error")
                }
            }
        }


    }

    fun doOnConfirm(milestone: Milestone, doDismiss:()->Unit){
        viewModelScope.launch {
            var done: Boolean
            _confirmDialogState.value= ConfirmDialogState.Loading
            with(validateDialogState.value as ValidateDialogState.Success){
                val updatedCourse=course!!.copy()
                updatedCourse.setMilestoneByPath(path, milestone)
                done=repository.setCourse(personalData!!.key,updatedCourse.toCourseDto())//set new course
            }

            if (done){
                _confirmDialogState.value= ConfirmDialogState.Success
            }else{
                _confirmDialogState.value= ConfirmDialogState.Failed
            }
            delay(1000)
            doDismiss.invoke()
            _confirmDialogState.value= ConfirmDialogState.Unpressed
        }

    }
}