package com.example.digitaldiary.repository;

import com.example.digitaldiary.model.Entry;

interface EntryRepository {
    fun addEntry(entry: Entry)
    fun updateEntry(entry: Entry)
    fun removeEntry(entry: Entry)
    fun getEntryId(entry: Entry): Int
    fun insertEntryAtIndex(entry: Entry, index: Int)
    fun replaceEntry(entryToInsert: Entry, contentOfEntryToReplace: String)
    fun getEntryByContent(content: String): Entry
    fun getEntries(): List<Entry>
}
