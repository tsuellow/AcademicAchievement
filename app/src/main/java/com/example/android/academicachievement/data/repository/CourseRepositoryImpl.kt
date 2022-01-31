package com.example.android.academicachievement.data.repository


import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.CourseDto
import com.example.android.academicachievement.data.remote.dto.CourseTemplateDto
import com.example.android.academicachievement.data.remote.dto.CurrentDataDto
import com.example.android.academicachievement.data.remote.dto.StudentDto
import com.example.android.academicachievement.data.response.FirebaseConnection
import com.example.android.academicachievement.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CourseRepositoryImpl  constructor(
    private val firebaseConnection: FirebaseConnection
):CourseRepository {
    override suspend fun getCourseTemplates(): Resource<HashMap<String, CourseTemplateDto>> {
        return firebaseConnection.getCourseTemplates()
    }

    override suspend fun getSingleCourseTemplate(id:String):Resource<CourseTemplateDto>{
        return firebaseConnection.getSingleCourseTemplate(id)
    }

    override suspend fun getCourse(studentId: String): Flow<Resource<CourseDto>> {
        return firebaseConnection.getCourse(studentId)
    }

    override suspend fun getCurrentData(studentId:String):Resource<CurrentDataDto>{
        return firebaseConnection.getCurrentData(studentId)
    }

    override suspend fun getStudent(studentId: String): Resource<StudentDto> {
        return firebaseConnection.getStudent(studentId)
    }

    override suspend fun setCourse(studentId: String, course: CourseDto): Boolean {
        return firebaseConnection.setCourse(studentId, course)
    }

    override suspend fun archiveCourse(studentId:String, course:CourseDto):Boolean{
        return firebaseConnection.archiveCourse(studentId, course)
    }
}