package com.example.android.academicachievement.presentation.enroll_choose_course.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.academicachievement.presentation.enroll_choose_course.EnrollChooseCourseViewModel

@Composable
fun Test(
    viewModel: EnrollChooseCourseViewModel = hiltViewModel()
) {
    Text("Test")
}