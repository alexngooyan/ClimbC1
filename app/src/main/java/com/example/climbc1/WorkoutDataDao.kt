package com.example.climbc1

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
//import androidx.room.vo.Dao
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDataDao {

    // Update or insert a tuple. Conflicting data will update / replace
    // by default.
    @Upsert
    suspend fun upsertTuple(workoutData: WorkoutData)

    @Delete
    suspend fun deleteTuple(workoutData: WorkoutData)

    @Query("SELECT * FROM WorkoutData AS wd WHERE wd.workoutID = :idParam AND wd.finger = :finger ORDER BY wd.time")
    suspend fun getWorkoutByIDAndFingOrderByTime(idParam: Int, finger: Int): List<WorkoutData>

    @Query("SELECT * FROM WorkoutData ORDER BY time")
    suspend fun getAllData(): List<WorkoutData>

    //added by Alex to get maxes for display on the home page (if these dont work, delete and try again)

    @Query("SELECT MAX(forceReading) FROM WorkoutData WHERE finger = :finger")
    suspend fun getMaxFRFromFinger(finger: Int): Int?

    //DANGEROUS! DESTRUCTIVE, USE W CAUTION
    @Query("DELETE FROM WorkoutData")
    suspend fun resetWorkoutTable()
}