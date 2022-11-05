package de.hsfl.kevinblaue.musicrun.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StatisticDao {
    @get:Query("SELECT * FROM statistics")
    val statistics: List<Statistic>

    @Insert
    fun insertStatistic(statistic: Statistic)
}