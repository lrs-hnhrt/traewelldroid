package de.hbch.traewelling.ui.activeCheckins

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.databinding.FragmentActiveCheckinsBinding

class ActiveCheckinsFragment : Fragment() {

    private lateinit var binding: FragmentActiveCheckinsBinding
    private val viewModel: ActiveCheckinsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActiveCheckinsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val recyclerView = binding.recyclerViewActiveCheckIns
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.statuses.observe(viewLifecycleOwner) { statusPage ->
            if (statusPage != null) {
                recyclerView.adapter =
                    CheckInAdapter(
                        statusPage.data.toMutableList()
                    )
            }
            binding.swipeRefreshCheckins.isRefreshing = false
        }
        getActiveCheckins()

        binding.swipeRefreshCheckins.setOnRefreshListener {
            getActiveCheckins()
        }

        return binding.root
    }

    fun getActiveCheckins() {
        binding.swipeRefreshCheckins.isRefreshing = true
        viewModel.getActiveCheckins()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}