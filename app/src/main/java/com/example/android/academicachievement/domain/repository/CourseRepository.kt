package com.example.android.academicachievement.domain.repository

import androidx.lifecycle.MutableLiveData
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.*
import kotlinx.coroutines.flow.Flow

interface CourseRepository {

    suspend fun getCourseTemplates(): Resource<HashMap<String,CourseTemplateDto>>

    suspend fun setCourseTemplate(courseId:String, courseTemplate:CourseTemplateDto):Boolean

    suspend fun getSingleCourseTemplate(id:String):Resource<CourseTemplateDto>

    suspend fun getCourse(studentId:String): Flow<Resource<CourseDto>>

    suspend fun getCurrentData(studentId:String):Resource<CurrentDataDto>

    suspend fun getStudent(studentId:String): Resource<StudentDto>

    suspend fun setStudent(studentId:String, student:StudentDto):Boolean

    suspend fun setCourse(studentId:String, course:CourseDto):Boolean

    suspend fun archiveCourse(studentId:String, course:CourseDto):Boolean

    suspend fun getTeachers():Resource<HashMap<String,TeacherDto>>

    suspend fun setTeachers(teachers:HashMap<String,TeacherDto>):Boolean
}