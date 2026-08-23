package com.discflux.medialist.di

import android.content.Context
import androidx.room.Room
import com.discflux.medialist.data.local.MediaDatabase
import com.discflux.medialist.data.local.dao.MediaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMediaDatabase(
        @ApplicationContext context: Context
    ): MediaDatabase {
        return Room.databaseBuilder(
            context,
            MediaDatabase::class.java,
            "media_hub.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideMediaDao(database: MediaDatabase): MediaDao {
        return database.mediaDao()
    }
}
