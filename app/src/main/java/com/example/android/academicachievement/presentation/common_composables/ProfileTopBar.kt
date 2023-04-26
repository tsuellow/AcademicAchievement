package com.example.android.academicachievement.presentation.common_composables

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.android.academicachievement.domain.model.Student

@Composable
fun ProfileTopBar(student: Student?){
    TopAppBar() {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
                Box(modifier =Modifier.padding(2.dp), contentAlignment = Alignment.Center ){
                    CircularProgressIndicator(modifier = Modifier
                            .size(44.dp)
                            .padding(6.dp),
                        color = Color.White)
                    Image(
                        painter = rememberImagePainter(student?.photoPath),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = student?.firstName+" "+student?.lastName+" ID: "+ (student?.key?.drop(1)
                    ?: ""),
                    style = MaterialTheme.typography.h6)
            }

            Icon(imageVector = Icons.Filled.MoreVert, contentDescription ="Menu", modifier = Modifier.padding(6.dp) )
        }



    }

}