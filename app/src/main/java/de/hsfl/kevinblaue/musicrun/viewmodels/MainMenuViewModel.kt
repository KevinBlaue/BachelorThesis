package de.hsfl.kevinblaue.musicrun.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hsfl.kevinblaue.musicrun.repositories.StatisticsRepository

class MainMenuViewModel : ViewModel() {
    private var statisticsRepository: StatisticsRepository? = null
    val beltConnected = MutableLiveData(false)

    /**
     * Writes the data from statistics table of RoomDatabase to a CSV-File.
     */
    suspend fun createCSV() {
        statisticsRepository?.writeStatisticsToCSV()
    }

    /**
     * Sets a reference from the MainActivity repository
     */
    fun setRepository(repository: StatisticsRepository) {
        statisticsRepository = repository
    }
}