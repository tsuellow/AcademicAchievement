package com.example.android.academicachievement.presentation.student_profile_view_create.components

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.airbnb.lottie.compose.*
import com.example.android.academicachievement.R
import com.example.android.academicachievement.presentation.common_composables.DefaultSnackbar
import com.example.android.academicachievement.presentation.common_composables.OutlinedTextFieldValidation
import com.example.android.academicachievement.common.Resource
import com.example.android.academicachievement.presentation.common_composables.RoundedCameraView
import com.example.android.academicachievement.domain.model.Student
import com.example.android.academicachievement.domain.model.StudentMutableState
import com.example.android.academicachievement.domain.model.StudentState
import com.example.android.academicachievement.presentation.common_composables.DropdownTextField
import com.example.android.academicachievement.presentation.common_composables.MyTopBar
import com.example.android.academicachievement.presentation.student_profile_view_create.*
import com.example.android.academicachievement.presentation.ui.theme.MyRed
import com.example.android.academicachievement.util.*

import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StudentProfile(
    viewModel: StudentProfileViewModel = hiltViewModel()
) {

    val scaffoldState: ScaffoldState = rememberScaffoldState()

    val context = LocalContext.current

    val keyboardController = LocalSoftwareKeyboardController.current



    if (viewModel.snackBarState.value !is SnackbarState.None) {
        val snackState = viewModel.snackBarState.value as SnackbarState.Result
        LaunchedEffect(scaffoldState.snackbarHostState) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = snackState.msg,
                actionLabel = "hide"
            )
        }
    }
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { scaffoldState.snackbarHostState },
        topBar = { MyTopBar(title = "Student Profile ID: " + viewModel.studentId) },
        floatingActionButton = {
            if (viewModel.networkResponse.value is Resource.Success)
                FloatingButtons(
                    isModifiable = viewModel.isModifiable.value,
                    isProcessing = viewModel.isProcessing.value,
                    onEdit = { viewModel.isModifiable.value = true },
                    onSave = { keyboardController?.hide()
                        viewModel.setStudent() },
                    onQr = { displayQrDialog(context, viewModel.studentState).show() }
                )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            LoadingWrapper(viewModel = viewModel)

            DefaultSnackbar(
                snackbarHostState = scaffoldState.snackbarHostState,
                onDismiss = { scaffoldState.snackbarHostState.currentSnackbarData?.dismiss() },
                modifier = Modifier.align(Alignment.TopCenter)
            )

        }

    }
}

@Composable
fun FloatingButtons(
    isModifiable: Boolean,
    isProcessing: Boolean = false,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onQr: () -> Unit
) {
    Column() {
        if (isModifiable) {
            FloatingActionButton(
                modifier = Modifier.padding(8.dp),
                onClick = onSave,
                backgroundColor = (if (isProcessing) Color.LightGray else MaterialTheme.colors.secondary)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator()
                } else {
                    Icon(imageVector = Icons.Filled.Save, contentDescription = "save")
                }
            }
        } else {
            FloatingActionButton(modifier = Modifier.padding(8.dp), onClick = onQr) {
                Icon(imageVector = Icons.Filled.QrCode2, contentDescription = "qr code")
            }
            FloatingActionButton(modifier = Modifier.padding(8.dp), onClick = onEdit) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "edit")
            }
        }

    }

}

@Composable
private fun LoadingWrapper(viewModel: StudentProfileViewModel) {
    val focusRequester = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusRequester.clearFocus()
                })
            }
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
                    StudentProfileShow(
                        viewModel.studentState,
                        isModifiable = viewModel.isModifiable.value,
                        onFirstNameChange = { viewModel.onEvent(SpEvent.ChangeFirstName(it)) },
                        firstNameError = viewModel.errorFirstName.value,
                        onLastNameChange = { viewModel.onEvent(SpEvent.ChangeLastName(it)) },
                        lastNameError = viewModel.errorLastName.value,
                        onPhoneChange = { viewModel.onEvent(SpEvent.ChangePhone(it)) },
                        phoneError = viewModel.errorPhone.value,
                        onGenderChange = { viewModel.onEvent(SpEvent.ChangeGender(it)) },
                        onOccupationChange = { viewModel.onEvent(SpEvent.ChangeOccupation(it)) },
                        onCityChange = { viewModel.onEvent(SpEvent.ChangeCity(it)) },
                        onDoBChange = { viewModel.onEvent(SpEvent.ChangeDoB(it)) },
                        onPhotoTake = { viewModel.onEvent(SpEvent.TakePhoto(it)) },
                        photoError = viewModel.errorPhoto.value
                    )
                }
            }
        }
    }
}

private fun getOutputDirectory(context: Context): File {
    val mediaDir = context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES).firstOrNull().let {
        File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    return mediaDir
}


@Composable
fun LottieView() {
    Box() {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_anim))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever,
            speed = 0.5f
        )
        LottieAnimation(composition, progress)
    }

}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun ProfilePic(
    photoUrl: String = "",
    photoPath: String = "",
    isModifiable: Boolean,
    onPhotoTaken: (String) -> Unit,
    error: String = ""
) {

    var isCameraShown by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.padding(16.dp)) {
            if (isCameraShown) {
                RoundedCameraView(
                    filename = "compressed",
                    outputDirectory = getOutputDirectory(LocalContext.current),
                    //executor = ,
                    onImageCaptured = {
                        onPhotoTaken(it.toFile().absolutePath)
                        isCameraShown = false
                    },
                    onError = {
                        Toast.makeText(context, "something failed", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                if (photoUrl.isNotEmpty() || photoPath.isNotEmpty()) {

                    var painter = rememberImagePainter(
                        data = if (photoPath.isNotEmpty()) Uri.fromFile(File(photoPath))
                            .toString() else photoUrl,
                        builder = {
                            placeholder(R.drawable.small_camera)
                        }
                    )
                    Image(
                        painter = painter,
                        contentDescription = "photo face",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                    )
                } else {
                    LottieView()
                }
                if (isModifiable) {
                    FloatingActionButton(
                        onClick = { isCameraShown = true },
                        backgroundColor = Color.LightGray,
                        modifier = Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "take photo"
                        )
                    }
                }
            }
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }


}

@Preview(device = Devices.NEXUS_10)
@OptIn(ExperimentalCoilApi::class, kotlinx.coroutines.InternalCoroutinesApi::class)
@Composable
fun StudentProfileShow(
    studentState: StudentState = StudentState(StudentMutableState(Student(key = "S0"))),
    isModifiable: Boolean = true,
    modify: (Boolean) -> Unit = {},
    onFirstNameChange: (String) -> Unit = {},
    firstNameError: String = "",
    onLastNameChange: (String) -> Unit = {},
    lastNameError: String = "",
    onDoBChange: (String) -> Unit = {},
    onGenderChange: (String) -> Unit = {},
    genderError: String = "",
    onOccupationChange: (String) -> Unit = {},
    onCityChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit = {},
    phoneError: String = "",
    onPhotoTake: (File?) -> Unit = {},
    photoError: String = "",
) {


    //photoPath.value=studentState.photoPath.value.toString()

    Spacer(modifier = Modifier.height(24.dp))

    val photoPath = remember {
        mutableStateOf("")
    }
    ProfilePic(
        photoUrl = studentState.photoPath.value,
        photoPath = photoPath.value,
        isModifiable = isModifiable,
        onPhotoTaken = {
            photoPath.value = it
            val file = if (it.isNotEmpty()) File(it) else null
            onPhotoTake(file)
        },
        error = photoError
    )



    OutlinedTextFieldValidation(
        value = studentState.firstName.value,
        onValueChange = { onFirstNameChange(it) },
        label = { Text(text = "First name") },
        enabled = isModifiable,
        error = firstNameError
    )

    OutlinedTextFieldValidation(
        value = studentState.lastName.value,
        onValueChange = { onLastNameChange(it) },
        label = { Text(text = "Last name") },
        enabled = isModifiable,
        error = lastNameError
    )

    OutlinedTextFieldValidation(
        value = studentState.phone.value,
        onValueChange = { onPhoneChange(it) },
        label = { Text(text = "Phone number") },
        enabled = isModifiable,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
        error = phoneError,
        visualTransformation = PhoneNumberVisualTransformation("NI")
    )


    val cal = Calendar.getInstance()
    val picker = DatePickerDialog(
        LocalContext.current,
        { _, year, month, dayOfMonth ->
            cal.clear()
            cal.set(year, month, dayOfMonth)
            onDoBChange(cal.time.toDateString())
        },
        2000, 0, 1
    )

    OutlinedTextFieldValidation(
        enabled = isModifiable,
        readOnly = true,
        value = studentState.dob.value.toDate()?.toReadableDateString() ?: "",
        onValueChange = { },
        label = { Text(text = "Date of birth") },
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                if (isModifiable)
                    LaunchedEffect(interactionSource) {
                        val flow: Flow<Interaction> = interactionSource.interactions
                        flow.collect { test: Interaction ->
                            if (test is PressInteraction.Release) {
                                picker.show()
                            }
                        }
                    }
            }
    )

    DropdownTextField(
        enabled = isModifiable,
        label = "Gender",
        options = Gender::class.nestedClasses.map { (it.objectInstance as Gender).value },
        value = studentState.gender.value,
        onValueChange = { onGenderChange(it) }
    )

    DropdownTextField(
        enabled = isModifiable,
        label = "Occupation",
        options = Occupation::class.nestedClasses.map { (it.objectInstance as Occupation).value },
        value = studentState.occupation.value,
        onValueChange = { onOccupationChange(it) }
    )

    DropdownTextField(
        enabled = isModifiable,
        label = "Branch",
        options = City::class.nestedClasses.map { (it.objectInstance as City).value },
        value = studentState.city.value,
        onValueChange = { onCityChange(it) }
    )

    Spacer(modifier = Modifier.height(96.dp))

}



