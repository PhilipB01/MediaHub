package com.example.medialist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.medialist.data.local.dao.MediaDao
import com.example.medialist.data.local.entity.MediaEntity

@Database(entities = [MediaEntity::class], version = 4, exportSchema = false)
abstract class MediaDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
}
