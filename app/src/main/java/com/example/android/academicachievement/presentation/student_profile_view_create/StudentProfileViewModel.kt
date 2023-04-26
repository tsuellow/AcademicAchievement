package com.example.android.academicachievement.presentation.student_profile_view_create

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.data.remote.dto.StudentDto
import com.example.android.academicachievement.domain.model.Student
import com.example.android.academicachievement.domain.model.StudentMutableState
import com.example.android.academicachievement.domain.model.StudentState
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.example.android.academicachievement.domain.repository.ImageRepository
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    val repository: CourseRepository,
    val imageRepo: ImageRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var studentId: String = "0"

    init {
        savedStateHandle.get<String>("studentId")?.let { id ->
            this.studentId = id
            getStudent(id)
        }
    }

    private val _networkResponse: MutableState<Resource<StudentDto>> =
        mutableStateOf<Resource<StudentDto>>(Resource.Loading<StudentDto>(null))
    val networkResponse: State<Resource<StudentDto>> = _networkResponse

    private lateinit var studentMutableState: StudentMutableState
    lateinit var studentState: StudentState

    var alreadyExists by mutableStateOf(false)
        private set

    var isModifiable = mutableStateOf(false)
        private set

    var isProcessing = mutableStateOf(false)
        private set

    var snackBarState = mutableStateOf<SnackbarState>(SnackbarState.None)
        private set

    var photoFile = mutableStateOf<File?>(null)
        private set

    //validation state
    var errorFirstName = mutableStateOf<String>("")
        private set
    var errorLastName = mutableStateOf<String>("")
        private set
    var errorPhoto = mutableStateOf<String>("")
        private set
    var errorPhone = mutableStateOf<String>("")
        private set

    private fun performValidations(): Boolean {
        Log.d("valideichon", "entered")
        studentMutableState.firstName.value=studentMutableState.firstName.value.trim()
        studentMutableState.lastName.value=studentMutableState.lastName.value.trim()
        val student = studentMutableState.getStudent()
        errorFirstName.value = if (student.firstName.isEmpty())"field cannot be empty" else ""

        Log.d("valideichon", errorFirstName.value)
        errorLastName.value =if (student.lastName.isEmpty())"field cannot be empty" else ""
        errorPhoto.value =if (student.photoPath.isEmpty() && photoFile.value == null)"photo is missing" else ""

        if (student.phone.isEmpty()){
            errorPhone.value = "field cannot be empty"
        }else{
            try {
                val phoneUtil = PhoneNumberUtil.getInstance()
                val nicaPhone = phoneUtil.parse(student.phone, "NI")
                errorPhone.value = if (phoneUtil.isValidNumber(nicaPhone)) "" else "number is invalid1"
            }catch (e:Exception){
                e.printStackTrace()
                errorPhone.value = "number is invalid2"
            }
        }
        Log.d("valideichon", errorPhone.value)
        var pass:Boolean=errorFirstName.value.isEmpty() && errorLastName.value.isEmpty() && errorPhone.value.isEmpty() && errorPhoto.value.isEmpty()
        Log.d("valideichon", "pass:"+pass)
        return pass
    }

    fun getStudent(id: String) {
        viewModelScope.launch {
            val response = repository.getStudent(id)
            when (response) {
                is Resource.Success<StudentDto> -> {
                    val student = response.data
                    student?.let {
                        studentMutableState = StudentMutableState(student.toStudent())
                        isModifiable.value = false
                        alreadyExists = true
                    } ?: run {
                        val studentModel = Student(key = id)
                        studentMutableState = StudentMutableState(studentModel)
                        isModifiable.value = true
                        alreadyExists = false
                    }
                    Log.d("jerson3", studentMutableState.firstName.value)
                    studentState = StudentState(studentMutableState)
                }
            }
            _networkResponse.value = response
        }
    }

    public fun setStudent() {
        if (performValidations()){
            Log.d("valideichon", "entramos aqui1")
            isProcessing.value = true
            viewModelScope.launch {
                Log.d("valideichon", "entramos aqui")
                val backupSuccessful: Boolean
                val student = studentMutableState.getStudent()
                if (photoFile.value != null) {
                    Log.d("backup", "failed file" + photoFile.value!!.absolutePath)
                    val downloadPath = imageRepo.setStudentPhoto(studentId.drop(1), photoFile.value!!)
                    if (downloadPath.isNotEmpty()) {
                        student.photoPath = downloadPath
                        Log.d("backup", "failed here" + photoFile.value!!.absolutePath)
                        backupSuccessful = repository.setStudent(studentId, student.toStudentDto())
                    } else {

                        backupSuccessful = false
                    }
                } else {
                    backupSuccessful = repository.setStudent(studentId, student.toStudentDto())
                }
                isProcessing.value = false
                if (backupSuccessful) {
                    getStudent(studentId)
                    snackBarState.value = SnackbarState.Result(success = true, msg = "Data saved successfully")
                } else {
                    snackBarState.value = SnackbarState.Result(success = false, msg = "Backup failed! try again")
                }
                delay(3000)
                snackBarState.value = SnackbarState.None
            }
        }
    }

    fun onEvent(event: SpEvent) {
        when (event) {
            is SpEvent.MakeModifiable -> {
                isModifiable.value = event.make
            }
            is SpEvent.ChangeFirstName -> {
                studentMutableState.firstName.value = event.name
            }
            is SpEvent.ChangeLastName -> {
                studentMutableState.lastName.value = event.name
            }
            is SpEvent.ChangeDoB -> {
                studentMutableState.dob.value = event.date
            }
            is SpEvent.ChangeGender -> {
                studentMutableState.gender.value = event.gender
            }
            is SpEvent.ChangePhone -> {
                studentMutableState.phone.value = event.phone
            }
            is SpEvent.ChangeOccupation -> {
                studentMutableState.occupation.value = event.occupation
            }
            is SpEvent.ChangeCity -> {
                studentMutableState.city.value = event.city
            }
            is SpEvent.TakePhoto -> {
                photoFile.value = event.photoFile
            }

        }
    }
}


sealed class Gender(val value: String) {
    object MALE : Gender("M")
    object FEMALE : Gender("F")
}

sealed class Occupation(val value: String) {
    object STUDENT : Occupation("Student")
    object ENTREPRENEUR : Occupation("Entrepreneur")
    object EMPLOYEE : Occupation("Employee")
    object OTHER : Occupation("Other")
}

sealed class City(val value: String) {
    object ESTELI : City("ESTELI")
    object LEON : City("LEON")
    object CHINANDEGA : City("CHINANDEGA")
}

sealed class SnackbarState() {
    object None : SnackbarState()
    class Result(val success: Boolean = true, val msg: String = "worked") : SnackbarState()
}

sealed class SpEvent() {
    data class MakeModifiable(val make: Boolean) : SpEvent()
    data class ChangeFirstName(val name: String) : SpEvent()
    data class ChangeLastName(val name: String) : SpEvent()
    data class ChangePhone(val phone: String) : SpEvent()
    data class ChangeDoB(val date: String) : SpEvent()
    data class ChangeGender(val gender: String) : SpEvent()
    data class ChangeOccupation(val occupation: String) : SpEvent()
    data class ChangeCity(val city: String) : SpEvent()
    data class TakePhoto(val photoFile: File?) : SpEvent()
}