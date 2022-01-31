package com.example.android.academicachievement.presentation.common_composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MyTopBar(
    title:String
){
    TopAppBar(elevation = 8.dp) {
        Text(text = title, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
    }
}