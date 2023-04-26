package com.example.android.academicachievement.presentation.edit_single_course

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.CourseTemplateDto
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.example.android.academicachievement.presentation.enroll_scan.CourseState
import com.example.android.academicachievement.presentation.enroll_scan.ScannerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCourseViewModel @Inject constructor(
    val repository: CourseRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    var courseId: String="0"

    private val _courseTemplateState: MutableState<CourseTemplateState> = mutableStateOf<CourseTemplateState>(CourseTemplateState.Loading)
    val courseTemplateState : State<CourseTemplateState> = _courseTemplateState

    private val _saved:MutableState<Boolean> = mutableStateOf<Boolean>(false)
    val saved :State<Boolean> = _saved

    init {
        savedStateHandle.get<String>("courseId")?.let { courseId ->
            this.courseId=courseId
            getCourse(courseId)
        }
    }

    private fun getCourse(courseId:String){
        viewModelScope.launch {
            val response=repository.getSingleCourseTemplate(courseId)
            when(response){
                is Resource.Success ->{
                    response.data?.let {
                        _courseTemplateState.value=CourseTemplateState.Success(it)
                    }
                }
                is Resource.Error ->{
                    _courseTemplateState.value=CourseTemplateState.Failed(error =response.message?:"unexpected error" )
                }
                else -> {
                    _courseTemplateState.value=CourseTemplateState.Failed(error = "unexpected error" )
                }
            }
        }
    }

    fun saveChanges(courseTemplateDto: CourseTemplateDto){
        viewModelScope.launch {
            _saved.value=repository.setCourseTemplate(courseId = courseId, courseTemplate = courseTemplateDto)
        }
    }

    fun refreshState(){
        if(_courseTemplateState.value is CourseTemplateState.Success){
            _courseTemplateState.value=(courseTemplateState.value as CourseTemplateState.Success).copy()
        }
    }
}