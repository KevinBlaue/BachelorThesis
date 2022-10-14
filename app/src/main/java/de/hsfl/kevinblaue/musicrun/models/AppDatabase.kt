package de.hsfl.kevinblaue.musicrun.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Statistic::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun statisticDao(): StatisticDao

    companion object {
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "app.db"
                ).build()
            }
            return instance as AppDatabase
        }
    }
}