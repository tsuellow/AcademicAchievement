package com.example.android.academicachievement.presentation.common_composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun EditTextDialog(title: String, label:String, value:String="", hideDialog: () -> Unit, doOnConfirm: (String) -> Unit) {

    val context = LocalContext.current
    var text by remember {
        mutableStateOf(value)
    }
    var error by remember {
        mutableStateOf("")
    }

    AlertDialog(onDismissRequest = { hideDialog() },
        confirmButton = {
            Button(onClick = {
                if (text.isNotEmpty()) {
                    doOnConfirm(text)
                    hideDialog()
                } else {
                    error = "cannot be empty"
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
        title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = title, style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextFieldValidation(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChange = {
                        text = it
                        error = ""
                    },
                    label = { Text(text = label) },
                    error = error
                )
            }
        }
    )
}