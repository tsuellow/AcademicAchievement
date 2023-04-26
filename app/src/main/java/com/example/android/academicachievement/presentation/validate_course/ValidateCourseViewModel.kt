package com.example.android.academicachievement.presentation.validate_course

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.PreferenceManager
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.domain.model.Milestone
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.example.android.academicachievement.presentation.enroll_scan.ConfirmDialogState
import com.example.android.academicachievement.presentation.enroll_scan.DialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ValidateCourseViewModel @Inject constructor(
    private val repository: CourseRepository,
    private val savedStateHandle: SavedStateHandle,
    private val preferences:PreferenceManager
): ViewModel() {

    private val _studentDataState:MutableState<StudentDataState> = mutableStateOf(StudentDataState.Loading)
    val studentDataState: State<StudentDataState> = _studentDataState

    private val _currentCourseState:MutableState<CurrentCourseState> = mutableStateOf(CurrentCourseState.Loading)
    val currentCourseState:State<CurrentCourseState> = _currentCourseState

    private var _studentId= mutableStateOf(0)
    val studentId: State<Int> = _studentId

    lateinit var loginData:PreferenceManager.LoginData

    init {
        savedStateHandle.get<String>("studentId")?.let { id ->
            _studentId.value=id.drop(1).toInt()
            viewModelScope.launch {
                launch{
                    loginData=preferences.getLoginData()
                    getStudentData(id)
                }
                getCourseFlow(id)
            }
        }
    }

    private suspend fun getStudentData(id:String){
        val response=repository.getStudent(id)
        when(response){
            is Resource.Success ->{
                response.data?.let {
                    _studentDataState.value= StudentDataState.Success(personalData = it.toStudent())
                }?:run{_studentDataState.value= StudentDataState.Inexistent}
            }
            is Resource.Error ->{
                _studentDataState.value= StudentDataState.Failed(error = response.message)
            }
            else -> {
                _studentDataState.value=StudentDataState.Failed(error = "unexpected error")
            }
        }
    }


    private suspend fun getCourseFlow(id:String){
        repository.getCourse(id).collect { result ->
            when(result){
                is Resource.Loading ->{
                    _currentCourseState.value=CurrentCourseState.Loading
                }
                is Resource.Success ->{
                    result.data?.let {
                        _currentCourseState.value=CurrentCourseState.Success(result.data.toCourse())
                    }?: run{
                        _currentCourseState.value=CurrentCourseState.NoCourse
                    }

                }
                is Resource.Error ->{
                    _currentCourseState.value=CurrentCourseState.Failed(error = result.message)
                }
            }
        }
    }

    //Dialog State

    private val _validateDialogState:MutableState<ValidateDialogState> = mutableStateOf<ValidateDialogState>(ValidateDialogState.Loading)
    val validateDialogState :State<ValidateDialogState> = _validateDialogState

    private val _confirmDialogState:MutableState<ConfirmDialogState> = mutableStateOf<ConfirmDialogState>(ConfirmDialogState.Unpressed)
    val confirmDialogState :State<ConfirmDialogState> = _confirmDialogState


    fun setDialogState(path:String){
        if (studentDataState.value is StudentDataState.Success && currentCourseState.value is CurrentCourseState.Success ){
            if ((currentCourseState.value as CurrentCourseState.Success).course?.getMilestoneByPath(path = path) !=null){
                _validateDialogState.value=ValidateDialogState.Success(personalData = (studentDataState.value as StudentDataState.Success).personalData!!, course = (currentCourseState.value as CurrentCourseState.Success).course!!.copy(), path)
            }else{
                _validateDialogState.value=ValidateDialogState.Failed(error = "milestone not found")
            }
        }else{
            _validateDialogState.value=ValidateDialogState.Failed(error = "failed to retrieve data, this is weird")
        }
    }

    fun doOnConfirm(milestone: Milestone, doDismiss:()->Unit){
        viewModelScope.launch {
            var done: Boolean
            _confirmDialogState.value=ConfirmDialogState.Loading
            with(validateDialogState.value as ValidateDialogState.Success){
                val updatedCourse=course!!.copy()
                updatedCourse.setMilestoneByPath(path, milestone.copy(validatedBy = loginData.fullName))
                done=repository.setCourse(personalData!!.key,updatedCourse.toCourseDto())//set new course
            }

            if (done){
                _confirmDialogState.value=ConfirmDialogState.Success
            }else{
                _confirmDialogState.value=ConfirmDialogState.Failed
            }
            delay(1000)
            doDismiss.invoke()
            _confirmDialogState.value=ConfirmDialogState.Unpressed
        }

    }


}