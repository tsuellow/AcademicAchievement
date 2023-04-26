package com.example.android.academicachievement.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.android.academicachievement.Constants
import com.example.android.academicachievement.R
import com.example.android.academicachievement.domain.model.StudentState
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun generateQrCode(context: Context, jsonText: String): Bitmap? {
        return try {
            //setting size of qr code
            val width = 900
            val height = 900
            val smallestDimension = Math.min(width, height)
            //setting parameters for qr code
            val charset = "UTF-8"
            val hintMap: MutableMap<EncodeHintType, ErrorCorrectionLevel> = HashMap()
            hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            createQRCode(
                jsonText,
                charset,
                hintMap,
                smallestDimension,
                smallestDimension,
                context
            )
        } catch (ex: Exception) {
            Log.e("QrGenerate", ex.message!!)
            null
        }
    }

    private fun createQRCode(
        qrCodeData: String,
        charset: String,
        hintMap: Map<EncodeHintType, *>,
        qrCodeheight: Int,
        qrCodewidth: Int,
        context: Context
    ): Bitmap? {
        return try {
            //generating qr code in bitmatrix type
            val matrix = MultiFormatWriter().encode(
                String(qrCodeData.toByteArray(charset(charset)), charset(charset)),
                BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap
            )
            //converting bitmatrix to bitmap
            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height)
            // All are 0, or black, by default
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
                    //pixels[offset + x] = matrix.get(x, y) ? ResourcesCompat.getColor(getResources(),R.color.colorB,null) :WHITE;
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            //setting bitmap to image view
            val overlayBig = BitmapFactory.decodeResource(context.resources, R.drawable.qr_image_2)
            val overlay =
                Bitmap.createScaledBitmap(overlayBig, Constants.qrDims, Constants.qrDims, false)
            mergeBitmaps(overlay, bitmap)
        } catch (er: Exception) {
            Log.e("QrGenerate", er.message!!)
            null
        }
    }

    private fun mergeBitmaps(overlay: Bitmap, bitmap: Bitmap): Bitmap {
        val height = bitmap.height
        val width = bitmap.width
        val combined = Bitmap.createBitmap(width, height, bitmap.config)
        val canvas = Canvas(combined)
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        canvas.drawBitmap(bitmap, Matrix(), null)
        val centreX = (canvasWidth - overlay.width) / 2
        val centreY = (canvasHeight - overlay.height) / 2
        canvas.drawBitmap(overlay, centreX.toFloat(), centreY.toFloat(), null)
        return combined
    }


    fun createQrCodeFile(clientId: Int, context: Context): File {
        // Create an image file name
        val imageFileName = "QR_CODE_$clientId"
        val storageDir =
            context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES).firstOrNull().let {
                File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
        // Save a file: path for use with ACTION_VIEW intents
        return File(storageDir, "$imageFileName.jpg")
    }

    fun saveQrCode(id: Int, context: Context) {
        try {
            val qrFile = createQrCodeFile(id, context)
            if (qrFile.exists()) {
                qrFile.delete()
            }
            val qrOut = FileOutputStream(qrFile)
            val qrText ="{\"obj\":\"l\",\"aaid\":$id}"
            val qrCode = generateQrCode(context, qrText)
            qrCode!!.compress(Bitmap.CompressFormat.JPEG, 100, qrOut)
            qrOut.flush()
            qrOut.close()
            Log.d("QR saved", "QR ok")
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.d("QR saved", "QR IOException")
        }
    }

    fun displayQrDialog(context: Context, student: StudentState): AlertDialog {
        saveQrCode(student.key.value.drop(1).toInt(), context)
        val mBuilder = AlertDialog.Builder(context)
        mBuilder.setTitle("Send QR code")
            .setMessage("Do you want to sent the QR key to " + student.firstName.value)
            .setNegativeButton(
                "cancel"
            ) { dialog, which -> dialog.dismiss() }
            .setPositiveButton(
                "send"
            ) { dialog, which ->
                shareFileOnWhatsApp(
                    student,
                    createQrCodeFile(student.key.value.drop(1).toInt(), context),
                    context
                )
                dialog.dismiss()
            }
        return mBuilder.create()
    }

    fun shareFileOnWhatsApp(student: StudentState, file: File, context: Context) {
        if (student.phone.value.isEmpty()) return
        var toNumber: String = depuratePhone(student.phone.value)
        toNumber = toNumber.replaceFirst("^0+(?!$)".toRegex(), "")
        val qrUri: Uri = FileProvider.getUriForFile(context,context.applicationContext.packageName+ ".provider",file).normalizeScheme()
        val tmpList: ArrayList<Uri> = ArrayList()
        tmpList.add(qrUri)
        val sendIntent = Intent(Intent.ACTION_SEND)

        //sendIntent.setComponent( ComponentName("com.whatsapp","com.whatsapp.ContactPicker"));
        sendIntent.setPackage("com.whatsapp")
        sendIntent.type = "image/jpeg"
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        sendIntent.putExtra(Intent.EXTRA_STREAM, qrUri)
        sendIntent.putExtra("jid", "$toNumber@s.whatsapp.net")
        sendIntent.putExtra(
            Intent.EXTRA_TEXT, "Hi "+student.firstName.value+", Alianza Americana warmly welcomes you!\n" +
                    "From now on you can use this QR code to enter our facilities and follow our courses.\n" +
                    "The following credentials will grant you access to our online portal. \n\n" +
                    "   ID:        *"+student.key.value+"* \n" +
                    "   Password:  *"+student.pin.value+"*"
        )
        context.startActivity(sendIntent)
    }
