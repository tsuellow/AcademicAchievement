package com.example.android.academicachievement.presentation.common_composables

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.android.academicachievement.Constants.idName
import com.example.android.academicachievement.presentation.enroll_scan.ScannerState

import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import org.json.JSONException
import org.json.JSONObject


@ExperimentalComposeUiApi
@Composable
fun ScannerScreen(
    state:State<ScannerState>,
    onScanIdOnly:(Int)->Unit,
    onScanMilestone:(Int,String)->Unit) {

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
                            try {
                                val json=JSONObject(barCodeOrQr)
                                val id=json.getInt(idName)
                                val obj=json.getString("obj")
                                if (obj.contentEquals("l")){
                                    onScanIdOnly(id)
                                }else if(obj.contentEquals("v")){
                                    val path=json.getString("path")
                                    onScanMilestone(id,path)
                                } //here inflate a dialog depending on the application of this screen
                            }catch (e: JSONException){
                                Log.d("scanError", "could not read")
                            }
                            lastResult = barCodeOrQr
                            lastRead = System.currentTimeMillis()
                        }
                    }
                }
            }
            //this.resume()
        }
    }

    DisposableEffect(key1 = "someKey" ){
        compoundBarcodeView.resume()
        onDispose {
            compoundBarcodeView.pause()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()) {

        MyTopBar(title = state.value.title)

        Column(
            modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = state.value.subTitle,
                style = MaterialTheme.typography.h6,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.secondary
                )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.weight(3f),
                shape = RoundedCornerShape(5)
            ) {
                AndroidView(
                    modifier = Modifier,
                    factory = { compoundBarcodeView },
                )
            }

            Column(modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .defaultMinSize(minHeight = 150.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top) {

                val focusManager = LocalFocusManager.current

                OutlinedTextField(value = manualText,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    onValueChange = { typed:String -> manualText=typed.filter { it.isDigit() } },
                    label = {Text("Enter ID")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {
                        keyboardController?.hide()
                        manualText.toInt().let(onScanIdOnly)
                        manualText=""
                        focusManager.clearFocus()
                    },
                    enabled=manualText.isNotEmpty(),
                    modifier = Modifier.height(50.dp).fillMaxWidth()) {
                    Text(text = "Search")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}




