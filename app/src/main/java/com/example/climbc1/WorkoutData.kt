package com.example.climbc1

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WorkoutData(
    val time: Long,
    val forceReading: Int,
    val workoutID: Int,
    val finger: Int, //0 pointer, 1 middle, 2, ring, 3 pinky
    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,
)
