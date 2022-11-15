package com.ikhokha.techcheck.dagger

import android.app.Application
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ikhokha.techcheck.data.databases.UserDatabase
import com.ikhokha.techcheck.data.databases.UserDatabase.Companion.dbName
import com.ikhokha.techcheck.utils.ConnectionLiveData
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
    fun provideNetworkReport(app: Application): ConnectionLiveData = ConnectionLiveData(app)

    @Provides
    @Singleton
    fun provideFirebaseDatabase() = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFireAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage() = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun providesDatabase(app: Application) =
        Room.databaseBuilder(app, UserDatabase::class.java, dbName)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideProductDao(db: UserDatabase) = db.productDao()
}