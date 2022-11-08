package de.hsfl.kevinblaue.musicrun.fragments

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import de.hsfl.kevinblaue.musicrun.R
import de.hsfl.kevinblaue.musicrun.databinding.FragmentActivityBinding
import de.hsfl.kevinblaue.musicrun.viewmodels.ActivityViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

class ActivityFragment : Fragment() {
    private val pitch: PlaybackParams = PlaybackParams().setPitch(1f)
    private val songs: Array<Int> = arrayOf(
        R.raw.hip_hop_rock_beats_118000,
        R.raw.bounce_114024,
        R.raw.space_120280,
        R.raw.electronic_future_beats_117997,
        R.raw.powerful_energetic_sport_rock_trailer_122077,
    )
    private val viewModel: ActivityViewModel by activityViewModels()
    private var binding: FragmentActivityBinding? = null
    private var currentSongId: Int = 0
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivityBinding.inflate(inflater, container, false)
        val heart = binding?.heart
        val anim = AnimationUtils.loadAnimation(context, R.anim.beat)
        heart?.startAnimation(anim)
        shuffleSongs()
        binding?.btnTraining?.setOnClickListener { btnClickTraining() }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.description.observe(viewLifecycleOwner) {
            binding?.rangeText?.text = it
        }
        viewModel.heartRate.observe(viewLifecycleOwner) { heartRate ->
            heartRate?.let { hr ->
                binding?.puls?.text = hr.toString()
                viewModel.handleHeartRate(hr)
            }
        }
        viewModel.underRange.observe(viewLifecycleOwner) { isUnderRange ->
            if (isUnderRange && viewModel.supportType.value == 1) {
                pitchMusicDown()
            } else {
                normalizePitch()
            }
        }
        viewModel.aboveRange.observe(viewLifecycleOwner) { isAboveRange ->
            if (isAboveRange && viewModel.supportType.value == 1) {
                pitchMusicUp()
            } else {
                normalizePitch()
            }
        }
       setMusic()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopMusic()
        viewModel.resetValues()
    }

    /**
     * OnClick handler for the button in dependence of the text.
     */
    private fun btnClickTraining() {
        if (binding?.btnTraining?.text == getString(R.string.play)) {
            startTraining()
        } else {
            toMainMenu()
        }
    }

    /**
     * Sets a normal [pitch] (value 0) and gives it to the [mediaPlayer].
     */
    private fun normalizePitch() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.playbackParams = pitch.setPitch(1f)
        }
    }

    /**
     * Sets a new **higher** [pitch] and gives it to the [mediaPlayer].
     */
    private fun pitchMusicUp() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.playbackParams = pitch.setPitch(1.25f)
        }
    }

    /**
     * Sets a new **lower** [pitch] and gives it to the [mediaPlayer].
     */
    private fun pitchMusicDown() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.playbackParams = pitch.setPitch(0.75f)
        }
    }

    /**
     * Starts the [mediaPlayer] and sets the latest [pitch] for the resource.
     */
    private fun playMusic() {
        mediaPlayer?.start()
        mediaPlayer?.playbackParams = pitch
    }

    /**
     * Prepares the [mediaPlayer] for the songs. After a song is played, the next from the playlist
     * is automatically selected because the method calls itself by incrementing the current song by 1.
     * @param increment Is 0 without a parameter e.g. when the first song should start and another number
     * for any other song in the list.
     */
    private fun setMusic(increment: Int = 0) {
        currentSongId += increment
        if (currentSongId < songs.size) {
            mediaPlayer = MediaPlayer.create(context, songs[currentSongId])
            mediaPlayer?.setOnCompletionListener { mediaPlayer ->
                mediaPlayer.stop()
                mediaPlayer.release()
                setMusic(1)
                playMusic()
            }
        }
    }

    /**
     * Sets the song titles in a random order of [songs] array.
     */
    private fun shuffleSongs() {
        songs.shuffle(Random(Random.nextInt()))
    }

    /**
     * Initiates the start of the training by disabling the button, starting the music by [playMusic]
     * and setting a [CountDownTimer] that increases the values for [ActivityViewModel.timeInRange]
     * and [ActivityViewModel.timeOutOfRange] when [CountDownTimer.onTick] is called. When the
     * [CountDownTimer]'s [CountDownTimer.onFinish] is called, the mechanics for
     * [ActivityViewModel.stopTraining] are called and a [Toast] is shown.
     */
    private fun startTraining() {
        binding?.btnTraining?.text = getString(R.string.endTraining)
        binding?.btnTraining?.isEnabled = false
        playMusic()
        viewModel.startTraining()
        object : CountDownTimer(TRAINING_TIME, INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                viewModel.handleOnTick()
            }

            override fun onFinish() {
                stopMusic()
                lifecycleScope.launch {
                    viewModel.stopTraining()
                    whenResumed {
                        binding?.btnTraining?.isEnabled = true
                        Toast.makeText(context,
                            "Trainigsdurchlauf beendet, drücke auf 'Hauptmenü'"
                            , Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    /**
     * Stops the music and releases the [mediaPlayer].
     */
    private fun stopMusic () {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Method replaces [MainMenuFragment] as new Fragment in FragmentManager.
     */
    private fun toMainMenu() {
        parentFragmentManager.commit {
            replace<MainMenuFragment>(R.id.fragment_container_view, "MAIN_MENU")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    companion object {
        const val TRAINING_TIME: Long = 300000 // 5 min
        const val INTERVAL: Long = 1000 // 1 sec
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