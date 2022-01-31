package com.example.android.academicachievement.presentation.ui.composables


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.academicachievement.Constants.URL_FIREBASE
import com.example.android.academicachievement.common.Screen
import com.example.android.academicachievement.data.response.FirebaseConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@Composable
fun MainScreen(navController: NavController
){
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(8.dp)
    ) {

        Text(text = "Hello World",
            fontStyle = FontStyle.Italic,
            color = Color.Blue
        )

        Spacer(modifier = Modifier.padding(16.dp))

        Button(onClick = { navController.navigate(Screen.Courses.route)
            //Toast.makeText(context,"test", Toast.LENGTH_LONG).show()


        }) {
            Text(text = "Enroll Student")
        }

        Spacer(modifier = Modifier.padding(16.dp))

        Button(onClick = {
            //Toast.makeText(context,"test", Toast.LENGTH_LONG).show()
            navController.navigate(Screen.Scanner.route)
        }) {
            Text(text = "Validate Milestone")
        }

        Spacer(modifier = Modifier.padding(16.dp))

        Button(onClick = {
            //Toast.makeText(context,"test", Toast.LENGTH_LONG).show()
            //navController.navigate(Screen.Scanner.route)
//            CoroutineScope(Dispatchers.IO).launch {
//                val repo=FirebaseConnection(URL_FIREBASE)
//                Log.d("testino",repo.getCourseTemplates().toString())
//            }
            navController.navigate(Screen.Test.route)

        }) {
            Text(text = "test")
        }

    }
}