package de.hsfl.kevinblaue.musicrun.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hsfl.kevinblaue.musicrun.models.RangeEntry
import de.hsfl.kevinblaue.musicrun.models.Statistic
import de.hsfl.kevinblaue.musicrun.repositories.StatisticsRepository
import java.sql.Timestamp
import java.util.*

class ActivityViewModel : ViewModel() {
    private var statisticsRepository: StatisticsRepository? = null
    private val uuid: UUID = UUID.randomUUID()
    private var rangeFrom = 0
    private var rangeTo = 0
    private var exceeds = 0
    var sumInRange: Long = 0
    var sumOutOfRange: Long = 0
    var latestTimeStamp: Long = 0
    var isInRange = false
    val isUnderRange: MutableLiveData<Boolean> = MutableLiveData(false)
    val isAboveRange: MutableLiveData<Boolean> = MutableLiveData(false)
    val supportType: MutableLiveData<Int> = MutableLiveData(-1)
    private var trainingStarted = false

    val description: MutableLiveData<String> = MutableLiveData("")

    val heartRate: MediatorLiveData<Int?> = MediatorLiveData<Int?>()

    fun setRangeEntry (entry: RangeEntry) {
        description.value = entry.description
        rangeFrom = entry.rangeFrom
        rangeTo = entry.rangeTo
    }

    private suspend fun saveStatistics() {
        statisticsRepository?.insertStatistic(
            Statistic(
                0,
                uuid,
                supportType.value!!,
                exceeds,
                sumInRange,
                sumOutOfRange
            )
        )
    }

    fun startTraining() {
        latestTimeStamp = Timestamp(System.currentTimeMillis()).time
        trainingStarted = true

        val currHeartRate = heartRate.value
        if (currHeartRate!! < rangeFrom) {
            isUnderRange.value = true
        } else if (currHeartRate > rangeTo) {
            isAboveRange.value = true
        } else {
            isInRange = true
        }
    }

    suspend fun stopTraining() {
        // Write data to Database
        saveStatistics()

        // Set all statistics back to 0
        resetValues()
    }

    fun setRepository(application: Application) {
        statisticsRepository = StatisticsRepository(application)
    }

    private fun resetValues() {
        isUnderRange.value = false
        isAboveRange.value = false
        isInRange = false
        sumInRange = 0
        sumOutOfRange = 0
        latestTimeStamp = 0
        trainingStarted = false
        exceeds = 0
        rangeFrom = 0
        rangeTo = 0
    }

    suspend fun createCSV() {
        statisticsRepository?.writeStatisticsToCSV()
    }

    // Logic for statistics
    fun handleRangeData(value: Int) {
        // Look if the heartbeat is in range
        if (!trainingStarted){
            return
        }
        if (isInRange) {
            if (value < rangeFrom) {
                handleInRange()
                isUnderRange.value = true
            } else if (value > rangeTo) {
                handleInRange()
                isAboveRange.value = true
            }
        } else if (
            (value > rangeFrom && isUnderRange.value == true)
            ||
            (value < rangeTo &&  isAboveRange.value == true)
        ) {
            handleOutOfRange()
        }
    }

    private fun handleOutOfRange() {
        isInRange = true
        isUnderRange.value = false
        isAboveRange.value = false
    }

    private fun handleInRange() {
        isInRange = false
        exceeds++
    }
}