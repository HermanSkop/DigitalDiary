package com.example.digitaldiary.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.digitaldiary.R
import com.example.digitaldiary.ViewModel
import com.example.digitaldiary.model.Note


class EntryListAdapter(private val viewModel: ViewModel) :
    RecyclerView.Adapter<EntryListAdapter.EntryViewHolder>() {
    private var notesList: List<Note> = listOf()

    class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val entryView: Button = view.findViewById(R.id.entry_item)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(R.layout.entry_item, parent, false)
        return EntryViewHolder(adapterLayout)
    }


    fun updateNotes(newNotes: List<Note>) {
        notesList = newNotes
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val item = notesList[position]
        with(holder.entryView) {
            text = item.title
            setOnClickListener {
                viewModel.editNote(item)
            }
            setOnLongClickListener {
                AlertDialog.Builder(context)
                    .setTitle(context.resources.getString(R.string.delete_note))
                    .setMessage(context.resources.getString(R.string.delete_note_message))
                    .setPositiveButton(context.resources.getString(R.string.yes)) { _, _ ->
                        viewModel.deleteNote(item)
                    }.setNegativeButton(context.resources.getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                    }.show()
                true
            }
        }
    }

    override fun getItemCount() = notesList.size
}