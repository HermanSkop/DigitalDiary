package com.example.digitaldiary.ui.note

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.digitaldiary.MainActivity
import com.example.digitaldiary.R
import com.example.digitaldiary.ViewModel
import com.example.digitaldiary.databinding.FragmentNoteBinding
import java.time.LocalDate


class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        _binding = FragmentNoteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.image.setOnLongClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_image))
                .setMessage(resources.getString(R.string.delete_image_message))
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    viewModel.removeImage()
                }.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
            true
        }
        menuSetUp()
        viewModelSetUp()
    }

    private fun menuSetUp() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.save -> {
                        if (binding.title.text.isBlank()) context?.resources?.let {
                            viewModel.showErrorMessage(it.getString(R.string.empty_title))
                        } else {
                            viewModel.saveNote(
                                binding.title.text.toString(),
                                binding.content.text.toString(),
                                activity as MainActivity
                            )

                        }
                        return true
                    }
                }
                return false
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_note, menu)
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun viewModelSetUp() {
        with(viewModel) {
            navigateTo.observe(viewLifecycleOwner) {
                val destination = it.getContentIfNotHandled() ?: return@observe
                when (destination) {
                    ViewModel.Destination.PAINT -> {
                        findNavController().navigate(R.id.action_NoteFragment_to_PaintFragment)
                    }
                    // ViewModel.Destination.AUDIO -> TODO()
                    else -> throw IllegalArgumentException("Unknown destination: $it")
                }
            }

            currentNote.observe(viewLifecycleOwner) {
                fillNote()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fillNote() {
        with(binding) {
            title.setText(viewModel.currentNote.value?.title)
            content.setText(viewModel.currentNote.value?.content)
            contentLayout.hint = LocalDate.now().toString()
            contentLayout.defaultHintTextColor =
                ColorStateList.valueOf(resources.getColor(R.color.textBright, null))
            location.text = viewModel.currentNote.value?.location

            if (viewModel.currentNote.value?.imageUri.isNullOrBlank()) {
                image.visibility = View.GONE
            } else {
                image.visibility = View.VISIBLE
                image.setImageURI(Uri.parse(viewModel.currentNote.value?.imageUri))
            }
        }
    }
}