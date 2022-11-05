package de.hsfl.kevinblaue.musicrun.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.LinearLayoutManager
import de.hsfl.kevinblaue.musicrun.R
import de.hsfl.kevinblaue.musicrun.databinding.FragmentChooseBinding
import de.hsfl.kevinblaue.musicrun.models.RangeEntry
import de.hsfl.kevinblaue.musicrun.viewmodels.ActivityViewModel
import de.hsfl.kevinblaue.musicrun.viewmodels.ChooseViewModel
import de.hsfl.kevinblaue.musicrun.views.RecyclerViewAdapter

class ChooseFragment : Fragment() {
    private var binding: FragmentChooseBinding? = null
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val chooseViewModel: ChooseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChooseBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chooseViewModel.list.observe(viewLifecycleOwner) { items ->
            val rv = binding?.recyclerView
            val adapter = RecyclerViewAdapter(items) {
                    entry -> clickBtnRange(entry)
            }
            rv?.adapter = adapter
            rv?.layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * OnClick handler for the list elements of [RecyclerViewAdapter]. Sets mandatory data for
     * the [ActivityViewModel] and replaces the Fragment of the FragmentManager by [ActivityFragment].
     * @param entry The chosen RangeEntry from the [ChooseViewModel.list].
     */
    private fun clickBtnRange(entry: RangeEntry) {
        activityViewModel.setRangeEntry(entry)
        parentFragmentManager.commit {
            replace<ActivityFragment>(R.id.fragment_container_view, "ACTIVITY_WITH_SUPPORT")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }
}