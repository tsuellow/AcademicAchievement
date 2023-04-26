package com.example.android.academicachievement.data.repository


import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.*
import com.example.android.academicachievement.data.response.FirebaseDatabaseConnection
import com.example.android.academicachievement.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow

class CourseRepositoryImpl  constructor(
    private val firebaseDatabaseConnection: FirebaseDatabaseConnection
):CourseRepository {
    override suspend fun getCourseTemplates(): Resource<HashMap<String, CourseTemplateDto>> {
        return firebaseDatabaseConnection.getCourseTemplates()
    }

    override suspend fun setCourseTemplate(courseId: String, courseTemplate: CourseTemplateDto):Boolean {
        return firebaseDatabaseConnection.setCourseTemplate(courseId,courseTemplate)
    }

    override suspend fun getSingleCourseTemplate(id:String):Resource<CourseTemplateDto>{
        return firebaseDatabaseConnection.getSingleCourseTemplate(id)
    }

    override suspend fun getCourse(studentId: String): Flow<Resource<CourseDto>> {
        return firebaseDatabaseConnection.getCourse(studentId)
    }

    override suspend fun getCurrentData(studentId:String):Resource<CurrentDataDto>{
        return firebaseDatabaseConnection.getCurrentData(studentId)
    }

    override suspend fun getStudent(studentId: String): Resource<StudentDto> {
        return firebaseDatabaseConnection.getStudent(studentId)
    }

    override suspend fun setStudent(studentId: String, student: StudentDto): Boolean {
        return firebaseDatabaseConnection.setStudent(studentId, student)
    }

    override suspend fun setCourse(studentId: String, course: CourseDto): Boolean {
        return firebaseDatabaseConnection.setCourse(studentId, course)
    }

    override suspend fun archiveCourse(studentId:String, course:CourseDto):Boolean{
        return firebaseDatabaseConnection.archiveCourse(studentId, course)
    }

    override suspend fun getTeachers(): Resource<HashMap<String, TeacherDto>> {
        return firebaseDatabaseConnection.getTeachers()
    }

    override suspend fun setTeachers(teachers: HashMap<String, TeacherDto>): Boolean {
        return firebaseDatabaseConnection.setTeachers(teachers)
    }
}