package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        VideoEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        QueueItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TuneFlowDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun videoDao(): VideoDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun queueDao(): QueueDao

    companion object {
        @Volatile
        private var INSTANCE: TuneFlowDatabase? = null

        fun getDatabase(context: Context): TuneFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TuneFlowDatabase::class.java,
                    "tuneflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
