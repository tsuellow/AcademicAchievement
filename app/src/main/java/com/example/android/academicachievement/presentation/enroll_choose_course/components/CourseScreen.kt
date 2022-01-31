package com.example.android.academicachievement.presentation.enroll_choose_course.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.presentation.common_composables.MyTopBar
import com.example.android.academicachievement.presentation.enroll_choose_course.EnrollChooseCourseViewModel

@Composable
fun CourseScreen(
    navController:NavController,
    viewModel:EnrollChooseCourseViewModel = hiltViewModel()
){
    val state=viewModel.state.value

    Column(Modifier.fillMaxSize()) {
        MyTopBar(title = "Courses")
        Box(Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()){
                items(state.courseList.map{it.value.toCourseDto(it.key)}){ course ->
                    CourseItem(course = course, onItemClicked = { clickedCourse -> navController.navigate(Screen.EnrollScanner.route+"/${clickedCourse.key}")})
                    Divider(Modifier.padding(horizontal = 8.dp))
                }
            }
            if(state.isLoading){
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(0.3f)
                        .padding(20.dp))
            }
            if(state.error.isNotEmpty()){
                Text(text = state.error, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            }
        }

    }


}