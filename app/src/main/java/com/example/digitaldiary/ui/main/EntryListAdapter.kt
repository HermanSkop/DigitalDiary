package com.example.digitaldiary.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.digitaldiary.R
import com.example.digitaldiary.repository.InMemoryEntryRepository


class EntryListAdapter(private val mainViewModel: MainViewModel) : RecyclerView.Adapter<EntryListAdapter.EntryViewHolder>() {
    private val entryRepository: InMemoryEntryRepository = InMemoryEntryRepository

    class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val entryView: Button = view.findViewById(R.id.entry_item)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.entry_item, parent, false)

        return EntryViewHolder(adapterLayout)
    }


    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val item = entryRepository.getEntries()[position]
        holder.entryView.text = item.title
        holder.entryView.setOnClickListener {
            mainViewModel.onEntryClicked()
        }
    }


    override fun getItemCount() = entryRepository.getEntries().size
}