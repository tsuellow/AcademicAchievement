package com.example.android.academicachievement.presentation.validate_course.components

import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.model.Milestone
import com.example.android.academicachievement.domain.model.Part
import com.example.android.academicachievement.presentation.common_composables.MyTopBar
import com.example.android.academicachievement.presentation.enroll_scan.EnrollScanViewModel
import com.example.android.academicachievement.presentation.ui.theme.MyGray
import com.example.android.academicachievement.presentation.ui.theme.MyGreen
import com.example.android.academicachievement.presentation.validate_course.ValidateCourseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun ValidateCourseScreen(
    viewModel: EnrollScanViewModel = hiltViewModel()
){
    Column(Modifier.fillMaxSize()) {
        
    }
}


@Composable
fun CourseTab(course:Course) {
    Column() {
        Surface(
            color = MaterialTheme.colors.primarySurface,
            shape = RoundedCornerShape(2.dp),
            elevation = 16.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp)) {
                Row() {
                    Text(text = course.key, fontSize = 22.sp)
                    Text(text = course.name, fontSize = 22.sp)
                }
                if (course.completed){
                    Icon(imageVector = Icons.Filled.QueryBuilder, contentDescription ="In progress", tint = MyGray )
                }else{
                    Icon(imageVector = Icons.Filled.Done, contentDescription ="Done", tint = MyGreen )
                }
            }
        }
        LazyColumn(){
            items(course.parts.map { it.value }){part ->
                PartTab(part = part)
            }
        }
    }
}

@Composable
fun PartTab(part: Part) {

    Column() {
        Surface(
            color = MaterialTheme.colors.primarySurface,
            shape = RoundedCornerShape(2.dp),
            elevation = 8.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp)) {
                Row() {
                    Text(text = part.key, fontSize = 18.sp)
                    Text(text = part.name, fontSize = 18.sp)
                }
                if (part.completed){
                    Icon(imageVector = Icons.Filled.QueryBuilder, contentDescription ="In progress", tint = MyGray )
                }else{
                    Icon(imageVector = Icons.Filled.Done, contentDescription ="Done", tint = MyGreen )
                }
            }
        }
        LazyColumn(){
            items(part.milestones.map { it.value }){milestone->
                MilestoneTab(milestone = milestone, validate = {onValidate()})
            }
        }
    }
}

@Composable
fun MilestoneTab(milestone: Milestone, validate: () -> Unit) {

    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp)) {
        Row() {
            Text(text = milestone.key, fontSize = 16.sp)
            Text(text = milestone.name, fontSize = 16.sp)
        }
        if (milestone.completed){
            Button(onClick = { validate()}, colors = ButtonDefaults.buttonColors(backgroundColor = MyGray)) {
                Text(text = "validate")
            }
        }else{
                Button(onClick = { validate()}, colors = ButtonDefaults.buttonColors(backgroundColor = MyGreen), shape = RoundedCornerShape(50)) {
                    Icon(imageVector = Icons.Filled.Done, contentDescription ="Done", tint = Color.White )
            }
        }
    }
}

fun onValidate():Unit{
    Log.d("test","test")
}

