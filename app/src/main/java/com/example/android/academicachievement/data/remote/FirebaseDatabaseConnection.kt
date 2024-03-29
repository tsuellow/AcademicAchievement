package com.example.android.academicachievement.data.response

import android.util.Log
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDatabaseConnection(val url:String) {

    private val firebaseRtdb= Firebase.database(url)

    private val templatesRef=firebaseRtdb.getReference("courses")
    private val studentsRef=firebaseRtdb.getReference("students")
    private val teachersRef=firebaseRtdb.getReference("teachers")

    suspend fun getCourseTemplates():Resource<HashMap<String,CourseTemplateDto>>{
        return when(val response=templatesRef.getEvent()){
            is Resource.Success -> {
                val result= HashMap<String,CourseTemplateDto>(response.data!!.children.associate { it.key!! to it.getValue(CourseTemplateDto::class.java)!! })
                Resource.Success(result)
            }
            is Resource.Error -> {
                Resource.Error(message = response.message)
            }
            else -> {
                Resource.Error(message = "unexpected error")
            }
        }

    }

    suspend fun getSingleCourseTemplate(id:String):Resource<CourseTemplateDto>{
        return when(val response=templatesRef.child(id).getEvent()){
            is Resource.Success -> {
                val result= response.data!!.getValue(CourseTemplateDto::class.java)
                Resource.Success(result)
            }
            is Resource.Error -> {
                Resource.Error(message = response.message)
            }
            else -> {
                Resource.Error(message = "unexpected error")
            }
        }

    }

    suspend fun getCurrentData(studentId:String):Resource<CurrentDataDto>{
        return when(val response=studentsRef.child(studentId).child("current").getEvent()){
            is Resource.Success -> {
                Log.d("testinofire", response.data.toString())
                try {
                    val result= response.data!!.getValue(CurrentDataDto::class.java)
                    Resource.Success(result)
                }catch (e:Exception){
                    Log.d("testinofire", "this failed")
                    Resource.Error(message = "fail")
                }

            }
            is Resource.Error -> {
                Resource.Error(message = response.message)
            }
            else -> {
                Resource.Error(message = "unexpected error")
            }
        }
    }

    suspend fun getStudent(studentId:String):Resource<StudentDto>{
        return when(val response=studentsRef.child(studentId).child("current").child("personalData").getEvent()){
            is Resource.Success -> {
                val result= response.data?.getValue(StudentDto::class.java)
                Log.d("jerson2",result.toString())
                Resource.Success(result)
            }
            is Resource.Error -> {
                Resource.Error(message = response.message)
            }
            else -> {
                Resource.Error(message = "unexpected error")
            }
        }
    }

    suspend fun setStudent(studentId:String, student:StudentDto):Boolean{
        val ref=studentsRef.child(studentId).child("current").child("personalData")
        //ref.setValue(course).addOnSuccessListener {  }
        return ref.setValueEvent(student)
    }

    suspend fun setCourse(studentId:String, course:CourseDto):Boolean{
        val ref=studentsRef.child(studentId).child("current").child("currentCourse")
        //ref.setValue(course).addOnSuccessListener {  }
        return ref.setValueEvent(course)
    }

    suspend fun setCourseTemplate(courseId:String, course:CourseTemplateDto):Boolean{
        val ref=templatesRef.child(courseId)
        //ref.setValue(course).addOnSuccessListener {  }
        return ref.setValueEvent(course)
    }

    suspend fun archiveCourse(studentId:String, course:CourseDto):Boolean{
        val ref=studentsRef.child(studentId).child("archive").child(course.key)
        //ref.setValue(course).addOnSuccessListener {  }
        return ref.setValueEvent(course)
    }


    @ExperimentalCoroutinesApi
    suspend fun getCourse(studentId:String): Flow<Resource<CourseDto>> = flow{
        val ref=studentsRef.child(studentId).child("current").child("currentCourse")
        emit(Resource.Loading<CourseDto>(null))
        val flow=ref.valueEventFlow()
        flow.collect { res ->
            when(res){
                is Resource.Success -> {
                    val result= res.data?.getValue(CourseDto::class.java)
                    emit(Resource.Success(result))
                }
                is Resource.Error -> {
                    emit(Resource.Error<CourseDto>(message = res.message))
                }
                else -> {
                    emit(Resource.Error<CourseDto>(message = "unexpected error"))
                }
            }
        }
    }

    suspend fun getTeachers():Resource<HashMap<String,TeacherDto>>{
        Log.d("teacher","firebase entered")
        return when(val response=teachersRef.getEvent()){
            is Resource.Success -> {
                Log.d("teacher",response.data.toString())
                val result= HashMap<String,TeacherDto>(response.data!!.children.associate { it.key!! to it.getValue(TeacherDto::class.java)!! })
                Log.d("teacher",result.toString())
                Resource.Success(result)
            }
            is Resource.Error -> {
                Resource.Error(message = response.message)
            }
            else -> {
                Resource.Error(message = "unexpected error")
            }
        }
    }

    suspend fun setTeachers(teachers: HashMap<String, TeacherDto>): Boolean {
        return teachersRef.setValueEvent(teachers)
    }


    //this turns normal get().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener) into a suspend function that returns resource containing the Snapshot
    suspend fun DatabaseReference.getEvent(): Resource<DataSnapshot> = suspendCoroutine { continuation ->
        val onSuccessListener =
            OnSuccessListener<DataSnapshot> { snapshot -> continuation.resume(Resource.Success(snapshot)) }
        val onFailureListener = OnFailureListener {continuation.resume(Resource.Error(message = it.message))}
        get().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener) // Subscribe to the event
    }

    suspend fun DatabaseReference.setValueEvent(value:Any): Boolean = suspendCoroutine { continuation ->
        val onSuccessListener =
            OnSuccessListener<Any>{ continuation.resume(true) }
        val onFailureListener = OnFailureListener {continuation.resume(false)}
        setValue(value).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener) // Subscribe to the event
    }

    //this turns valueEventListener into a suspend function
    @ExperimentalCoroutinesApi
    suspend fun DatabaseReference.valueEventFlow(): Flow<Resource<DataSnapshot>> = callbackFlow {
        val valueEventListener:ValueEventListener=object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                trySendBlocking(Resource.Success(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                trySendBlocking(Resource.Error(message = error.message))
            }
        }
        addValueEventListener(valueEventListener)
        awaitClose{
            removeEventListener(valueEventListener)
        }
    }



}



