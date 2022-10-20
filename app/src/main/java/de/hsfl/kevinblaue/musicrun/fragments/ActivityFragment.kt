package de.hsfl.kevinblaue.musicrun.fragments

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import de.hsfl.kevinblaue.musicrun.R
import de.hsfl.kevinblaue.musicrun.databinding.FragmentActivityBinding
import de.hsfl.kevinblaue.musicrun.models.RangeEntry
import de.hsfl.kevinblaue.musicrun.viewmodels.ActivityViewModel
import kotlinx.coroutines.launch

class ActivityFragment : Fragment() {
    private val viewModel: ActivityViewModel by activityViewModels()
    private var binding: FragmentActivityBinding? = null
    private var mediaPlayer: MediaPlayer? = null
    private val pitch: PlaybackParams = PlaybackParams()
    private var currentSongId: Int = 0
    private val songs: Array<Int> = arrayOf(
        R.raw.hip_hop_rock_beats_118000,
        R.raw.bounce_114024,
        R.raw.space_120280,
        R.raw.electronic_future_beats_117997,
        R.raw.powerful_energetic_sport_rock_trailer_122077,
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivityBinding.inflate(inflater, container, false)

        // Set heartbeat animation
        val heart = binding?.heart
        val anim = AnimationUtils.loadAnimation(context, R.anim.beat)
        heart?.startAnimation(anim)

        // Shuffle the songs
        songs.shuffle()

        // Bind onlick listeners
        binding?.btnTraining?.setOnClickListener {
            lifecycleScope.launch {
                btnClickTraining()
            }
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.description.observe(viewLifecycleOwner) {
            binding?.rangeText?.text = it
        }
        viewModel.heartRate.observe(viewLifecycleOwner) { heartRate ->
            binding?.puls?.text = heartRate.toString()
            if (heartRate != null) {
                viewModel.handleRangeData(heartRate)
            }
        }
        viewModel.isUnderRange.observe(viewLifecycleOwner) { isUnderRange ->
            if (isUnderRange && viewModel.supportType.value == 1) {
                pitchMusicDown()
            } else {
                normalizePitch()
            }
        }
        viewModel.isAboveRange.observe(viewLifecycleOwner) { isAboveRange ->
            if (isAboveRange && viewModel.supportType.value == 1) {
                pitchMusicUp()
            } else {
                normalizePitch()
            }
        }

        // Set first song
       setMusic()
    }

    override fun onStop() {
        super.onStop()
        stopMusic()
        viewModel.setRangeEntry(
            RangeEntry(
                description = "",
                rangeFrom = 0,
                rangeTo = 0
            ))
    }

    private fun setMusic(increment: Int = 0) {
        stopMusic()
        currentSongId += increment
        if (currentSongId < songs.size) {
            mediaPlayer = MediaPlayer.create(context, songs[currentSongId])

            // Play next song if song is completed or release media player
            mediaPlayer?.setOnCompletionListener {
                setMusic(1)
                playMusic()
            }
        }
    }

    private suspend fun btnClickTraining() {
        if (binding?.btnTraining?.text!! == getString(R.string.play)) {
            playMusic()
        } else {
            stopTraining()
        }
    }

    private fun playMusic() {
        viewModel.startTraining()
        mediaPlayer?.start()
        binding?.btnTraining?.text = getString(R.string.endTraining)
    }

    private fun stopMusic () {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private suspend fun stopTraining() {
        // Stop and release Mediaplayer
        stopMusic()

        // Write data to Database
        viewModel.saveStatistics()

        // Set all statistics back to 0
        viewModel.resetValues()

        // Go back to MainMenu
        parentFragmentManager.commit {
            replace<MainMenuFragment>(R.id.fragment_container_view, "MAIN_MENU")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private fun pitchMusicUp() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.playbackParams = pitch.setPitch(1.25f)
        }
    }

    private fun pitchMusicDown() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.playbackParams = pitch.setPitch(0.75f)
        }
    }

    private fun normalizePitch() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.playbackParams = pitch.setPitch(1f)
        }
    }
}

/**
 * Music sources:
 * 1.
 * Music by <a href="https://pixabay.com/de/users/qubesounds-24397640/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=118000">QubeSounds</a> from <a href="https://pixabay.com/music//?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=118000">Pixabay</a>
 * 2.
 * Music by <a href="https://pixabay.com/de/users/coma-media-24399569/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=114024">Coma-Media</a> from <a href="https://pixabay.com/music//?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=114024">Pixabay</a>
 * 3.
 * Music by <a href="https://pixabay.com/de/users/music_unlimited-27600023/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=120280">Music_Unlimited</a> from <a href="https://pixabay.com/music//?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=120280">Pixabay</a>
 * 4.
 * Music by <a href="https://pixabay.com/de/users/qubesounds-24397640/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=117997">QubeSounds</a> from <a href="https://pixabay.com//?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=117997">Pixabay</a>
 * 5.
 * Music by <a href="https://pixabay.com/de/users/qubesounds-24397640/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=122077">QubeSounds</a> from <a href="https://pixabay.com//?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=122077">Pixabay</a>
 */