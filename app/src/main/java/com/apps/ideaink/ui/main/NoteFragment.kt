package com.apps.ideaink.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.apps.ideaink.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Note(
    val title: String,
    val content: String
)

class NoteFragment : Fragment() {

    private lateinit var noteEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note, container, false)
        noteEditText = view.findViewById(R.id.etNoteContent)
        val saveNoteButton: Button = view.findViewById(R.id.btnSaveNote)

        saveNoteButton.setOnClickListener {
            showSaveDialog()
        }

        return view
    }

    private fun showSaveDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter title"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Save Note")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString()
                if (title.isNotEmpty()) {
                    saveNote(title)
                    // Send broadcast to notify MainFragment
                    val intent = Intent("com.apps.ideaink.NOTE_SAVED")
                    requireContext().sendBroadcast(intent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveNote(title: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("notes", Context.MODE_PRIVATE)
        val gson = Gson()
        val notesJson = sharedPreferences.getString("notes", null)
        val type = object : TypeToken<List<Note>>() {}.type
        val notes: MutableList<Note> = if (notesJson != null) {
            gson.fromJson(notesJson, type)
        } else {
            mutableListOf()
        }

        val content = noteEditText.text.toString()
        val newNote = Note(title, content)
        notes.add(newNote)

        if (notes.size > 20) {
            notes.subList(0, notes.size - 20).clear()
        }

        val editor = sharedPreferences.edit()
        editor.putString("notes", gson.toJson(notes))
        editor.apply()
    }
}
