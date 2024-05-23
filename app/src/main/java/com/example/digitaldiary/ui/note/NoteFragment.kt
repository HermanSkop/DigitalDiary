package com.example.digitaldiary.ui.note

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.digitaldiary.R
import com.example.digitaldiary.ViewModel
import com.example.digitaldiary.databinding.FragmentNoteBinding
import com.example.digitaldiary.model.Note
import java.time.LocalDate

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null

    private val binding get() = _binding!!
    private lateinit var viewModel: ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        _binding = FragmentNoteBinding.inflate(inflater, container, false)

        fillNote()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.save -> {
                        if (binding.title.text.isBlank()) context?.resources?.let {
                            viewModel.showErrorMessage(it.getString(R.string.empty_title))
                        } else
                            viewModel.saveNote(
                                binding.title.text.toString(), binding.content.text.toString()
                            )
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fillNote() {
        with(binding) {
            title.setText(viewModel.currentNote?.title)
            content.setText(viewModel.currentNote?.content)
            contentLayout.hint = LocalDate.now().toString()
            contentLayout.defaultHintTextColor =
                ColorStateList.valueOf(resources.getColor(R.color.textBright, null))
        }
    }
}