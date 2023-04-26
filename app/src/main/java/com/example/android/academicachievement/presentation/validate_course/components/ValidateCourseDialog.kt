package com.example.android.academicachievement.presentation.validate_course.components

import android.content.Context
import android.util.Log
import java.util.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberImagePainter
import com.example.android.academicachievement.Constants
import com.example.android.academicachievement.common.PreferenceManager
import com.example.android.academicachievement.domain.model.Milestone
import com.example.android.academicachievement.domain.model.Student
import com.example.android.academicachievement.presentation.enroll_scan.ConfirmDialogState
import com.example.android.academicachievement.presentation.ui.theme.MyGreen
import com.example.android.academicachievement.presentation.ui.theme.MyRed
import com.example.android.academicachievement.presentation.validate_course.ValidateDialogState
import com.example.android.academicachievement.util.toDateTimeString

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ValidateCourseDialog(
    validateDialogState: ValidateDialogState,
    confirmDialogState: ConfirmDialogState,
    onSubmitButtonClick: (milestone: Milestone) -> Unit,
    onDismissRequest: () -> Unit,
    loginData:PreferenceManager.LoginData
) {


    var milestone by remember { mutableStateOf(Milestone()) }
    val context = LocalContext.current
    var overRulePin by remember {
        mutableStateOf("")
    }
    Dialog(properties = DialogProperties(
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false
    ),
        onDismissRequest = { onDismissRequest.invoke() }) {
        Surface(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(0.9f)
            //.width(400.dp)
            //.height(600.dp),
            , shape = RoundedCornerShape(10.dp)
        ) {

            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .wrapContentHeight(),
                //verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                DialogTitle(validateDialogState)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.weight(1f)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                    //verticalArrangement = Arrangement.SpaceBetween
                ) {

                    when (validateDialogState) {
                        is ValidateDialogState.Loading -> {
                            CircularProgressIndicator(Modifier.padding(24.dp))
                        }
                        is ValidateDialogState.Failed -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Error",
                                    color = MyRed,
                                    style = MaterialTheme.typography.h6
                                )
                                Spacer(modifier = Modifier.padding(16.dp))
                                Text(
                                    text = "Could not retrieve milestone data:" + (validateDialogState.error
                                        ?: "unexpected error")
                                )
                            }

                        }
                        is ValidateDialogState.Success -> {
                            if (milestone.key.contentEquals("0")) {
                                milestone = validateDialogState.course!!.getMilestoneByPath(
                                    validateDialogState.path
                                )!!
                            }
                            validateDialogState.personalData?.let { ProfileTab(student = it) }
                            Spacer(modifier = Modifier.padding(8.dp))
                            RootedMilestoneTitle(validateDialogState = validateDialogState)
                            if (milestone.completed) {
                                AlreadyValidatedScreen(
                                    milestone = milestone,
                                    overRulePin = overRulePin,
                                    doOnPinTyped = { overRulePin = it })
                            } else {
                                ValidationNeededScreen(milestone)
                            }

                        }
                    }

                }

                Column() {

                    Divider(Modifier.padding(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Button(
                            modifier = Modifier.padding(8.dp),
                            onClick = {
                                onDismissRequest.invoke()
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(text = "Cancel")

                        }

                        Spacer(modifier = Modifier.padding(16.dp))

                        ConfirmButtonValidate(
                            confirmDialogState = confirmDialogState,
                            validateDialogState = validateDialogState,
                            overRule = (overRulePin.contentEquals(loginData.pin)),
                            onSubmit = {
                                val milestoneCopy = milestone.copy()
                                completeMilestone(milestoneCopy, loginData)
                                onSubmitButtonClick(milestoneCopy)
                            }
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun DialogTitle(validateDialogState: ValidateDialogState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (validateDialogState is ValidateDialogState.Success) {
            with(validateDialogState) {
                if (course!!.getMilestoneByPath(path)!!.completed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Milestone done!", style = MaterialTheme.typography.h5)
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "done",
                            tint = MyGreen,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    Text(text = "Validate Milestone", style = MaterialTheme.typography.h5)
                }
            }

        } else {
            Text(text = "Validate Milestone", style = MaterialTheme.typography.h5)
        }

        Divider(Modifier.padding(8.dp))
    }
}

@Composable
fun ValidationNeededScreen(milestone: Milestone) {

    Log.d("sesteo", "" + milestone.grades["grade"])
    var comment by remember {
        mutableStateOf("")
    }

    Spacer(modifier = Modifier.padding(8.dp))

    LazyColumn() {
        items(milestone.grades.map { it.key }) { key ->
            GradingItem(key = key, milestone = milestone)
        }
    }
    Spacer(modifier = Modifier.padding(4.dp))

    OutlinedTextField(
        value = comment,
        onValueChange = {
            comment = it
            milestone.comment = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        label = { Text(text = "Comment") },
        singleLine = false,
        maxLines = 3
    )
}

fun completeMilestone(milestone: Milestone, loginData: PreferenceManager.LoginData) {
    if (milestone.completed) {
        milestone.date = "never"
        milestone.validatedBy = ""
        milestone.completed = false
    } else {
        milestone.date = Date().toDateTimeString()
        milestone.validatedBy = loginData.fullName
        milestone.completed = true
    }

}

@Composable
fun AlreadyValidatedScreen(
    milestone: Milestone,
    overRulePin: String,
    doOnPinTyped: (String) -> Unit
) {

    Spacer(modifier = Modifier.padding(8.dp))

    LazyColumn() {
        items(milestone.grades.map { it.key }) { key ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                backgroundColor = MyGreen,
                shape = RoundedCornerShape(50),
                contentColor = Color.White
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    )
                ) {
                    Text(text = "$key:", fontWeight = FontWeight.Bold)
                    Text(text = "" + milestone.grades[key], fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    Spacer(modifier = Modifier.padding(8.dp))

    if (milestone.comment.isNotEmpty()) {
        Text(text = "Comment:", fontWeight = FontWeight.Bold)
        Text(text = milestone.comment)
    }

    Spacer(modifier = Modifier.padding(8.dp))

    Text(text = "Validated by:", fontWeight = FontWeight.Bold)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = milestone.validatedBy,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "on: " + milestone.date, fontStyle = FontStyle.Italic)
    }
    OutlinedTextField(value = overRulePin,
        onValueChange = { doOnPinTyped(it) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier
            .fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
        label = { Text(text = "Overrule PIN") })

}

@Composable
fun RootedMilestoneTitle(validateDialogState: ValidateDialogState.Success) {
    val course = validateDialogState.course
    val path = validateDialogState.path
    Column() {
        if (course != null) {
            course.getMilestoneName(path)
                ?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.h6,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            Spacer(modifier = Modifier.padding(1.dp))
            course.getMilestoneCourseName(path)
                ?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.subtitle2,
                        color = Color.Gray,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            course.getMilestonePartName(path)
                ?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.subtitle2,
                        color = Color.LightGray,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
        }

    }
}


@Composable
fun GradingItem(key: String, milestone: Milestone) {
    var gradeValue by remember {
        mutableStateOf(milestone.grades[key])
    }
    Card(
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(50),
        backgroundColor = MaterialTheme.colors.secondary,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = key,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (gradeValue!! > 1) {
                        gradeValue = gradeValue!! - 1
                        milestone.grades[key] = gradeValue!!
                    }
                }, modifier = Modifier.clip(CircleShape)) {
                    Icon(imageVector = Icons.Default.RemoveCircle, contentDescription = "minus")
                }

                Text(
                    text = "" + gradeValue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(onClick = {
                    if (gradeValue!! < 10) {
                        gradeValue = gradeValue!! + 1
                        milestone.grades[key] = gradeValue!!
                    }
                }, modifier = Modifier.clip(CircleShape)) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = "plus")
                }
            }
        }
    }

}


@Composable
fun ConfirmButtonValidate(
    confirmDialogState: ConfirmDialogState,
    validateDialogState: ValidateDialogState,
    overRule: Boolean,
    onSubmit: () -> Unit
) {
    var alreadyValid = false
    var enabled = false
    if (validateDialogState is ValidateDialogState.Success) {
        enabled = true
        if (validateDialogState.course!!.getMilestoneByPath(validateDialogState.path)!!.completed) {
            alreadyValid = true
            enabled = false
        }
        if (overRule) {
            enabled = true
        }
    }

    var color = MaterialTheme.colors.primary
    var isClickable = false
    when (confirmDialogState) {
        is ConfirmDialogState.Unpressed -> {
            isClickable = true
        }
        is ConfirmDialogState.Loading -> {
            color = Color.Gray
        }
        is ConfirmDialogState.Failed -> {
            color = Color.Red
        }
        is ConfirmDialogState.Success -> {
            color = Color.Green
        }
    }

    var sizeText by remember { mutableStateOf(IntSize.Zero) }

    Button(
        modifier = Modifier.padding(8.dp),
        onClick = {
            if (isClickable)
                onSubmit()
        },
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = MaterialTheme.shapes.medium,
        enabled = enabled
    ) {
        when (confirmDialogState) {
            is ConfirmDialogState.Unpressed -> {
                val text = if (alreadyValid) "Invalidate" else "Confirm"
                Text(text = text, modifier = Modifier.onGloballyPositioned { sizeText = it.size })
            }
            is ConfirmDialogState.Loading -> {
                Box(
                    Modifier.size(with(LocalDensity.current) { (sizeText.width).toDp() }, with(
                        LocalDensity.current
                    ) { (sizeText.height).toDp() }),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier
                        .padding(1.dp)
                        .size(with(LocalDensity.current) { (sizeText.height).toDp() }),
                        color = Color.White
                    )
                }
            }
            is ConfirmDialogState.Failed -> {
                Text(text = "Failed")
            }
            is ConfirmDialogState.Success -> {
                Box(
                    Modifier.size(with(LocalDensity.current) { (sizeText.width).toDp() }, with(
                        LocalDensity.current
                    ) { (sizeText.height).toDp() }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Done",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileTab(
    student: Student
) {
    Card(
        backgroundColor = MaterialTheme.colors.primary,
        shape = RoundedCornerShape(percent = 50),
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.padding(2.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                )
                Image(
                    painter = rememberImagePainter(student.photoPath),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Column() {
                Text(
                    text = student.firstName + " " + student.lastName,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "ID: " + student.key.drop(1),
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun MilestoneDetailView() {

}



