package com.example.android.academicachievement.data.repository

import com.example.android.academicachievement.data.remote.FirebaseStorageConnection
import com.example.android.academicachievement.domain.repository.ImageRepository
import java.io.File

class ImageRepositoryImpl(val firebaseStorageConnection: FirebaseStorageConnection):ImageRepository {

    override suspend fun setStudentPhoto(name: String, photoFile: File):String {
        val uri=firebaseStorageConnection.uploadStudentPhoto(name, photoFile)
        return if (uri != null) {
            uri.toString()?:""
        }else{
            ""
        }
    }

}