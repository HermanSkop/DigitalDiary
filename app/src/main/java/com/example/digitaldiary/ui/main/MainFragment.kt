package com.example.digitaldiary.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.digitaldiary.R
import com.example.digitaldiary.ViewModel
import com.example.digitaldiary.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private lateinit var viewModel: ViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = EntryListAdapter(viewModel)
        binding.entryList.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.navigateTo.observe(viewLifecycleOwner) {
            val destination = it.getContentIfNotHandled() ?: return@observe
            when (destination) {
                ViewModel.Destination.NOTE -> {
                    findNavController().navigate(R.id.action_MainFragment_to_NoteFragment)
                }
                else -> throw IllegalArgumentException("Unknown destination: $it")
            }
        }

        viewModel.notesLiveData.observe(viewLifecycleOwner) { notes ->
            adapter.updateNotes(notes ?: emptyList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}