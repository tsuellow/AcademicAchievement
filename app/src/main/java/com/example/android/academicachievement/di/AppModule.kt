package com.example.android.academicachievement.di


import com.example.android.academicachievement.Constants
import com.example.android.academicachievement.data.remote.FirebaseStorageConnection
import com.example.android.academicachievement.data.repository.CourseRepositoryImpl
import com.example.android.academicachievement.data.repository.ImageRepositoryImpl
import com.example.android.academicachievement.data.response.FirebaseDatabaseConnection
import com.example.android.academicachievement.domain.repository.CourseRepository
import com.example.android.academicachievement.domain.repository.ImageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseConn():FirebaseDatabaseConnection{
        return FirebaseDatabaseConnection(Constants.URL_FIREBASE)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorageConn(): FirebaseStorageConnection {
        return FirebaseStorageConnection(Constants.URL_STORAGE)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(firebaseDatabaseConnection: FirebaseDatabaseConnection): CourseRepository {
        return CourseRepositoryImpl(firebaseDatabaseConnection)
    }

    @Provides
    @Singleton
    fun provideImageRepository(firebaseStorageConnection: FirebaseStorageConnection): ImageRepository {
        return ImageRepositoryImpl(firebaseStorageConnection)
    }
}