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

class MemoFragment : Fragment() {

    private lateinit var memoEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_memo, container, false)
        memoEditText = view.findViewById(R.id.memoEditText)
        val saveMemoButton: Button = view.findViewById(R.id.btnSaveMemo)

        saveMemoButton.setOnClickListener {
            showSaveDialog()
        }

        return view
    }

    private fun showSaveDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter title"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Save Memo")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString()
                if (title.isNotEmpty()) {
                    saveMemo(title)
                    // Send broadcast to notify MainFragment
                    val intent = Intent("com.apps.ideaink.MEMO_SAVED")
                    requireContext().sendBroadcast(intent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveMemo(title: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("memos", Context.MODE_PRIVATE)
        val gson = Gson()
        val memosJson = sharedPreferences.getString("memos", null)
        val type = object : TypeToken<List<Memo>>() {}.type
        val memos: MutableList<Memo> = if (memosJson != null) {
            gson.fromJson(memosJson, type)
        } else {
            mutableListOf()
        }

        val content = memoEditText.text.toString()
        val newMemo = Memo(title, content)
        memos.add(newMemo)

        if (memos.size > 20) {
            memos.subList(0, memos.size - 20).clear()
        }

        val editor = sharedPreferences.edit()
        editor.putString("memos", gson.toJson(memos))
        editor.apply()
    }
}
