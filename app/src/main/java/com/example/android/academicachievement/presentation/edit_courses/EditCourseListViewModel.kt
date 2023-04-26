package com.example.android.academicachievement.presentation.edit_courses

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.CourseTemplateDto
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.example.android.academicachievement.presentation.enroll_choose_course.CourseListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCourseListViewModel @Inject constructor(
    val repository: CourseRepository
):ViewModel() {

    private val _state = mutableStateOf(CourseListState())
    val state: State<CourseListState> = _state

    init {
        getCourseList()
    }

    private fun getCourseList(){
        viewModelScope.launch {
            _state.value= CourseListState(isLoading = true)
            when(val response=repository.getCourseTemplates()){
                is Resource.Success ->{
                    response.data?.let {
                        _state.value = CourseListState(courseList = it)
                    }
                }
                is Resource.Error ->{
                    _state.value = CourseListState(error = response.message?:"unexpected error")
                }
                else -> {
                    _state.value= CourseListState(isLoading = true)
                }
            }

        }
    }

    fun getNextCourse():String{
        var number=0
        if (!state.value.isLoading && state.value.error.isEmpty()){
            for (key in state.value.courseList.keys){
                val currNum=key.drop(1).toInt()
                if (currNum>=number){
                    number=currNum+1
                }
            }
        }
        return "C$number"
    }

    fun saveNewCourse(name:String){
        viewModelScope.launch {
            val success=repository.setCourseTemplate(getNextCourse(),CourseTemplateDto(name=name))
            if (success){
                getCourseList()
            }else{
                _state.value= CourseListState(error = "Could not add new course. Exit app, check connection and try again")
            }
        }
    }
}