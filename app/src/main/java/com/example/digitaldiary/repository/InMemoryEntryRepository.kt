package com.example.digitaldiary.repository

import com.example.digitaldiary.model.Entry

object InMemoryEntryRepository : EntryRepository {
    private val entryList = mutableListOf(
        Entry(1, "Entry 1"),
        Entry(2, "Entry 2"),
        Entry(3, "Entry 3"),
        Entry(4, "Entry 4"),
        Entry(5, "Entry 5"),
        Entry(6, "Entry 6")
    )

    override fun addEntry(entry: Entry) {
        if (entryList.contains(entry)) {
            throw IllegalArgumentException("Entry already exists")
        }
        entryList.add(entry)
    }

    override fun removeEntry(entry: Entry) {
        entryList.remove(entry)
    }

    override fun updateEntry(entry: Entry) {
        if (!entryList.contains(entry)) {
            throw IllegalArgumentException("Entry does not exist")
        }
        entryList[entryList.indexOf(entry)] = entry
    }

    override fun getEntryId(entry: Entry): Int {
        return entryList.indexOf(entry)
    }

    override fun insertEntryAtIndex(entry: Entry, index: Int) {
        entryList.add(index, entry)
    }

    override fun replaceEntry(entryToInsert: Entry, contentOfEntryToReplace: String) {
        if (entryList.contains(entryToInsert) && entryToInsert.content != contentOfEntryToReplace) {
            throw IllegalArgumentException("Entry already exists")
        }
        val entryToReplace = getEntryByContent(contentOfEntryToReplace)
        val index = getEntryId(entryToReplace)
        removeEntry(entryToReplace)
        insertEntryAtIndex(entryToInsert, index)
    }

    override fun getEntryByContent(content: String): Entry {
        return entryList.find { it.content == content }!!
    }

    override fun getEntries(): List<Entry> {
        return entryList
    }
}