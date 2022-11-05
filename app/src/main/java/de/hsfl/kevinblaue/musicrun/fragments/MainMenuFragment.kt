package de.hsfl.kevinblaue.musicrun.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import de.hsfl.kevinblaue.musicrun.R
import de.hsfl.kevinblaue.musicrun.databinding.FragmentMainMenuBinding
import de.hsfl.kevinblaue.musicrun.models.SupportType
import de.hsfl.kevinblaue.musicrun.viewmodels.ActivityViewModel
import de.hsfl.kevinblaue.musicrun.viewmodels.MainMenuViewModel
import kotlinx.coroutines.launch

class MainMenuFragment : Fragment() {
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val viewModel: MainMenuViewModel by activityViewModels()
    private var binding: FragmentMainMenuBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        binding?.btnWithout?.setOnClickListener{ clickBtnWithoutSupport() }
        binding?.btnWith?.setOnClickListener{ clickBtnWithSupport() }
        binding?.appTitle?.setOnClickListener{ exportDataToCSV() }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.beltConnected.observe(viewLifecycleOwner) { connected ->
            if (connected) {
                binding?.infoText?.text = getString(R.string.bluetoothInformationConnected)
            }
        }
    }

    /**
     * OnClick handler for the first button in the [MainMenuFragment] UI.
     * Sets the [SupportType] value for not supported training in the [ActivityFragment] and replaces
     * the current Fragment of the FragmentManager with the [ChooseFragment].
     */
    private fun clickBtnWithSupport() {
        activityViewModel.supportType.value = SupportType.WITH_SUPPORT.type
        parentFragmentManager.commit {
            replace<ChooseFragment>(R.id.fragment_container_view, "CHOOSE")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    /**
     * OnClick handler for the second button in the [MainMenuFragment] UI.
     * Sets the [SupportType] value for supported training in the [ActivityFragment] and replaces
     * the current Fragment of the FragmentManager with the [ChooseFragment].
     */
    private fun clickBtnWithoutSupport() {
        activityViewModel.supportType.value = SupportType.WITHOUT_SUPPORT.type
        parentFragmentManager.commit {
            replace<ChooseFragment>(R.id.fragment_container_view, "CHOOSE")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    /**
     * OnClick handler for the hidden button of the app name in the [MainMenuFragment] UI.
     * Starts the coroutine for the CSV data exportation.
     */
    private fun exportDataToCSV() {
        lifecycleScope.launch {
            viewModel.createCSV()

        }
    }
}
