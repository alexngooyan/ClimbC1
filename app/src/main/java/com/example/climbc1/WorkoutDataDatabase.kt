package com.example.climbc1

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutData::class],
    version = 1
)
abstract class WorkoutDataDatabase: RoomDatabase() {

    abstract val dao: WorkoutDataDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDataDatabase? = null

        fun getDatabase(context: Context): WorkoutDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDataDatabase::class.java,
                    "workoutdatas.db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}