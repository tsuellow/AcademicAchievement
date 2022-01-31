package com.example.android.academicachievement.presentation.ui.composables

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.android.academicachievement.Constants.idName
import com.example.android.academicachievement.presentation.common_composables.MyTopBar
import com.example.android.academicachievement.presentation.enroll_scan.ScannerState

import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.min



@ExperimentalComposeUiApi
@Composable
fun ScannerScreen(
    state:State<ScannerState>,
    onScan:(Int)->Unit) {

    val context = LocalContext.current
    var lastResult by remember { mutableStateOf("")}
    var lastRead by remember { mutableStateOf(System.currentTimeMillis())}
    var manualText by remember { mutableStateOf("")}

    val keyboardController = LocalSoftwareKeyboardController.current


    val compoundBarcodeView = remember {
        CompoundBarcodeView(context).apply {
            val capture = CaptureManager(context as Activity, this)
            capture.initializeFromIntent(context.intent, null)
            this.setStatusText("")
            capture.decode()
            this.decodeContinuous { result ->
                if(!state.value.pauseScan) {
                    result.text?.let { barCodeOrQr ->
                        if (lastResult != barCodeOrQr || System.currentTimeMillis() - lastRead > 2000) {
                            id = getIdz(barCodeOrQr)
                            onScan(id)//here inflate a dialog depending on the application of this screen
                            lastResult = barCodeOrQr
                            lastRead = System.currentTimeMillis()
                        }
                    }
                }
            }
            this.resume()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()) {

        MyTopBar(title = state.value.title)

        Column() {
            Text(text = state.value.subTitle,
                style = MaterialTheme.typography.h6,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp))

            AndroidView(
                modifier = Modifier.weight(3f),
                factory = { compoundBarcodeView },
            )

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .defaultMinSize(minHeight = 150.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top) {

                OutlinedTextField(value = manualText,
                    onValueChange = { it:String -> manualText=it },
                    label = {Text("Enter ID")},
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = { manualText.toInt().let(onScan)
                    keyboardController?.hide()},
                    modifier = Modifier.height(50.dp)) {
                    Text(text = "Search")
                }
        }
        }
    }
}

fun getIdz(qr:String):Int {
    return try {
        val json=JSONObject(qr)
        json.getInt(idName)
    }catch (e: JSONException){
        0
    }
}



