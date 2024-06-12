package com.apps.ideaink.ui.search

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apps.ideaink.databinding.ModalsheetdialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheetDialog(private val filterCallback: (String) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: ModalsheetdialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalsheetdialogBinding.inflate(inflater, container, false)

        // Set up button click listeners
        binding.categoryTasks.setOnClickListener {
            filterCallback("Tasks")
            dismiss()
        }

        binding.categoryNotes.setOnClickListener {
            filterCallback("Notes")
            dismiss()
        }

        binding.categoryExpenses.setOnClickListener {
            filterCallback("Expenses")
            dismiss()
        }

        binding.categoryMemos.setOnClickListener {
            filterCallback("Memos")
            dismiss()
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { it ->
            val d = it as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    companion object {
        const val TAG = "ModalBottomSheetDialog"
    }
}
