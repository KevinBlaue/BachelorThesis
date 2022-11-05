package de.hsfl.kevinblaue.musicrun.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hsfl.kevinblaue.musicrun.models.RangeEntry
import de.hsfl.kevinblaue.musicrun.models.Statistic
import de.hsfl.kevinblaue.musicrun.repositories.StatisticsRepository
import java.util.*

class ActivityViewModel : ViewModel() {
    private val uuid: UUID = UUID.randomUUID()
    private var exceeds = 0
    private var isInRange = false
    private var rangeFrom = 0
    private var rangeTo = 0
    private var statisticsRepository: StatisticsRepository? = null
    private var timeInRange: Long = 0
    private var timeOutOfRange: Long = 0
    private var trainingStarted = false
    val description: MutableLiveData<String> = MutableLiveData("")
    val heartRate: MediatorLiveData<Int?> = MediatorLiveData<Int?>()
    val isUnderRange: MutableLiveData<Boolean> = MutableLiveData(false)
    val isAboveRange: MutableLiveData<Boolean> = MutableLiveData(false)
    val supportType: MutableLiveData<Int> = MutableLiveData(-1)

    /**
     * Increments the counter [timeInRange] by 1
     */
    private fun incrementTimeInRange() {
        timeInRange += 1
    }

    /**
     * Increments the counter [timeOutOfRange] by 1
     */
    private fun incrementTimeOutOfRange() {
        timeOutOfRange += 1
    }

    /**
     * Stores the statistical data from the training into the Statistics model of the database
     */
    private suspend fun saveStatistics() {
        statisticsRepository?.insertStatistic(
            Statistic(
                0,
                uuid,
                supportType.value!!,
                exceeds,
                timeInRange,
                timeOutOfRange
            )
        )
    }

    /**
     * Resets all values for another training
     */
    private fun resetValues() {
        isUnderRange.value = false
        isAboveRange.value = false
        description.value = ""
        isInRange = false
        timeInRange = 0
        timeOutOfRange = 0
        trainingStarted = false
        exceeds = 0
        rangeFrom = 0
        rangeTo = 0
    }

    /**
     * Handles the case when a person gets in the chosen range
     */
    private fun handleInRange() {
        isInRange = true
        isUnderRange.value = false
        isAboveRange.value = false
    }

    /**
     * Handles the case when a person gets out of the chosen range
     */
    private fun handleOutOfRange() {
        isInRange = false
        exceeds++
    }

    /**
     * Increases the in and out of range times every second in dependence of [isInRange]
     */
    fun handleOnTick() {
        if (isInRange) {
            incrementTimeInRange()
        } else {
            incrementTimeOutOfRange()
        }
    }

    /**
     * Handler for the logic during the training.
     * @param heartRate The heart-rate from the bluetooth belt
     */
    fun handleHeartRate(heartRate: Int) {
        if (!trainingStarted){
            return
        }
        if (isInRange) {
            if (heartRate < rangeFrom) {
                handleOutOfRange()
                isUnderRange.value = true
            } else if (heartRate > rangeTo) {
                handleOutOfRange()
                isAboveRange.value = true
            }
        } else if (
            (heartRate > rangeFrom && isUnderRange.value == true)
            ||
            (heartRate < rangeTo &&  isAboveRange.value == true)
        ) {
            handleInRange()
        }
    }

    /**
     * Sets the range data for a training
     * @param entry The range entry from the ChooseViewModel
     */
    fun setRangeEntry (entry: RangeEntry) {
        description.value = entry.description
        rangeFrom = entry.rangeFrom
        rangeTo = entry.rangeTo
    }

    /**
     * Sets all mandatory values so that the logic for the training can start working.
     * This method also sets the boolean value for the handler logic whether the persons heart-beat
     * is in the chosen range or not when he starts.
     */
    fun startTraining() {
        trainingStarted = true
        heartRate.value?.let { currentHeartRate ->
            if (currentHeartRate < rangeFrom) {
                isUnderRange.value = true
            } else if (currentHeartRate > rangeTo) {
                isAboveRange.value = true
            } else {
                isInRange = true
            }
        }
    }

    /**
     * Stops the current training. Has to be suspend because the data of the training is going to be
     * saved here @[saveStatistics]. This method also calls the [resetValues] method
     */
    suspend fun stopTraining() {
        saveStatistics()
        resetValues()
    }

    /**
     * Sets a reference from the MainActivity repository
     */
    fun setRepository(repository: StatisticsRepository) {
        statisticsRepository = repository
    }
}