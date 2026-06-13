package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserProfile::class,
        Prediction::class,
        Post::class,
        Comment::class,
        MatchEntity::class,
        Transaction::class,
        Report::class,
        ApiSyncState::class,
        FavoriteTeam::class,
        FavoriteLeague::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(FanArenaConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fanarena_master_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
