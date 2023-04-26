package com.example.android.academicachievement.presentation.edit_single_course.components

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.data.remote.dto.CourseTemplateDto
import com.example.android.academicachievement.data.remote.dto.PartTemplateDto
import com.example.android.academicachievement.presentation.common_composables.MyTopBar
import com.example.android.academicachievement.presentation.edit_single_course.CourseTemplateState
import com.example.android.academicachievement.presentation.edit_single_course.EditCourseViewModel
import com.example.android.academicachievement.presentation.ui.theme.MyGray
import com.example.android.academicachievement.presentation.ui.theme.MyRed
import java.lang.Integer.min
import kotlin.math.max

@Composable
fun EditCourse(
    navController: NavController,
    viewModel: EditCourseViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    var courseTemplate by remember {
        mutableStateOf(
            CourseTemplateDto()
            , policy = neverEqualPolicy()
        )
    }
    val state = viewModel.courseTemplateState.value

    if (viewModel.saved.value) {
        navController.popBackStack(route = Screen.EditCourseList.route, inclusive = false)
    }

    var showExitDialog by remember {
        mutableStateOf(false)
    }
    BackHandler(enabled = true) {
        showExitDialog = true
    }
    if (showExitDialog) {
        ExitDialog(
            hideDialog = { showExitDialog = false },
            doOnConfirm = {
                navController.popBackStack(
                    route = Screen.EditCourseList.route,
                    inclusive = false
                )
            }
        )
    }


    Scaffold(
        topBar = { MyTopBar(title = "Edit Course ${viewModel.courseId}") },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Toast.makeText(context, "saving changes", Toast.LENGTH_SHORT).show()
                viewModel.saveChanges(courseTemplate)
            }) {
                Icon(imageVector = Icons.Filled.Save, contentDescription = "add course")
            }
        }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            when (state) {
                is CourseTemplateState.Loading -> {
                    CircularProgressIndicator(Modifier.padding(20.dp))
                }
                is CourseTemplateState.Failed -> {
                    Text(
                        text = "ERROR: try again later",
                        color = MyRed,
                        style = MaterialTheme.typography.h6
                    )
                }
                is CourseTemplateState.Success -> {
                    LaunchedEffect(Unit ){
                        courseTemplate = state.courseTemplate.copy()
                    }

//                CourseEdit(
//                    key =viewModel.courseId,
//                    courseTemplateDto =courseTemplate ,
//                    refreshView ={
//                        courseTemplate= courseTemplate.copy()
//                    }
//                )
                    JointCourseTemplate(
                        courseTemplateDto = courseTemplate,
                        key = viewModel.courseId,
                        refreshView = {
                            courseTemplate.parts.putAll(courseTemplate.parts)
                            courseTemplate = courseTemplate.copy() },
                        refreshExplicit = {courseTemplate=it}
                    )
                }
            }
        }
    }


}

@Composable
fun JointCourseTemplate(
    courseTemplateDto: CourseTemplateDto,
    key: String,
    refreshView: () -> Unit,
    refreshExplicit:(CourseTemplateDto)->Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var partKey: String? by remember {
        mutableStateOf(null)
    }
    var msKey: String? by remember {
        mutableStateOf(null)
    }
    var deleteOpt: Boolean by remember {
        mutableStateOf(false)
    }

//    val partsMut = remember {
//        mutableStateMapOf<String, PartTemplateDto>()
//    }
//    partsMut.putAll(courseTemplateDto.parts)

    fun setIntent(part: String? = null, key: String? = null, delete: Boolean = false) {
        partKey = part
        msKey = key
        deleteOpt = delete
        //refreshView()
    }

    Column(Modifier.fillMaxWidth()) {
        CourseEdit(
            courseTemplateDto = courseTemplateDto,
            key = key,
            refreshView = {
                refreshView()
                //partsMut.putAll(courseTemplateDto.parts)
            },
            refreshExplicit = {refreshExplicit(it)}
        )
        LazyColumn(state = listState) {

            for ((keyPart, part) in courseTemplateDto.parts) {
                item {
                        PartEdit(
                        courseTemplateDto = courseTemplateDto,
                        key = keyPart,
                        doAddMilestone = {
                            setIntent(
                                part = keyPart,
                                key = part.keyForNextMilestone()
                            )
                        },
                        doOnRename = { setIntent(part = keyPart) },
                        doOnDelete = { setIntent(part = keyPart, delete = true) }
                    )
                }
                for ((keyMilestone, milestone) in part.milestones) {
                    item {
                        if (!keyMilestone.contentEquals("m1")) {
                            Divider(Modifier.padding(8.dp))
                        }
                        MilestoneEdit(
                            courseTemplateDto = courseTemplateDto,
                            partKey = keyPart,
                            key = keyMilestone,
                            doOnRename = { setIntent(part = keyPart, key = keyMilestone) },
                            doOnDelete = {
                                setIntent(
                                    part = keyPart,
                                    key = keyMilestone,
                                    delete = true
                                )
                            }
                        )
                    }
                }
            }
        }
    }


    if (partKey != null) {
        if (msKey != null) {
            if (deleteOpt) {
                ConfirmDeletionDialog(
                    key = msKey!!,
                    hideDialog = { setIntent() },
                    doOnConfirm = {
                        courseTemplateDto.parts[partKey]!!.milestones.remove(msKey)
                        courseTemplateDto.parts[partKey]!!.reKeyMilestones()
                    })
            } else {
                RenameDialog(
                    key = msKey!!,
                    oldName = courseTemplateDto.parts[partKey]!!.milestones[msKey] ?: "",
                    hideDialog = { setIntent() },
                    doOnConfirm = { newName ->
                        courseTemplateDto.parts[partKey]!!.milestones[msKey!!] = newName
                    }
                )

            }
        } else {
            if (deleteOpt) {
                ConfirmDeletionDialog(
                    key = partKey!!,
                    hideDialog = { setIntent() },
                    doOnConfirm = {
                        courseTemplateDto.parts.remove(partKey)
                        courseTemplateDto.reKeyParts()
                        Log.d("kherson", courseTemplateDto.parts.toString())
                    })
            } else {
                PartDialog(
                    key = partKey!!,
                    oldName = courseTemplateDto.parts[partKey]!!.name,
                    gradingDims = HashMap(courseTemplateDto.parts[partKey]!!.gradingDimensions),
                    hideDialog = { setIntent() },
                    doOnConfirm = { name, dims ->
                        courseTemplateDto.parts[partKey]!!.name = name
                        courseTemplateDto.parts[partKey]!!.gradingDimensions.clear()
                        courseTemplateDto.parts[partKey]!!.gradingDimensions.putAll(dims)
                    }
                )

            }
        }

    }


}


@Composable
fun CourseEdit(courseTemplateDto: CourseTemplateDto, key: String, refreshView: () -> Unit, refreshExplicit: (CourseTemplateDto) -> Unit) {
    var showCourseDialog by remember {
        mutableStateOf(false)
    }
    var addPartDialog by remember {
        mutableStateOf(false)
    }
    Surface(
        color = MyGray,
        //MaterialTheme.colors.primaryVariant,
        elevation = 16.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, top = 8.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = key + ". ",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.wrapContentWidth(align = Alignment.Start),
                color = Color.White
            )
            Text(
                text = courseTemplateDto.name,
                fontSize = 22.sp,
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.Start, unbounded = false)
                    .weight(1f),
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { addPartDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "add part",
                        tint = Color.White,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
                IconButton(onClick = { showCourseDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "In progress",
                        tint = Color.White,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }


        }
    }

    if (showCourseDialog) {
        RenameDialog(
            key = key,
            oldName = courseTemplateDto.name,
            hideDialog = { showCourseDialog = false },
            doOnConfirm = { newName ->
                courseTemplateDto.name = newName
                Log.d("cagada",courseTemplateDto.name)
                //refreshView()
                refreshExplicit(courseTemplateDto)
            })
    }
    if (addPartDialog) {
        PartDialog(
            key = courseTemplateDto.keyForNextPart(),
            oldName = "",
            hideDialog = { addPartDialog = false },
            doOnConfirm = { name, dims ->
                courseTemplateDto.parts.put(
                    courseTemplateDto.keyForNextPart(),
                    PartTemplateDto(name = name, gradingDimensions = dims)
                )
                refreshView()
            })
    }

}

@Composable
fun PartEdit(
    courseTemplateDto: CourseTemplateDto,
    key: String,
    doAddMilestone: () -> Unit,
    doOnRename: () -> Unit,
    doOnDelete: () -> Unit
) {
    val partName = courseTemplateDto.parts[key]!!.name
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(text = key + ". ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = partName, fontSize = 18.sp, overflow = TextOverflow.Ellipsis)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { doAddMilestone() }) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "add milestone",
                        tint = Color.Black
                    )
                }
                IconButton(onClick = { doOnRename() }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "edit part",
                        tint = Color.Black
                    )
                }
                IconButton(onClick = { doOnDelete() }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "delete part",
                        tint = Color.Black
                    )
                }
            }

        }
    }
}

@Composable
fun MilestoneEdit(
    courseTemplateDto: CourseTemplateDto,
    partKey: String,
    key: String,
    doOnRename: () -> Unit,
    doOnDelete: () -> Unit
) {
    val name = courseTemplateDto.parts[partKey]!!.milestones[key]!!
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Text(text = key + ". ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = name,
                fontSize = 16.sp,
                modifier = Modifier.wrapContentWidth(align = Alignment.Start, unbounded = false)
            )
        }
        Spacer(modifier = Modifier.padding(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { doOnRename() }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "edit",
                    tint = MaterialTheme.colors.secondary
                )
            }
            IconButton(onClick = { doOnDelete() }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "delete",
                    tint = MaterialTheme.colors.secondary
                )
            }
        }
    }
}

@Composable
fun ConfirmDeletionDialog(key: String, hideDialog: () -> Unit, doOnConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = { hideDialog() },
        confirmButton = {
            Button(onClick = {
                doOnConfirm()
                hideDialog()
            }) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { hideDialog() }) {
                Text(text = "Cancel")
            }
        },
        title = { Text(text = "Confirm deletion of $key.", style = MaterialTheme.typography.h5) },
        text = {
            Text(text = "By confirming this feature and all related data will be deleted")
        })

}

@Composable
fun RenameDialog(
    key: String,
    oldName: String,
    hideDialog: () -> Unit,
    doOnConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    var newName by remember {
        mutableStateOf(oldName)
    }

    AlertDialog(onDismissRequest = { hideDialog() },
        confirmButton = {
            Button(onClick = {
                if (newName.isNotEmpty()) {
                    doOnConfirm(newName)
                    hideDialog()
                } else {
                    Toast.makeText(context, "Course name cannot be empty", Toast.LENGTH_SHORT)
                        .show()
                }
            }) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { hideDialog() }) {
                Text(text = "Cancel")
            }
        },
        title = { Text(text = "Enter name for $key.", style = MaterialTheme.typography.h5) },
        text = {
            OutlinedTextField(value = newName,
                onValueChange = { newName = it },
                label = { Text(text = "Name") })
        }
    )

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PartDialog(
    key: String, oldName: String, gradingDims: HashMap<String, Int> = hashMapOf("grade" to 7),
    hideDialog: () -> Unit, doOnConfirm: (String, HashMap<String, Int>) -> Unit
) {

    var newName by remember {
        mutableStateOf(oldName)
    }
    val gradingDimsCopy = remember {
        mutableStateMapOf<String, Int>()
    }
    gradingDimsCopy.putAll(gradingDims)

    Dialog(properties = DialogProperties(
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false
    ),
        onDismissRequest = { hideDialog() }) {
        Surface(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(0.9f),
            //.width(400.dp)
            //.height(600.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Enter name for $key.", style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(value = newName,
                    onValueChange = { newName = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    label = { Text(text = "Part Name") })
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = "Define grading dimensions", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.padding(4.dp))
                for ((dimKey, dimValue) in gradingDimsCopy) {
                    EditGradingDim(name = dimKey,
                        defaultGrade = dimValue,
                        onDelete = {
                            gradingDimsCopy.remove(it)
                            Log.d("grads", "" + gradingDimsCopy.size)
                            //gradingDimsCopy.putAll(gradingDimsCopy)
                        })
                }
                Spacer(modifier = Modifier.padding(4.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    backgroundColor = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(10),
                    contentColor = Color.White
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {

                        val focusManager = LocalFocusManager.current

                        var newGd by remember {
                            mutableStateOf("")
                        }
                        var newGdG by remember { mutableStateOf("") }
                        OutlinedTextField(modifier = Modifier.weight(2f),
                            value = newGd,
                            onValueChange = { newGd = it },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                            label = {
                                Text(
                                    text = "Dim. Name",
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            })
                        Spacer(modifier = Modifier.padding(4.dp))
                        OutlinedTextField(modifier = Modifier.weight(1f),
                            value = "" + newGdG,
                            onValueChange = { newGdG = it },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            label = {
                                Text(
                                    text = "default",
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            })
                        Spacer(modifier = Modifier.padding(8.dp))
                        IconButton(onClick = {
                            if (newGd.isNotEmpty()) {
                                gradingDimsCopy[newGd] = max(
                                    min(
                                        10, try {
                                            newGdG.toInt()
                                        } catch (e: NumberFormatException) {
                                            5
                                        }
                                    ), 1
                                )
                            }
                            newGd = ""
                            newGdG = ""
                            focusManager.clearFocus()
                            //gradingDimsCopy= HashMap(gradingDimsCopy)
                        }) {
                            Icon(imageVector = Icons.Filled.AddCircle, contentDescription = "add")
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Divider(modifier = Modifier.padding(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Button(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            hideDialog()
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "Cancel")

                    }

                    Spacer(modifier = Modifier.padding(16.dp))

                    Button(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            doOnConfirm(newName, HashMap(gradingDimsCopy))
                            hideDialog()
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "Confirm")

                    }
                }
            }
        }

    }


}

@Composable
fun EditGradingDim(name: String, defaultGrade: Int, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        backgroundColor = MyGray,
        shape = RoundedCornerShape(50),
        contentColor = Color.White
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Text(
                text = name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "" + defaultGrade)
                Spacer(modifier = Modifier.padding(8.dp))
                IconButton(onClick = { onDelete(name) }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
                }
            }

        }
    }

}

@Composable
fun ExitDialog(hideDialog: () -> Unit, doOnConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = { hideDialog() },
        confirmButton = {
            Button(onClick = {
                hideDialog()
                doOnConfirm()
            }) {
                Text(text = "exit anyway")
            }
        },
        dismissButton = {
            Button(onClick = { hideDialog() }) {
                Text(text = "close")
            }
        },
        title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Confirm Exit", style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Unsaved changes will be lost. Please press the save button if you wish to persist any changes")
            }

        }
    )
}