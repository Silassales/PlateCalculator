package com.silas.platecalculator

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class WeightPlateNumberDialog(label: String, currentCount: Number?) : DialogFragment() {

    private lateinit var listener: NoticeDialogListener
    lateinit var numberPicker: NumberPicker
    var label = label
    var currentCount = currentCount

    interface NoticeDialogListener {
        fun onDialogPositiveClick(value: Number, label: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.weight_plate_dialog, null)

            numberPicker = view.findViewById(R.id.weightPlateDialogNumberPicker)
            numberPicker.minValue = 0
            numberPicker.maxValue = 10
            numberPicker.displayedValues = arrayOf("0","1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
            numberPicker.wrapSelectorWheel = false
            numberPicker.value = 10
            currentCount?.let { c -> numberPicker.value = c as Int }

            builder.setView(view)
                .setPositiveButton(R.string.select
                ) { _, _ ->
                    listener.onDialogPositiveClick(numberPicker.value, label)
                }
                .setNegativeButton(R.string.cancel
                ) { _, _ ->
                    dialog?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}