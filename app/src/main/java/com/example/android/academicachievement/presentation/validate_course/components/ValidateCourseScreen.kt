package com.example.android.academicachievement.presentation.validate_course.components

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.model.Milestone
import com.example.android.academicachievement.domain.model.Part
import com.example.android.academicachievement.domain.model.Student
import com.example.android.academicachievement.presentation.common_composables.ProfileTopBar
import com.example.android.academicachievement.presentation.enroll_scan.components.StudentInexistentDialog
import com.example.android.academicachievement.presentation.ui.theme.MyGray
import com.example.android.academicachievement.presentation.ui.theme.MyGreen
import com.example.android.academicachievement.presentation.ui.theme.MyRed
import com.example.android.academicachievement.presentation.validate_course.CurrentCourseState
import com.example.android.academicachievement.presentation.validate_course.StudentDataState
import com.example.android.academicachievement.presentation.validate_course.ValidateCourseViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ValidateCourseScreen(
    viewModel: ValidateCourseViewModel,
    navController: NavController,
    onButtonClick: (path: String) -> Unit
) {

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //topbar
        when (val studentState: StudentDataState = viewModel.studentDataState.value) {
            is StudentDataState.Loading -> {
                ProfileTopBar(
                    student = Student(
                        key = "S" + viewModel.studentId.value,
                        firstName = "Loading data for"
                    )
                )
            }
            is StudentDataState.Inexistent -> {
                TopAppBar(title = { Text("No such student ID: ${viewModel.studentId.value}") },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "error", tint = MyRed
                        )
                    }
                )
                Text(text = "Nothing to show", modifier = Modifier.padding(16.dp), color = MyRed)
                StudentInexistentDialog(
                    studentId = viewModel.studentId.value,
                    hideDialog = { navController.popBackStack() },
                    doOnConfirm = {navController.navigate(Screen.StudentProfile.withId(viewModel.studentId.value)){
                        popUpTo(Screen.ValidateScanner.route)
                    } }
                )
            }
            is StudentDataState.Failed -> {
                TopAppBar(title = { Text("Failed to load ID: ${viewModel.studentId.value}") },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "error", tint = MyRed
                        )
                    }
                )
                studentState.error?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MyRed
                    )
                }
            }
            is StudentDataState.Success -> {
                ProfileTopBar(student = studentState.personalData)
            }
        }
        //body
        when (val currentCourseState: CurrentCourseState = viewModel.currentCourseState.value) {
            is CurrentCourseState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
            is CurrentCourseState.Failed -> {
                currentCourseState.error?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is CurrentCourseState.Success -> {
                currentCourseState.course?.let {
                    CourseTab(course = it, onButtonClick = onButtonClick)
                }
            }
            is CurrentCourseState.NoCourse -> {
                Text(
                        "No current course data was found for this student. Student has not yet been enrolled into any course",
                        modifier = Modifier.padding(16.dp)
                    )

            }

        }
    }
}


@Composable
fun CourseTab(course: Course, onButtonClick: (path: String) -> Unit) {

    val state = rememberLazyListState()
    val filterMap =
        HashMap(course.parts.mapValues { PartFilterItem(it.key + ". " + it.value.name, 3) })

    Column(Modifier.fillMaxSize()) {
        Surface(
            color = MyGray,
            //MaterialTheme.colors.primaryVariant,
            elevation = 16.dp
        ) {
            Column() {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 8.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = course.key + ". ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.wrapContentWidth(align = Alignment.Start),
                        color = Color.White
                    )
                    Text(
                        text = course.name,
                        fontSize = 22.sp,
                        modifier = Modifier.wrapContentWidth(
                            align = Alignment.Start,
                            unbounded = false
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!course.completed) {
                        Icon(
                            imageVector = Icons.Filled.QueryBuilder,
                            contentDescription = "In progress",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Done",
                            tint = MyGreen,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
                PartFilter(filterMap = filterMap, lazyListState = state)
            }
        }
        JointCourse(
            course = course,
            lazyListState = state,
            filterMap = filterMap,
            onButtonClick = onButtonClick
        )
    }

}

data class PartFilterItem(var name: String = "", var position: Int = 3)

@Composable
fun PartFilter(filterMap: HashMap<String, PartFilterItem>, lazyListState: LazyListState) {
    val coroutineScope = rememberCoroutineScope()
    LazyRow() {
        items(filterMap.map { it.value }) { partFilterItem ->
            Button(
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.secondary,
                    contentColor = Color.White
                ),
                onClick = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(partFilterItem.position)
                        Log.d("testino6", "" + partFilterItem.position)
                    }
                }) {
                Text(
                    text = partFilterItem.name,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(min = 56.dp, max = 300.dp)
                )
            }

        }

    }
}

@Composable
fun JointCourse(
    course: Course,
    lazyListState: LazyListState,
    filterMap: HashMap<String, PartFilterItem>,
    onButtonClick: (path: String) -> Unit
) {
    LazyColumn(state = lazyListState) {
        var i: Int = 0
        for ((keyPart, part) in course.parts) {
            item { PartTab(part = part, modifier = Modifier) }
            filterMap[keyPart]?.position = i
            for ((keyMilestone, milestone) in part.milestones) {
                item {
                    if (!keyMilestone.contentEquals("m1")) {
                        Divider(Modifier.padding(8.dp))
                    }
                    MilestoneTab(
                        milestone = milestone,
                        validate = { onButtonClick(course.key + "/" + part.key + "/" + milestone.key) })
                }
                i++
            }
            i++
        }
    }
}

@Composable
fun PartTab(part: Part, modifier: Modifier) {

    Column(modifier = modifier) {
        Surface(
            color = Color.LightGray,
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = part.key + ". ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = part.name, fontSize = 18.sp)
                }
                if (!part.completed) {
                    Icon(
                        imageVector = Icons.Filled.QueryBuilder,
                        contentDescription = "In progress",
                        tint = Color.Black,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Done",
                        tint = MyGreen,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }
//        LazyColumn(){
//            items(part.milestones.map { it.value }){milestone->
//                MilestoneTab(milestone = milestone, validate = {onValidate()})
//            }
//        }
    }
}

@Composable
fun MilestoneTab(milestone: Milestone, validate: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Text(text = milestone.key + ". ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = milestone.name,
                fontSize = 16.sp,
                modifier = Modifier.wrapContentWidth(align = Alignment.Start, unbounded = false)
            )
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Box(modifier = Modifier.width(120.dp), contentAlignment = Alignment.Center) {
            if (!milestone.completed) {
                Button(
                    onClick = { validate() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MyGray),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "validate", color = Color.White)
                }
            } else {
                IconButton(onClick = { validate() }, modifier = Modifier.padding(8.dp)) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Done",
                        tint = MyGreen
                    )
                }
            }
        }

    }
}


