package de.hsfl.kevinblaue.musicrun.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hsfl.kevinblaue.musicrun.models.RangeEntry

class ChooseViewModel : ViewModel() {
    private val valueRanges: List<RangeEntry> = listOf(
        /*RangeEntry(
            description = "90 bis 100 bpm",
            rangeFrom = 90,
            rangeTo = 100
        ),*/
        RangeEntry(
            description = "120 bis 129 bpm",
            rangeFrom = 120,
            rangeTo = 129
        ),
        RangeEntry(
            description = "130 bis 139 bpm",
            rangeFrom = 130,
            rangeTo = 139
        ),
        RangeEntry(
            description = "140 bis 149 bpm",
            rangeFrom = 140,
            rangeTo = 149
        ),
        RangeEntry(
            description = "150 bis 159 bpm",
            rangeFrom = 150,
            rangeTo = 159
        ),
        RangeEntry(
            description = "160 bis 169 bpm",
            rangeFrom = 160,
            rangeTo = 169
        )
    )
    val list: MutableLiveData<List<RangeEntry>> = MutableLiveData(valueRanges)
}