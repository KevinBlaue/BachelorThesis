package de.hsfl.kevinblaue.musicrun.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "statistics"
)
data class Statistic(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "uuid") val uuid: UUID,
    @ColumnInfo(name = "support_type") val supportType: Int,
    @ColumnInfo(name = "exceeds") val exceeds: Int,
    @ColumnInfo(name = "time_in_range") val timeInRange: Long,
    @ColumnInfo(name = "time_out_of_range") val timeOutOfRange: Long
)