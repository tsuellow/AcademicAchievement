package com.example.android.academicachievement.presentation.enroll_scan

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.PreferenceManager
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.CurrentDataDto
import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.example.android.academicachievement.util.toDateString
import com.example.android.academicachievement.util.toDateTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EnrollScanViewModel @Inject constructor(
    private val repository: CourseRepository,
    private val savedStateHandle: SavedStateHandle,
    private val preferences: PreferenceManager
):ViewModel() {

    private val _courseState = mutableStateOf(CourseState())
    val courseState: State<CourseState> = _courseState

    private val _scannerState = mutableStateOf(ScannerState(title = "Enroll Students", pauseScan = true))
    val scannerState: State<ScannerState> = _scannerState

    private val _dialogState = mutableStateOf(DialogState(isLoading = true))
    val dialogState: State<DialogState> = _dialogState

    private val _confirmDialogState:MutableState<ConfirmDialogState> = mutableStateOf(ConfirmDialogState.Unpressed)
    val confirmDialogState: State<ConfirmDialogState> = _confirmDialogState

    lateinit var loginData:PreferenceManager.LoginData

    init {
        savedStateHandle.get<String>("courseId")?.let { courseId ->
            getCourse(courseId)
        }
    }

    fun isAuthorized(pin:String):Boolean{
        return pin.contentEquals(loginData.pin)
    }

    private fun getCourse(courseId:String){
        viewModelScope.launch {
            //first get login data from prefs
            loginData=preferences.getLoginData()
            //then make db request
            when(val response=repository.getSingleCourseTemplate(courseId)){
                is Resource.Success ->{
                    response.data?.let {
                        _courseState.value = CourseState(course = it.toCourseDto(courseId).toCourse().copy(enrolledBy = loginData.fullName, dateEnrolled = Date().toDateTimeString()))
                        _scannerState.value = _scannerState.value.copy(subTitle=courseState.value.course.key+". "+courseState.value.course.name, pauseScan = false)
                    }
                }
                is Resource.Error ->{
                    _courseState.value = CourseState(error = response.message?:"unexpected error")
                    _scannerState.value = ScannerState(subTitle=response.message?:"unexpected error", pauseScan = true)
                }
                else -> {
                    _courseState.value= CourseState(isLoading = true)
                    _scannerState.value = ScannerState(subTitle="unexpected error", pauseScan = true)
                }
            }
        }
    }

    fun pauseScanner(yes:Boolean){
        _scannerState.value.pauseScan=yes
    }

    fun getCurrentStudentData(id:String){
        _dialogState.value=DialogState(isLoading = true)
        viewModelScope.launch {
            when(val response=repository.getCurrentData(id)){
                is Resource.Success ->{
                    Log.d("testinofull", response.data.toString() )
                    response.data?.let {
                        _dialogState.value=processNetworkResponse(it)
                    }?:run {
                        _dialogState.value= DialogState(inexistent = true )
                    }
                }
                is Resource.Error ->{
                    _dialogState.value= DialogState(error = response.message)
                }
                else -> {
                    _dialogState.value=DialogState(error = "unexpected error")
                }
            }
        }
    }

    private fun processNetworkResponse(currentDataDto: CurrentDataDto):DialogState{
        Log.d("testinofull", currentDataDto.toString() )
        currentDataDto?.let { Log.d("testinofull", it.toString() )}
        val newDialogState=DialogState(isLoading = false)
        if (currentDataDto.personalData==null){
            newDialogState.error="Student ID does not exist / has not been registered jet"//TODO add direct link to create student
        }else{
            newDialogState.personalData=currentDataDto.personalData.toStudent()
            if(currentDataDto.currentCourse==null){
                newDialogState.isApproved=true
                newDialogState.observations.add("This is this students first course")
            }else{
                newDialogState.currentCourse=currentDataDto.currentCourse.toCourse()
                if (currentDataDto.currentCourse.toCourse().completed){
                    if(isNextCourse(currentDataDto.currentCourse.toCourse())){
                        newDialogState.isApproved=true
                    }else{
                        newDialogState.observations.add("The student's past course is ${currentDataDto.currentCourse.key} so he cannot be enrolled in ${courseState.value.course.key}")
                    }
                }else{
                    if(!isNextCourse(currentDataDto.currentCourse.toCourse())){
                        newDialogState.observations.add("The student's past course is ${currentDataDto.currentCourse.key}. so he cannot be enrolled in ${courseState.value.course.key}.")
                    }
                    newDialogState.observations.add("The student has not yet completed his past course")
                }
            }

        }
        return newDialogState
    }

    private fun isNextCourse(course: Course):Boolean{
        return try{
            courseState.value.course.key.drop(1).toInt() == course.key.drop(1).toInt()+1
        }catch (e:Exception){
            false
        }
    }

    fun doOnConfirm(doDismiss:()->Unit){
        viewModelScope.launch {
            _confirmDialogState.value=ConfirmDialogState.Loading
            var archive=true
            var reset=false
            if (dialogState.value.currentCourse!=null){
                archive=false
                with(dialogState.value){
                    archive=repository.archiveCourse(personalData!!.key,currentCourse!!.toCourseDto())//archive old course
                }
            }
            if (archive){
                with(dialogState.value){
                    reset=repository.setCourse(personalData!!.key,courseState.value.course.toCourseDto())//set new course if archive was successful
                }
            }
            if (archive && reset){
                _confirmDialogState.value=ConfirmDialogState.Success
            }else{
                _confirmDialogState.value=ConfirmDialogState.Failed
            }
            delay(1500)
            resetConfirmState()
            doDismiss.invoke()
            pauseScanner(false)
        }
    }

    fun resetConfirmState(){
        _confirmDialogState.value=ConfirmDialogState.Unpressed
    }


}