package de.hsfl.kevinblaue.musicrun.viewmodels

import android.app.Application
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
    private var timeInRange: Long = 0
    private var timeOutOfRange: Long = 0
    var latestTimeStamp: Long = 0
    private var isInRange = false
    val isUnderRange: MutableLiveData<Boolean> = MutableLiveData(false)
    val isAboveRange: MutableLiveData<Boolean> = MutableLiveData(false)
    val supportType: MutableLiveData<Int> = MutableLiveData(-1)
    private var trainingStarted = false

    val description: MutableLiveData<String> = MutableLiveData("")

    // ToDo: Je nachdem was der Gürtel rausgibt
    val heartBeat: MediatorLiveData<String?> = MediatorLiveData<String?>().apply {
        value = if (this.value == null) {
            "0"
        } else {
            handleRangeData(this.value?.toInt()!!)
            this.value
        }
    }

    fun setRangeEntry (entry: RangeEntry) {
        description.value = entry.description
        rangeFrom = entry.rangeFrom
        rangeTo = entry.rangeTo
    }

    suspend fun saveStatistics() {
        statisticsRepository?.insertStatistic(
            Statistic(
                0,
                uuid,
                exceeds,
                timeInRange,
                timeOutOfRange
            )
        )
    }

    suspend fun createCSV() {
        statisticsRepository?.writeStatisticsToCSV()
    }

    // Logic for statistics
    private fun handleRangeData(value: Int) {
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
        } else if (value > rangeFrom || value < rangeTo) {
            handleOutOfRange()
        }
    }

    private fun handleOutOfRange() {
        isInRange = true
        isUnderRange.value = false
        isAboveRange.value = false

        val currentTimeStamp = Timestamp(System.currentTimeMillis()).time
        timeOutOfRange += (latestTimeStamp - currentTimeStamp)
        latestTimeStamp = currentTimeStamp
    }

    private fun handleInRange() {
        isInRange = false
        exceeds++

        val currentTimeStamp = Timestamp(System.currentTimeMillis()).time
        timeInRange += (latestTimeStamp - currentTimeStamp)
        latestTimeStamp = currentTimeStamp
    }

    fun startTraining() {
        latestTimeStamp = Timestamp(System.currentTimeMillis()).time
        trainingStarted = true
    }

    fun setRepository(application: Application) {
        statisticsRepository = StatisticsRepository(application)
    }
}