package de.hsfl.kevinblaue.musicrun.repositories

import android.app.Application
import android.os.Environment
import com.opencsv.CSVWriter
import de.hsfl.kevinblaue.musicrun.models.AppDatabase
import de.hsfl.kevinblaue.musicrun.models.Statistic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileWriter
import java.io.IOException

class StatisticsRepository(application: Application) {
    var database: AppDatabase? = null

    suspend fun insertStatistic(statistic: Statistic) {
        withContext(Dispatchers.IO) {
            database?.statisticDao()?.insertStatistic(statistic)
        }
    }

    suspend fun loadStatistics(): List<Statistic>? {
        return withContext(Dispatchers.IO) {
            database?.statisticDao()?.statistics
        }
    }

    // Creates a csv file with the current data in Download directory
    suspend fun writeStatisticsToCSV() {
        withContext(Dispatchers.IO) {
            val statistics = loadStatistics()
            try {
                val writer = CSVWriter(FileWriter(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ).absolutePath + "/statistics.csv"
                ))

                val data = ArrayList<Array<String>>()

                data.add(
                    arrayOf("id", "uuid", "exceeds", "timeInRange", "timeOutOfRange")
                )

                statistics?.forEach { statistic ->
                    data.add(
                        arrayOf(
                            statistic.id.toString(),
                            statistic.uuid.toString(),
                            statistic.exceeds.toString(),
                            statistic.timeInRange.toString(),
                            statistic.timeOutOfRange.toString()
                        )
                    )
                }
                writer.writeAll(data)
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    init {
        database = AppDatabase.getDatabase(application)
    }
}