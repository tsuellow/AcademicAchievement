package com.example.android.academicachievement.presentation.enroll_choose_course

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrollChooseCourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository
): ViewModel() {

    private val _state = mutableStateOf(CourseListState())
    val state: State<CourseListState> = _state

    init {
        getCourseList()

    }

    private fun getCourseList(){
        viewModelScope.launch {
            _state.value=CourseListState(isLoading = true)
            when(val response=courseRepository.getCourseTemplates()){
                is Resource.Success ->{
                    response.data?.let {
                        _state.value = CourseListState(courseList = it)
                    }
                }
                is Resource.Error ->{
                    _state.value = CourseListState(error = response.message?:"unexpected error")
                }
                else -> {
                    _state.value=CourseListState(isLoading = true)
                }
            }

        }
    }

}


