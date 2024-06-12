package com.apps.ideaink.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apps.ideaink.R

class FinanceAdapter(private val expenseList: MutableList<Expense>) :
    RecyclerView.Adapter<FinanceAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountTextView: TextView = itemView.findViewById(R.id.tvAmount)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentExpense = expenseList[position]
        holder.titleTextView.text = currentExpense.title
        holder.amountTextView.text = currentExpense.amount
        holder.descriptionTextView.text = currentExpense.description
    }

    override fun getItemCount() = expenseList.size

    fun addExpense(expense: Expense) {
        if (expenseList.size >= 20) {
            expenseList.removeAt(0)
        }
        expenseList.add(expense)
        notifyItemInserted(expenseList.size - 1)
    }

    fun getExpenses(): List<Expense> {
        return expenseList
    }
}
