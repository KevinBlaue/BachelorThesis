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
    private var binding: FragmentMainMenuBinding? = null
    private val viewModel: MainMenuViewModel by activityViewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()

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

    private fun clickBtnWithoutSupport() {
        activityViewModel.supportType.value = SupportType.WITHOUT_SUPPORT.type
        parentFragmentManager.commit {
            replace<ChooseFragment>(R.id.fragment_container_view, "CHOOSE")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private fun clickBtnWithSupport() {
        activityViewModel.supportType.value = SupportType.WITH_SUPPORT.type
        parentFragmentManager.commit {
            replace<ChooseFragment>(R.id.fragment_container_view, "CHOOSE")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private fun exportDataToCSV() {
        lifecycleScope.launch {
            activityViewModel.createCSV()
        }
    }
}
