package com.example.android.academicachievement.presentation.common_composables

import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import kotlinx.coroutines.coroutineScope

fun Modifier.cancelOnExpandedChangeIfScrolling() = pointerInput(Unit) {
    forEachGesture {
        coroutineScope {
            awaitPointerEventScope {
                var event: PointerEvent
                var startPosition = Offset.Unspecified
                var cancel = false

                do {
                    event = awaitPointerEvent(PointerEventPass.Initial)
                    if (startPosition == Offset.Unspecified) {
                        startPosition = event.changes.first().position
                    }

                    val distance = startPosition.minus(event.changes.last().position).getDistance()
                    cancel = distance > 10f || cancel
                } while (
                    !event.changes.all { it.changedToUp() }
                )

                if (cancel) {
                    event.changes.forEach { it.consumeAllChanges() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DropdownTextField(
    enabled: Boolean,
    label: String,
    options: List<String> = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5"),
    value: String,
    onValueChange: (String) -> Unit,
    error: String = ""
) {


    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = Modifier.cancelOnExpandedChangeIfScrolling()
    ) {
        OutlinedTextFieldValidation(
            readOnly = true,
            value = value,
            enabled = enabled,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded && enabled
                )
            },
            //colors = ExposedDropdownMenuDefaults.textFieldColors(),
            error = error
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    }
                ) {
                    Text(text = selectionOption)
                }
            }
        }
    }
}