package com.apps.ideaink.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.apps.ideaink.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Task(val title: String)

class ToDoFragment : Fragment() {

    private lateinit var taskEditText: EditText
    private lateinit var tasksAdapter: ArrayAdapter<String>
    private val tasksList: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_do, container, false)

        taskEditText = view.findViewById(R.id.etTask)
        val addTaskButton: Button = view.findViewById(R.id.btnAddTask)
        val saveTasksButton: Button = view.findViewById(R.id.btnSaveTasks)
        val tasksListView: ListView = view.findViewById(R.id.lvTasks)

        tasksAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tasksList)
        tasksListView.adapter = tasksAdapter

        addTaskButton.setOnClickListener {
            val task = taskEditText.text.toString()
            if (task.isNotEmpty()) {
                tasksList.add(task)
                tasksAdapter.notifyDataSetChanged()
                taskEditText.text.clear()
            }
        }

        saveTasksButton.setOnClickListener {
            showSaveDialog()
        }

        return view
    }

    private fun showSaveDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter title"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Save Tasks")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString()
                if (title.isNotEmpty()) {
                    saveTasks(title)
                    // Send broadcast to notify MainFragment
                    val intent = Intent("com.apps.ideaink.TASKS_SAVED")
                    requireContext().sendBroadcast(intent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveTasks(title: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val gson = Gson()
        val tasksJson = sharedPreferences.getString("tasks", null)
        val type = object : TypeToken<List<Task>>() {}.type
        val tasks: MutableList<Task> = if (tasksJson != null) {
            gson.fromJson(tasksJson, type)
        } else {
            mutableListOf()
        }

        val newTask = Task(title)
        tasks.add(newTask)

        if (tasks.size > 20) {
            tasks.subList(0, tasks.size - 20).clear()
        }

        val editor = sharedPreferences.edit()
        editor.putString("tasks", gson.toJson(tasks))
        editor.apply()
    }
}
