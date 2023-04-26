package com.example.android.academicachievement.data.remote

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseStorageConnection(url:String) {
    private val storage= Firebase.storage

    private val imagesRef=storage.getReferenceFromUrl(url).child("studentImages")

    suspend fun uploadStudentPhoto(studentId:String,photoFile:File):Uri?{
       return imagesRef.uploadFile("$studentId.jpg",photoFile)
    }

    private suspend fun StorageReference.uploadFile(name:String, file: File):Uri?= suspendCoroutine { continuation ->
        val fileRef=this.child(name)
        val uploadTask=fileRef.putFile(Uri.fromFile(file))
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            fileRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                continuation.resume(downloadUri)
            } else {
                continuation.resume(null)
            }
        }
    }
}