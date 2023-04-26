package com.example.android.academicachievement.presentation.edit_courses.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.presentation.common_composables.EditTextDialog
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.data.remote.dto.CourseDto
import com.example.android.academicachievement.presentation.common_composables.MyTopBar
import com.example.android.academicachievement.presentation.edit_courses.EditCourseListViewModel
import com.example.android.academicachievement.presentation.enroll_choose_course.CourseListState


@Composable
fun EditCourseList(
    navController: NavController,
    viewModel: EditCourseListViewModel = hiltViewModel()
){
    var showDialog by remember {
        mutableStateOf(false)
    }
    Scaffold(topBar = { MyTopBar(title = "Edit Courses") },
        floatingActionButton = { FabAddCourse {
            if (viewModel.state.value.error.isEmpty()) {
                showDialog = true
            }
        }
        }) {

        EditCourseLazyList(navController = navController, viewModel.state.value)

        AddDialog(showDialog =showDialog , nextCourseId = viewModel.getNextCourse(), hideDialog = { showDialog=false }, doOnConfirm ={name->viewModel.saveNewCourse(name)} )


    }


}

@Composable
fun FabAddCourse(showDialog:()->Unit) {
    FloatingActionButton(onClick = { showDialog() }) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "add course")
    }
}

@Composable
fun AddDialog(showDialog:Boolean, nextCourseId:String, hideDialog:()->Unit, doOnConfirm:(String)->Unit) {
    val context= LocalContext.current
    var newCourseName by remember {
        mutableStateOf("")
    }
    if (showDialog){
        EditTextDialog(title = "Add new course $nextCourseId.", label = "Course name", hideDialog = {hideDialog()}, doOnConfirm ={
            Toast.makeText(context,"wait for list to update", Toast.LENGTH_SHORT).show()
            doOnConfirm(it)} )
//        AlertDialog(onDismissRequest = { hideDialog()},
//        confirmButton = {
//            Button(onClick = { if (newCourseName.isNotEmpty()) {
//                Toast.makeText(context,"wait for list to update", Toast.LENGTH_SHORT).show()
//                doOnConfirm(newCourseName)
//                hideDialog()
//            }else{
//                Toast.makeText(context,"Course name cannot be empty", Toast.LENGTH_SHORT).show()
//            } }) {
//                Text(text = "Confirm")
//            }
//            },
//        dismissButton = {
//            Button(onClick = {hideDialog()}) {
//                Text(text = "Cancel")
//            }},
//        title = { Text(text = "Add new course $nextCourseId.", style = MaterialTheme.typography.h5)},
//        text = {
//            OutlinedTextField(value = newCourseName,
//                onValueChange = {newCourseName=it},
//                label = { Text(text = "Course Name")})
//
//        })
    }
}


@Composable
fun EditCourseLazyList(
    navController: NavController,
    courseListState: CourseListState
){

    val context=LocalContext.current

    Column(Modifier.fillMaxSize()) {

        Box(Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()){
                items(courseListState.courseList.map{it.value.toCourseDto(it.key)}.sortedBy { it.key }){ course ->
                    CourseEditItem(course = course, onEditItem = { clickedCourse -> navController.navigate(
                        Screen.EditCourse.withId(clickedCourse.key))},
                        onDeleteItem = { Toast.makeText(context, "not yet supported!", Toast.LENGTH_SHORT).show()})
                    Divider(Modifier.padding(horizontal = 8.dp))
                }
            }
            if(courseListState.isLoading){
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(0.3f)
                        .padding(20.dp))
            }
            if(courseListState.error.isNotEmpty()){
                Text(text = courseListState.error, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            }
        }

    }
}

@Composable
fun CourseEditItem(
    course: CourseDto,
    onEditItem: (CourseDto)->Unit,
    onDeleteItem: (CourseDto)->Unit
){

    Row(modifier = Modifier
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {

            Text(modifier = Modifier
                .padding(16.dp),
                text = course.key+".",
                style = MaterialTheme.typography.h5)

            Text(text = course.name,
                style = MaterialTheme.typography.body1,
                overflow = TextOverflow.Ellipsis)

        }


        Row(horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically) {

            IconButton(onClick = { onEditItem(course) }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "edit")
            }

            IconButton(onClick = { onDeleteItem(course) }) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
            }
        }


    }

}