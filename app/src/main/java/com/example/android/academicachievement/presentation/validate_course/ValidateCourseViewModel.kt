package com.example.android.academicachievement.presentation.validate_course

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.CurrentDataDto
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.example.android.academicachievement.presentation.enroll_scan.DialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ValidateCourseViewModel @Inject constructor(
    private val repository: CourseRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _currentDataState:MutableState<CurrentDataState> = mutableStateOf(CurrentDataState.Loading)
    val currentDataState: State<CurrentDataState> = _currentDataState

    init {
        savedStateHandle.get<String>("courseId")?.let { courseId ->
            //getCourse(courseId)
        }
    }

    fun getCurrentStudentData(id:String){
        viewModelScope.launch {
            val response=repository.getCurrentData(id)
            when(response){
                is Resource.Success ->{
                    response.data?.let {
                        if (it.personalData==null){
                            _currentDataState.value= CurrentDataState.Failed(error = "Student is not registered / cannot be found")
                        }
                        _currentDataState.value=CurrentDataState.Success(it.personalData?.toStudent(),
                            it.currentCourse?.toCourse()
                        )
                    }
                }
                is Resource.Error ->{
                    _currentDataState.value= CurrentDataState.Failed(error = response.message)
                }
                else -> {
                    _currentDataState.value=CurrentDataState.Failed(error = "unexpected error")
                }
            }
        }
    }


}