package com.apps.ideaink.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apps.ideaink.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Expense(val title: String, val amount: String, val description: String)

class FinanceFragment : Fragment() {

    private lateinit var financeAdapter: FinanceAdapter
    private lateinit var amountEditText: EditText
    private lateinit var descriptionEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_finance, container, false)

        amountEditText = view.findViewById(R.id.etAmount)
        descriptionEditText = view.findViewById(R.id.etDescription)
        val addExpenseButton: Button = view.findViewById(R.id.btnAddExpense)
        val saveExpenseButton: Button = view.findViewById(R.id.btnSaveExpense)
        val recyclerView: RecyclerView = view.findViewById(R.id.rvExpenses)

        financeAdapter = FinanceAdapter(mutableListOf())
        recyclerView.adapter = financeAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        addExpenseButton.setOnClickListener {
            val amount = amountEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (amount.isNotEmpty() && description.isNotEmpty()) {
                val expense = Expense("", amount, description)
                financeAdapter.addExpense(expense)
                amountEditText.text.clear()
                descriptionEditText.text.clear()
            }
        }

        saveExpenseButton.setOnClickListener {
            showSaveDialog()
        }

        return view
    }

    private fun showSaveDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter title"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Save Finance")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString()
                if (title.isNotEmpty()) {
                    saveExpenses(title)
                    // Send broadcast to notify MainFragment
                    val intent = Intent("com.apps.ideaink.EXPENSE_SAVED")
                    requireContext().sendBroadcast(intent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveExpenses(title: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("expenses", Context.MODE_PRIVATE)
        val gson = Gson()
        val expensesJson = sharedPreferences.getString("expenses", null)
        val type = object : TypeToken<List<Expense>>() {}.type
        val expenses: MutableList<Expense> = if (expensesJson != null) {
            gson.fromJson(expensesJson, type)
        } else {
            mutableListOf()
        }

        val newExpenses = financeAdapter.getExpenses().map { it.copy(title = title) }
        expenses.addAll(newExpenses)

        if (expenses.size > 20) {
            expenses.subList(0, expenses.size - 20).clear()
        }

        val editor = sharedPreferences.edit()
        editor.putString("expenses", gson.toJson(expenses))
        editor.apply()
        Log.d("FinanceFragment", "Finance saved: ${gson.toJson(expenses)}")
    }
}
