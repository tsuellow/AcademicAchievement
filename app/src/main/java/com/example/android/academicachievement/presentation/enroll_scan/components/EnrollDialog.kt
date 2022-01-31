package com.example.android.academicachievement.presentation.enroll_scan.components

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberImagePainter
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.model.Student
import com.example.android.academicachievement.presentation.enroll_choose_course.components.CourseItem
import com.example.android.academicachievement.presentation.enroll_scan.ConfirmDialogState
import com.example.android.academicachievement.presentation.enroll_scan.DialogState

@Composable
fun EnrollDialog(dialogState: State<DialogState>,
                 confirmDialogState: ConfirmDialogState,
                 onSubmitButtonClick: () -> Unit,
                 onDismissRequest: () -> Unit) {

    var overrule by remember { mutableStateOf(false)}
    var overrulePin by remember { mutableStateOf("")}

    Dialog(onDismissRequest = { onDismissRequest.invoke() }) {
        Surface(modifier = Modifier
            .width(400.dp)
            .height(600.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally) {

                DialogTitle(dialogState = dialogState)

                if (dialogState.value.isLoading){
                    CircularProgressIndicator()
                }else{

                    dialogState.value.personalData?.let {
                        ProfileView(student = dialogState.value.personalData!!)
                    }

                    DialogSubTitle(dialogState = dialogState)

                    Spacer(modifier = Modifier.padding(8.dp))

                    Observations(dialogState = dialogState)

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (!dialogState.value.isApproved){
                        OutlinedTextField(value = overrulePin, 
                            label = { Text(text = "Overrule PIN")},
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            onValueChange = {
                            overrulePin=it
                            if (overrulePin=="1111"){overrule=true}
                            })
                    }
                }
                Column() {

                    Divider(Modifier.padding(8.dp))

                    Row (modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween){

                        Button(modifier = Modifier.padding(8.dp),
                            onClick = {
                            onDismissRequest.invoke()
                        },
                            shape = MaterialTheme.shapes.medium) {
                            Text(text = "Cancel")

                        }

                        Spacer(modifier = Modifier.padding(16.dp))

                        ConfirmButton(
                            confirmDialogState = confirmDialogState,
                            dialogState = dialogState,
                            overrule = overrule,
                            onSubmit = onSubmitButtonClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmButton(confirmDialogState: ConfirmDialogState, dialogState: State<DialogState>, overrule:Boolean, onSubmit:()->Unit){
    var color=MaterialTheme.colors.primary
    var enabled=dialogState.value.isApproved || overrule
    var isClickable=false
    when(confirmDialogState){
        is ConfirmDialogState.Unpressed->{
            isClickable=true
        }
        is ConfirmDialogState.Loading ->{
            color=Color.Gray
        }
        is ConfirmDialogState.Failed -> {
            color=Color.Red
        }
        is ConfirmDialogState.Success -> {
            color=Color.Green
        }
    }

    var sizeText by remember { mutableStateOf(IntSize.Zero) }

    Button(modifier = Modifier.padding(8.dp),
        onClick = {
            if (isClickable)
            onSubmit()
        },
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = MaterialTheme.shapes.medium,
        enabled = enabled) {
        when(confirmDialogState){
            is ConfirmDialogState.Unpressed->{
                Text(text = "Confirm", modifier = Modifier.onGloballyPositioned { sizeText=it.size })
            }
            is ConfirmDialogState.Loading ->{
                Box(Modifier.size(with(LocalDensity.current){ (sizeText.width).toDp()},with(LocalDensity.current){ (sizeText.height).toDp()}),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier
                        .padding(1.dp).size(with(LocalDensity.current){ (sizeText.height).toDp()}), color =  Color.White )
                }
            }
            is ConfirmDialogState.Failed -> {
                Text(text = "Failed")
            }
            is ConfirmDialogState.Success -> {
                Box(Modifier.size(with(LocalDensity.current){ (sizeText.width).toDp()},with(LocalDensity.current){ (sizeText.height).toDp()}),
                    contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Filled.Done, contentDescription ="Done", tint = Color.White )
                }
            }
        }
    }
}

@Composable
fun ProfileView(
    student: Student
){
    Card(backgroundColor = Color.Gray,
        shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier =Modifier.padding(2.dp), contentAlignment = Alignment.Center ){
                CircularProgressIndicator(modifier = Modifier
                    .size(100.dp)
                    .padding(14.dp))
                Image(
                    painter = rememberImagePainter(student.photoPath),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape),
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = student.firstName+" "+student.lastName,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp)
        }
    }


}

@Composable
fun DialogTitle(dialogState: State<DialogState>){
    val title=if (dialogState.value.isLoading){
        "Loading data..."
    } else {
        if (dialogState.value.error!=null){
            "Error"
        }else{
            if(dialogState.value.isApproved) "Please confirm enrollment" else "Unable to enroll"
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(text = title, style = MaterialTheme.typography.h5)

        Divider(Modifier.padding(8.dp))
    }

}

@Composable
fun DialogSubTitle(dialogState: State<DialogState>){
    if (dialogState.value.error!=null){
        Column {
            Text(text = "Unable to process request", color = Color.Red, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = dialogState.value.error!!)
        }

    }else{
        if(dialogState.value.isApproved){
            Text(text = "Enrollment approved", color = Color.Green, style = MaterialTheme.typography.h6)
        } else{
            Text(text = "Enrollment denied", color = Color.Red, style = MaterialTheme.typography.h6)
        }
    }
}

@Composable
fun Observations(dialogState: State<DialogState>){
    if (dialogState.value.error==null){
        if (dialogState.value.observations.size>0){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (dialogState.value.isApproved) "Observations:" else "Reasons:",
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp)
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)){
                    items(dialogState.value.observations){
                        Text(text = "- $it")
                        Spacer(modifier = Modifier.padding(4.dp))
                    }
                }
            }

        }
    }
}