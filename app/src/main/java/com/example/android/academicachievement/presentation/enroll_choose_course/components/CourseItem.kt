package com.example.android.academicachievement.presentation.enroll_choose_course.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.android.academicachievement.data.remote.dto.CourseDto
import com.example.android.academicachievement.data.remote.dto.CourseTemplateDto

@Composable
fun CourseItem(
    course: CourseDto,
    onItemClicked: (CourseDto)->Unit
){

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onItemClicked(course) },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically) {
        
        Text(modifier =Modifier
            .padding(16.dp),
            text = course.key+".",
            style = MaterialTheme.typography.h5)

        Text(text = course.name,
            style = MaterialTheme.typography.body1,
            overflow = TextOverflow.Ellipsis)

    }
}