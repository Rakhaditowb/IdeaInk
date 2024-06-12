package com.apps.ideaink.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.ideaink.databinding.FragmentSearchBinding
import com.apps.ideaink.ui.main.Expense
import com.apps.ideaink.ui.main.Memo
import com.apps.ideaink.ui.main.Note
import com.apps.ideaink.ui.main.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchAdapter
    private val items = mutableListOf<SearchItem>()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.recyclerView
        val searchView = binding.searchView

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchAdapter = SearchAdapter(items)
        recyclerView.adapter = searchAdapter

        loadRecentItems()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (!newText.isNullOrEmpty()) {
                    items.filter { it.title.contains(newText, true) }
                } else {
                    items
                }
                searchAdapter.updateData(filteredList)
                return true
            }
        })

        binding.ivFilter.setOnClickListener {
            val modal = ModalBottomSheetDialog { category ->
                filterRecyclerViewByCategory(category)
            }
            childFragmentManager.let { modal.show(it, ModalBottomSheetDialog.TAG) }
        }
    }

    private fun loadRecentItems() {
        // Load recent tasks
        val sharedPreferences = requireActivity().getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val tasksJson = sharedPreferences.getString("tasks", null)
        tasksJson?.let {
            val type = object : TypeToken<List<Task>>() {}.type
            val tasks: List<Task> = gson.fromJson(tasksJson, type)
            items.addAll(tasks.map { SearchItem(it.title, "Tasks") })
        }

        // Load recent notes
        val notesSharedPreferences = requireActivity().getSharedPreferences("notes", Context.MODE_PRIVATE)
        val notesJson = notesSharedPreferences.getString("notes", null)
        notesJson?.let {
            val type = object : TypeToken<List<Note>>() {}.type
            val notes: List<Note> = gson.fromJson(notesJson, type)
            items.addAll(notes.map { SearchItem(it.title, "Notes") })
        }

        // Load recent expenses
        val expensesSharedPreferences = requireActivity().getSharedPreferences("expenses", Context.MODE_PRIVATE)
        val expensesJson = expensesSharedPreferences.getString("expenses", null)
        expensesJson?.let {
            val type = object : TypeToken<List<Expense>>() {}.type
            val expenses: List<Expense> = gson.fromJson(expensesJson, type)
            items.addAll(expenses.map { SearchItem(it.title, "Expenses") })
        }

        // Load recent memos
        val memosSharedPreferences = requireActivity().getSharedPreferences("memos", Context.MODE_PRIVATE)
        val memosJson = memosSharedPreferences.getString("memos", null)
        memosJson?.let {
            val type = object : TypeToken<List<Memo>>() {}.type
            val memos: List<Memo> = gson.fromJson(memosJson, type)
            items.addAll(memos.map { SearchItem(it.title, "Memos") })
        }

        // Notify the adapter of the new data
        searchAdapter.updateData(items)
    }

    private fun filterRecyclerViewByCategory(category: String) {
        val filteredItems = items.filter { it.category == category }
        searchAdapter.updateData(filteredItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
