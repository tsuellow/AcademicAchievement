package com.example.android.academicachievement.domain.repository

import android.net.Uri
import java.io.File

interface ImageRepository {

    suspend fun setStudentPhoto(name:String, photoFile: File): String
}