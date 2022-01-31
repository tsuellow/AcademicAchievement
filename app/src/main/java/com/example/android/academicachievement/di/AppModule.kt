package com.example.android.academicachievement.di


import com.example.android.academicachievement.Constants
import com.example.android.academicachievement.data.repository.CourseRepositoryImpl
import com.example.android.academicachievement.data.response.FirebaseConnection
import com.example.android.academicachievement.domain.repository.CourseRepository
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
    fun provideFirebaseConn():FirebaseConnection{
        return FirebaseConnection(Constants.URL_FIREBASE)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(firebaseConnection: FirebaseConnection): CourseRepository {
        return CourseRepositoryImpl(firebaseConnection)
    }

}