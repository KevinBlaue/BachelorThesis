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
        R.raw.sound1,
        R.raw.sound2,
        R.raw.sound3,
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

        // Bind onlick listeners
        binding?.btnPlay?.setOnClickListener { playMusic() }
        binding?.btnStop?.setOnClickListener {
            lifecycleScope.launch {
                stopTraining()
            }
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.description.observe(viewLifecycleOwner) {
            binding?.rangeText?.text = it
        }
        viewModel.heartBeat.observe(viewLifecycleOwner) {
            binding?.puls?.text = it
        }
        viewModel.isUnderRange.observe(viewLifecycleOwner) {
            if (it && viewModel.supportType.value == 1) {
                pitchMusicDown()
            } else {
                normalizePitch()
            }
        }
        viewModel.isAboveRange.observe(viewLifecycleOwner) {
            if (it && viewModel.supportType.value == 1) {
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

    private fun playMusic() {
        if (binding?.btnPlay?.text!! == getString(R.string.play)) {
            viewModel.startTraining()
            mediaPlayer?.start()
            binding?.btnPlay?.text = getString(R.string.running)
            binding?.btnPlay?.isEnabled = true
        }
    }

    private fun stopMusic () {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        binding?.btnPlay?.text = getString(R.string.play)
    }

    private suspend fun stopTraining() {
        // Stop and release Mediaplayer
        stopMusic()

        // Write data to Database
        viewModel.saveStatistics()

        // Go back to MainMenu
        parentFragmentManager.commit {
            replace<MainMenuFragment>(R.id.fragment_container_view, "MAIN_MENU")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private fun pitchMusicUp() {
        mediaPlayer?.playbackParams = pitch.setPitch(1.25f)
    }

    private fun pitchMusicDown() {
        mediaPlayer?.playbackParams = pitch.setPitch(0.75f)
    }

    private fun normalizePitch() {
        mediaPlayer?.playbackParams = pitch.setPitch(1f)
    }
}