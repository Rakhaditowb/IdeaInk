package com.apps.ideaink.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.apps.ideaink.R
import com.apps.ideaink.databinding.FragmentMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

data class Memo(val title: String, val content: String)

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()

    private val taskReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadRecentTasks()
        }
    }

    private val noteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadRecentNotes()
        }
    }

    private val expenseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadRecentExpenses()
        }
    }

    private val memoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadRecentMemos()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.noteTemplate.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_noteFragment)
        }

        binding.financeTemplate.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_financeFragment)
        }

        binding.memoTemplate.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_memoFragment)
        }

        binding.todoTemplate.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_todoFragment)
        }

        loadAllRecentItems()

        return root
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(taskReceiver, IntentFilter("com.apps.ideaink.TASKS_SAVED"))
        requireContext().registerReceiver(noteReceiver, IntentFilter("com.apps.ideaink.NOTE_SAVED"))
        requireContext().registerReceiver(expenseReceiver, IntentFilter("com.apps.ideaink.EXPENSE_SAVED"))
        requireContext().registerReceiver(memoReceiver, IntentFilter("com.apps.ideaink.MEMO_SAVED"))
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(taskReceiver)
        requireContext().unregisterReceiver(noteReceiver)
        requireContext().unregisterReceiver(expenseReceiver)
        requireContext().unregisterReceiver(memoReceiver)
    }

    private fun loadAllRecentItems() {
        binding.recentLayout.removeAllViews()
        loadRecentTasks()
        loadRecentNotes()
        loadRecentExpenses()
        loadRecentMemos()
    }

    private fun loadRecentTasks() {
        val sharedPreferences = requireActivity().getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val tasksJson = sharedPreferences.getString("tasks", null)
        if (tasksJson != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            val tasks: List<Task> = gson.fromJson(tasksJson, type)
            for ((index, task) in tasks.withIndex()) {
                addRecentTaskView(task, index, tasks.toMutableList())
            }
        }
    }

    private fun addRecentTaskView(task: Task, index: Int, tasks: MutableList<Task>) {
        val recentLayout = binding.recentLayout
        val taskView = LayoutInflater.from(requireContext()).inflate(R.layout.item_task, recentLayout, false)
        val titleTextView: TextView = taskView.findViewById(R.id.tvTitle)
        titleTextView.text = task.title

        val deleteButton: Button = taskView.findViewById(R.id.btnDelete)

        deleteButton.setOnClickListener {
            deleteTask(index, tasks)
        }

        taskView.setOnClickListener {
            showTaskDetails(task)
        }

        recentLayout.addView(taskView)
    }

    private fun deleteTask(index: Int, tasks: MutableList<Task>) {
        tasks.removeAt(index)
        saveTasks(tasks)
        loadAllRecentItems()
    }

    private fun saveTasks(tasks: List<Task>) {
        val sharedPreferences = requireActivity().getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("tasks", gson.toJson(tasks))
        editor.apply()
    }

    private fun showTaskDetails(task: Task) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Task Details")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val titleTextView = TextView(requireContext())
        titleTextView.text = "Title: ${task.title}"
        layout.addView(titleTextView)

        builder.setView(layout)
        builder.setPositiveButton("Close", null)
        builder.show()
    }

    private fun loadRecentNotes() {
        val sharedPreferences = requireActivity().getSharedPreferences("notes", Context.MODE_PRIVATE)
        val notesJson = sharedPreferences.getString("notes", null)
        if (notesJson != null) {
            val type = object : TypeToken<List<Note>>() {}.type
            val notes: List<Note> = gson.fromJson(notesJson, type)
            for ((index, note) in notes.withIndex()) {
                addRecentNoteView(note, index, notes.toMutableList())
            }
        }
    }

    private fun addRecentNoteView(note: Note, index: Int, notes: MutableList<Note>) {
        val recentLayout = binding.recentLayout
        val noteView = LayoutInflater.from(requireContext()).inflate(R.layout.item_note, recentLayout, false)
        val titleTextView: TextView = noteView.findViewById(R.id.tvTitle)
        val contentTextView: TextView = noteView.findViewById(R.id.tvContent)
        titleTextView.text = note.title
        contentTextView.text = note.content

        val editButton: Button = noteView.findViewById(R.id.btnEdit)
        val deleteButton: Button = noteView.findViewById(R.id.btnDelete)

        editButton.setOnClickListener {
            showEditNoteDialog(note, index, notes)
        }

        deleteButton.setOnClickListener {
            deleteNote(index, notes)
        }

        noteView.setOnClickListener {
            showNoteDetails(note)
        }

        recentLayout.addView(noteView)
    }

    private fun showEditNoteDialog(note: Note, index: Int, notes: MutableList<Note>) {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val contentEditText = EditText(requireContext())
        contentEditText.setText(note.content)
        contentEditText.hint = "Edit your note"
        layout.addView(contentEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Note")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val updatedNote = note.copy(
                    content = contentEditText.text.toString()
                )
                notes[index] = updatedNote
                saveNotes(notes)
                loadRecentNotes()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNote(index: Int, notes: MutableList<Note>) {
        notes.removeAt(index)
        saveNotes(notes)
        loadAllRecentItems()
    }

    private fun saveNotes(notes: List<Note>) {
        val sharedPreferences = requireActivity().getSharedPreferences("notes", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("notes", gson.toJson(notes))
        editor.apply()
    }

    private fun showNoteDetails(note: Note) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Note Details")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val titleTextView = TextView(requireContext())
        titleTextView.text = "Title: ${note.title}"
        layout.addView(titleTextView)

        val contentTextView = TextView(requireContext())
        contentTextView.text = "Content: ${note.content}"
        layout.addView(contentTextView)

        builder.setView(layout)
        builder.setPositiveButton("Close", null)
        builder.show()
    }

    private fun loadRecentExpenses() {
        val sharedPreferences = requireActivity().getSharedPreferences("expenses", Context.MODE_PRIVATE)
        val expensesJson = sharedPreferences.getString("expenses", null)
        if (expensesJson != null) {
            val type = object : TypeToken<List<Expense>>() {}.type
            val expenses: List<Expense> = gson.fromJson(expensesJson, type)
            for ((index, expense) in expenses.withIndex()) {
                addRecentExpenseView(expense, index, expenses.toMutableList())
            }
        }
    }

    private fun addRecentExpenseView(expense: Expense, index: Int, expenses: MutableList<Expense>) {
        val recentLayout = binding.recentLayout
        val expenseView = LayoutInflater.from(requireContext()).inflate(R.layout.item_expense, recentLayout, false)
        val amountTextView: TextView = expenseView.findViewById(R.id.tvAmount)
        val descriptionTextView: TextView = expenseView.findViewById(R.id.tvDescription)
        val titleTextView: TextView = expenseView.findViewById(R.id.tvTitle)
        titleTextView.text = expense.title
        amountTextView.text = expense.amount
        descriptionTextView.text = expense.description

        val editButton: Button = expenseView.findViewById(R.id.btnEdit)
        val deleteButton: Button = expenseView.findViewById(R.id.btnDelete)

        editButton.setOnClickListener {
            showEditExpenseDialog(expense, index, expenses)
        }

        deleteButton.setOnClickListener {
            deleteExpense(index, expenses)
        }

        expenseView.setOnClickListener {
            showExpenseDetails(expense)
        }

        recentLayout.addView(expenseView)
    }

    private fun showEditExpenseDialog(expense: Expense, index: Int, expenses: MutableList<Expense>) {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val amountEditText = EditText(requireContext())
        amountEditText.setText(expense.amount)
        amountEditText.hint = "Enter amount"
        layout.addView(amountEditText)

        val descriptionEditText = EditText(requireContext())
        descriptionEditText.setText(expense.description)
        descriptionEditText.hint = "Enter description"
        layout.addView(descriptionEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Expense")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val updatedExpense = expense.copy(
                    amount = amountEditText.text.toString(),
                    description = descriptionEditText.text.toString()
                )
                expenses[index] = updatedExpense
                saveExpenses(expenses)
                loadRecentExpenses()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteExpense(index: Int, expenses: MutableList<Expense>) {
        expenses.removeAt(index)
        saveExpenses(expenses)
        loadAllRecentItems()
    }

    private fun saveExpenses(expenses: List<Expense>) {
        val sharedPreferences = requireActivity().getSharedPreferences("expenses", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("expenses", gson.toJson(expenses))
        editor.apply()
    }

    private fun showExpenseDetails(expense: Expense) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Finance Details")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val titleTextView = TextView(requireContext())
        titleTextView.text = "Title: ${expense.title}"
        layout.addView(titleTextView)

        val amountTextView = TextView(requireContext())
        amountTextView.text = "Amount: ${expense.amount}"
        layout.addView(amountTextView)

        val descriptionTextView = TextView(requireContext())
        descriptionTextView.text = "Description: ${expense.description}"
        layout.addView(descriptionTextView)

        builder.setView(layout)
        builder.setPositiveButton("Close", null)
        builder.show()
    }

    private fun loadRecentMemos() {
        val sharedPreferences = requireActivity().getSharedPreferences("memos", Context.MODE_PRIVATE)
        val memosJson = sharedPreferences.getString("memos", null)
        if (memosJson != null) {
            val type = object : TypeToken<List<Memo>>() {}.type
            val memos: List<Memo> = gson.fromJson(memosJson, type)
            for ((index, memo) in memos.withIndex()) {
                addRecentMemoView(memo, index, memos.toMutableList())
            }
        }
    }

    private fun addRecentMemoView(memo: Memo, index: Int, memos: MutableList<Memo>) {
        val recentLayout = binding.recentLayout
        val memoView = LayoutInflater.from(requireContext()).inflate(R.layout.item_memo, recentLayout, false)
        val titleTextView: TextView = memoView.findViewById(R.id.tvTitle)
        val contentTextView: TextView = memoView.findViewById(R.id.tvContent)
        titleTextView.text = memo.title
        contentTextView.text = memo.content

        val editButton: Button = memoView.findViewById(R.id.btnEdit)
        val deleteButton: Button = memoView.findViewById(R.id.btnDelete)

        editButton.setOnClickListener {
            showEditMemoDialog(memo, index, memos)
        }

        deleteButton.setOnClickListener {
            deleteMemo(index, memos)
        }

        memoView.setOnClickListener {
            showMemoDetails(memo)
        }

        recentLayout.addView(memoView)
    }

    private fun showEditMemoDialog(memo: Memo, index: Int, memos: MutableList<Memo>) {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val contentEditText = EditText(requireContext())
        contentEditText.setText(memo.content)
        contentEditText.hint = "Edit your memo"
        layout.addView(contentEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Memo")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val updatedMemo = memo.copy(
                    content = contentEditText.text.toString()
                )
                memos[index] = updatedMemo
                saveMemos(memos)
                loadRecentMemos()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMemo(index: Int, memos: MutableList<Memo>) {
        memos.removeAt(index)
        saveMemos(memos)
        loadAllRecentItems()
    }

    private fun saveMemos(memos: List<Memo>) {
        val sharedPreferences = requireActivity().getSharedPreferences("memos", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("memos", gson.toJson(memos))
        editor.apply()
    }

    private fun showMemoDetails(memo: Memo) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Memo Details")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val titleTextView = TextView(requireContext())
        titleTextView.text = "Title: ${memo.title}"
        layout.addView(titleTextView)

        val contentTextView = TextView(requireContext())
        contentTextView.text = "Content: ${memo.content}"
        layout.addView(contentTextView)

        builder.setView(layout)
        builder.setPositiveButton("Close", null)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
