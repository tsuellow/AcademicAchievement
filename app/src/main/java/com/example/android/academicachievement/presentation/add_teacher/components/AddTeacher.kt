package com.example.android.academicachievement.presentation.add_teacher.components

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SendToMobile
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.academicachievement.presentation.common_composables.OutlinedTextFieldValidation
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.domain.model.Teacher
import com.example.android.academicachievement.presentation.add_teacher.AddTeacherViewModel
import com.example.android.academicachievement.presentation.add_teacher.TeacherEvent
import com.example.android.academicachievement.presentation.common_composables.MyTopBar
import com.example.android.academicachievement.presentation.ui.theme.MyLightLightGray
import com.example.android.academicachievement.presentation.ui.theme.MyRed
import com.example.android.academicachievement.util.PhoneNumberVisualTransformation
import com.example.android.academicachievement.util.sendWhatsAppMsg
import com.google.i18n.phonenumbers.PhoneNumberUtil


@Composable
fun AddTeacher(viewModel: AddTeacherViewModel = hiltViewModel()) {


    Column(modifier = Modifier.fillMaxSize()) {

        MyTopBar(title = "Teacher List")

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LoadingWrapper(viewModel = viewModel)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MyLightLightGray),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NewTeacherEntry(
                enabled = viewModel.networkResponse.value is Resource.Success,
                onTeacherAdded = { viewModel.onEvent(TeacherEvent.AddTeacher(teacher = it)) })
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NewTeacherEntry(enabled: Boolean, onTeacherAdded: (Teacher) -> Unit) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Text(
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        text = "Add new teacher",
        style = MaterialTheme.typography.h6,
        color = MaterialTheme.colors.primary
    )

    var login by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    OutlinedTextFieldValidation(
        value = login,
        onValueChange = { typed -> login = typed.filter { it.isLetter() } },
        label = { Text(text = "User login") },
        error = loginError
    )

    var fullName by remember { mutableStateOf("") }
    var fullNameError by remember { mutableStateOf("") }
    OutlinedTextFieldValidation(
        value = fullName,
        onValueChange = { fullName = it },
        label = { Text(text = "Full name") },
        error = fullNameError
    )

    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    OutlinedTextFieldValidation(
        value = phone,
        onValueChange = { phone = it.filter { it.isDigit() } },
        label = { Text(text = "Phone") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
        error = phoneError,
        visualTransformation = PhoneNumberVisualTransformation("NI")
    )
    Spacer(modifier = Modifier.size(16.dp))
    Button(
        onClick = {
            fun validate(): Boolean {
                loginError = if (login.isEmpty()) "field cannot be empty" else ""
                fullNameError = if (fullName.isEmpty()) "field cannot be empty" else ""
                phoneError = if (phone.isEmpty()) {
                    "field cannot be empty"
                } else {
                    try {
                        val phoneUtil = PhoneNumberUtil.getInstance()
                        val nicaPhone = phoneUtil.parse(phone, "NI")
                        if (phoneUtil.isValidNumber(nicaPhone)) "" else "number is invalid"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        "number is invalid"
                    }
                }
                return loginError.isEmpty() && fullNameError.isEmpty() && phoneError.isEmpty()
            }
            if (validate()) {
                onTeacherAdded(Teacher(login.trim(), fullName.trim(), phone))
                login = ""
                fullName = ""
                phone = ""
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        },
        enabled = enabled
    ) {
        Text(text = "Save")
    }

    Spacer(modifier = Modifier.size(16.dp))


}


@Composable
private fun LoadingWrapper(viewModel: AddTeacherViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = ScrollState(0)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val res = viewModel.networkResponse.value) {
                is Resource.Loading -> {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator()
                }
                is Resource.Error -> {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Error!", style = MaterialTheme.typography.h6, color = MyRed)
                    Text(
                        text = res.message ?: "Unknown failure",
                        style = MaterialTheme.typography.body1,
                        color = MyRed
                    )
                }
                is Resource.Success -> {
                    TeacherList(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TeacherList(viewModel: AddTeacherViewModel) {
    val context = LocalContext.current
    LazyColumn(
        Modifier.heightIn(min=200.dp, max=800.dp)
    ) {
        items(viewModel.teacherMap.map { it.value }.sortedBy { it.login }) { teacher ->
            TeacherRow(
                teacher = teacher,
                onSendQr = { sendLoginData(teacher, context) },
                onTeacherDeleted = { viewModel.onEvent(TeacherEvent.DeleteTeacher(teacher.login)) })
            Divider(Modifier.padding(horizontal = 8.dp))
        }
    }
}

fun sendLoginData(teacher: Teacher, context: Context) {
    val text = "Login data for:\n" +
            "_${teacher.fullName}_\n\n" +
            "login: *${teacher.login}*\n" +
            "pin: *${teacher.pin}*"

    sendWhatsAppMsg(teacher.phone, text, context)
}

@Composable
fun TeacherRow(
    teacher: Teacher,
    onSendQr: () -> Unit,
    onTeacherDeleted: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = teacher.fullName, overflow = TextOverflow.Ellipsis)

        Row {
            IconButton(
                onClick = {
                    onSendQr()
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.SendToMobile,
                    contentDescription = "send",
                    tint = MaterialTheme.colors.secondary
                )
            }

            IconButton(
                onClick = {
                    onTeacherDeleted(teacher.login)
                },
                enabled = !teacher.login.contentEquals("admin")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "delete",
                    tint = MaterialTheme.colors.secondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

    }
}

