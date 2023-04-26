package com.example.android.academicachievement.presentation.add_teacher

import android.util.Log
import androidx.compose.runtime.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.TeacherDto
import com.example.android.academicachievement.domain.model.Teacher
import com.example.android.academicachievement.domain.repository.CourseRepository
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel
class AddTeacherViewModel @Inject constructor(
    val repository:CourseRepository
): ViewModel() {

    private val _networkResponse: MutableState<Resource<HashMap<String, TeacherDto>>> = mutableStateOf(Resource.Loading(null))
    val networkResponse : State<Resource<HashMap<String, TeacherDto>>> = _networkResponse

    var teacherMap = mutableStateMapOf<String, Teacher>()
        private set

    init {
        getTeachers()
    }

    private fun getTeachers(){
        Log.d("teacher","at least it entered" )
        _networkResponse.value = Resource.Loading(null)
        viewModelScope.launch {
            Log.d("teacher","at least it entered1" )
            val response = repository.getTeachers()
            when (response) {
                is Resource.Success<HashMap<String,TeacherDto>> -> {
                    val teachers = response.data
                    if (teachers != null) {
                        Log.d("teacher","got til here")
                        teacherMap= teachers.map{Pair(it.key,it.value.toTeacher())}.toMutableStateMap()
                        Log.d("teacher",teacherMap.get("admin").toString())
                    }
                }
            }
            _networkResponse.value = response
            Log.d("teacher","finished" )
        }
    }

    fun setTeacher(){
        viewModelScope.launch {
            repository.setTeachers(HashMap(teacherMap.mapValues { it.value.toTeacherDto()}))
        }
    }

    fun onEvent(event:TeacherEvent){
        when(event){
            is TeacherEvent.AddTeacher -> {
                teacherMap.put(event.teacher.login,event.teacher)
                setTeacher()
                getTeachers()
            }
            is TeacherEvent.DeleteTeacher -> {
                teacherMap.remove(event.login)
                setTeacher()
                getTeachers()
            }
        }
    }
}

sealed class TeacherEvent() {
    data class AddTeacher(val teacher: Teacher) : TeacherEvent()
    data class DeleteTeacher(val login: String) : TeacherEvent()
}