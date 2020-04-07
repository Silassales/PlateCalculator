package com.silas.platecalculator

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.snackbar.Snackbar
import com.silas.platecalculator.Constants.BARBELL_WEIGHT_KG
import com.silas.platecalculator.Constants.BARBELL_WEIGHT_LB
import com.silas.platecalculator.Constants.FIRST_SLOT_MARGIN
import com.silas.platecalculator.Constants.MAX_WEIGHT_KG
import com.silas.platecalculator.Constants.MAX_WEIGHT_LB
import com.silas.platecalculator.Constants.PLATE1_KG
import com.silas.platecalculator.Constants.PLATE1_LB
import com.silas.platecalculator.Constants.PLATE2_KG
import com.silas.platecalculator.Constants.PLATE2_LB
import com.silas.platecalculator.Constants.PLATE3_KG
import com.silas.platecalculator.Constants.PLATE3_LB
import com.silas.platecalculator.Constants.PLATE4_KG
import com.silas.platecalculator.Constants.PLATE4_LB
import com.silas.platecalculator.Constants.PLATE5_KG
import com.silas.platecalculator.Constants.PLATE5_LB
import com.silas.platecalculator.Constants.PLATE6_KG
import com.silas.platecalculator.Constants.PLATE6_LB
import com.silas.platecalculator.Constants.PLATE7_KG
import com.silas.platecalculator.Constants.PLATE7_LB
import com.silas.platecalculator.Constants.SLOT_MARGIN
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.NumberFormatException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var use25KG: Boolean = true
    private lateinit var currentUnit: String

    lateinit var prefs: SharedPreferences
    lateinit var weightEditText: EditText
    lateinit var unitSpinner: Spinner
    lateinit var weightSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getPreferences(Context.MODE_PRIVATE)
        weightEditText = findViewById(R.id.barbell_weight_edittext)
        weightSeekBar = findViewById(R.id.weight_seek_bar)
        currentUnit = prefs.getString(getString(R.string.storedUnit), getString(R.string.KG)).toString()

        setupWeightPlateLabels()

        unitSpinner = findViewById(R.id.barbell_weight_unit_spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.wtUnitArray,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            unitSpinner.adapter = adapter
        }
        unitSpinner.onItemSelectedListener = this

        weightEditText.addTextChangedListener(object:
            TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateBarbell()
            }
        })

        weightSeekBar.max = if(currentUnit == getString(R.string.KG)) {
            MAX_WEIGHT_KG
        } else {
            MAX_WEIGHT_LB
        }
        try {
            val weight = prefs.getFloat(getString(R.string.storedWeight), 0.0F)
            weightSeekBar.progress = if(currentUnit == getString(R.string.KG)) {
                weight.toInt()
            } else {
                weight.toInt()
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        weightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                weightEditText.setText(progress.toString())
                updateBarbell()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        val use25KGSwitch = findViewById<Switch>(R.id.use_25kg_switch)
        // TODO change this to a storedPref
        use25KGSwitch.isChecked = true
        use25KG = use25KGSwitch.isChecked
        setup25Switch()
        use25KGSwitch.setOnCheckedChangeListener { _, isChecked ->
            use25KG = isChecked
            updateBarbell()

            findViewById<TextView>(R.id.plate_weight_label1).visibility = if (isChecked) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        val savedWeight = prefs.getFloat(getString(R.string.storedWeight), 0.0F)
        currentUnit = prefs.getString(getString(R.string.storedUnit), getString(R.string.KG)).toString()
        if(savedWeight != 0.0F) {
            weightEditText.setText(savedWeight.toString())
            updateBarbell()
        }
    }

    override fun onPause() {
        super.onPause()

        try {
            prefs.edit().putFloat(getString(R.string.storedWeight), weightEditText.text.toString().toFloat()).apply()
        } catch (e: NumberFormatException) {
            Log.e("onPause", "weight edit text does not contain a number")
        }

        prefs.edit().putString(getString(R.string.storedUnit), unitSpinner.selectedItem.toString()).apply()
    }

    private fun setup25Switch() {
        val switch = findViewById<Switch>(R.id.use_25kg_switch)
        val wtUnitSpinner = findViewById<Spinner>(R.id.barbell_weight_unit_spinner)
        val weightUnit = wtUnitSpinner?.selectedItem ?: getString(R.string.KG)

        switch.text = if(weightUnit == getString(R.string.KG)) getString(R.string.use25SwitchTextKG) else getString(R.string.use25SwitchTextLB)
    }

    private fun setupWeightPlateLabels() {
        val wtUnitSpinner = findViewById<Spinner>(R.id.barbell_weight_unit_spinner)
        val weightUnit = wtUnitSpinner?.selectedItem ?: getString(R.string.KG)

        findViewById<TextView>(R.id.plate_weight_label1).text = if(weightUnit == getString(R.string.KG)) PLATE1_KG.toString() else PLATE1_LB.toString()
        findViewById<TextView>(R.id.plate_weight_label2).text = if(weightUnit == getString(R.string.KG)) PLATE2_KG.toString() else PLATE2_LB.toString()
        findViewById<TextView>(R.id.plate_weight_label3).text = if(weightUnit == getString(R.string.KG)) PLATE3_KG.toString() else PLATE3_LB.toString()
        findViewById<TextView>(R.id.plate_weight_label4).text = if(weightUnit == getString(R.string.KG)) PLATE4_KG.toString() else PLATE4_LB.toString()
        findViewById<TextView>(R.id.plate_weight_label5).text = if(weightUnit == getString(R.string.KG)) PLATE5_KG.toString() else PLATE5_LB.toString()
        findViewById<TextView>(R.id.plate_weight_label6).text = if(weightUnit == getString(R.string.KG)) PLATE6_KG.toString() else PLATE6_LB.toString()
        findViewById<TextView>(R.id.plate_weight_label7).text = if(weightUnit == getString(R.string.KG)) PLATE7_KG.toString() else PLATE7_LB.toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // called everytime the view is redrawn (orientation change, when the activity start, etc), so
        // we need to make sure we actually want to covert the weights
        if(currentUnit != parent?.getItemAtPosition(position)) {
            // try to convert the current weight
            try {
                currentUnit = parent?.getItemAtPosition(position).toString()
                var currentWeight = weightEditText.text.toString().toDouble()
                val weightUnit = parent?.getItemAtPosition(position).toString()

                if (weightUnit == getString(R.string.LB)) { // we are going from LG -> KG
                    currentWeight *= 2.2
                } else if (weightUnit == getString(R.string.KG)) { // we are going from KG -> LB
                    currentWeight /= 2.2
                }

                // round to the nearest .5
                currentWeight = (currentWeight * 2).roundToInt() / 2.0

                weightEditText.setText(currentWeight.toString())
            } catch (e: NumberFormatException) {
                Log.d("onItemSelected", "trying to parse current weight: $e")
            }
            setupWeightPlateLabels()
            setup25Switch()
            updateBarbell()
        }
    }

    private fun updateBarbell(selectedUnit: String = "") {
        var weightUnit = selectedUnit
        if(selectedUnit.isBlank()) {
            val wtUnitSpinner = findViewById<Spinner>(R.id.barbell_weight_unit_spinner)
            wtUnitSpinner?.let { weightUnit = it.selectedItem as String }
        }

        when (weightUnit) {
            getString(R.string.KG) -> updateBarbellKG()
            getString(R.string.LB) -> updateBarbellLB()
            else -> Log.d("updateBarbell", "Invalid unit spinner value: $weightUnit")
        }
    }

    private fun updateBarbellKG(){
        val weightEditText = findViewById<EditText>(R.id.barbell_weight_edittext)
        val constraintLayout = findViewById<ConstraintLayout>(R.id.barbell_plates_constraint_layout)
        var resourceIdToSet: Int
        var previousPlateId: Int
        var plateId = 0
        var firstSlotEmpty = true

        var weight = try { weightEditText.text.toString().toDouble() } catch (e: NumberFormatException) { Log.d("updateBarbellKG", e.stackTrace.toString()); return }

        /* make sure we don't have any plates on there currently, in case we are removing weight */
        constraintLayout.removeAllViews()

        /* make sure too much doesn't crash the program */
        if(weight > MAX_WEIGHT_KG) {
            weightEditText.setText(MAX_WEIGHT_KG.toString())
            return
        }

        /* adjust for the weight of the bar */
        weight -= BARBELL_WEIGHT_KG

        /* adjust for just showing one half of the bar */
        weight /= 2

        while(weight > 0) {
            // first determine the icon we want to use
            if(weight >= PLATE1_KG && use25KG) {
                resourceIdToSet = R.drawable.ic_25kg
                weight -= PLATE1_KG
            } else if(weight >= PLATE2_KG) {
                resourceIdToSet = R.drawable.ic_20kg
                weight -= PLATE2_KG
            } else if (weight >= PLATE3_KG) {
                resourceIdToSet = R.drawable.ic_15kg
                weight -= PLATE3_KG
            } else if (weight >= PLATE4_KG) {
                resourceIdToSet = R.drawable.ic_10kg
                weight -= PLATE4_KG
            } else if(weight >= PLATE5_KG) {
                resourceIdToSet = R.drawable.ic_5kg
                weight -= PLATE5_KG
            } else if(weight >= PLATE6_KG) {
                resourceIdToSet = R.drawable.ic_2_5kg
                weight -= PLATE6_KG
            } else if(weight >= PLATE7_KG) {
                resourceIdToSet = R.drawable.ic_1_25kg
                weight -= PLATE7_KG
            } else {
                break // TODO display some additional weight needed message to the user
            }

            val imageView = ImageView(this)
            previousPlateId = plateId
            plateId = View.generateViewId()
            imageView.id = plateId

            /* setup image width and height */
            imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            imageView.setImageResource(resourceIdToSet)
            constraintLayout.addView(imageView)

            /* constraints */
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            /* connect image to top and bottom of the barbell */
            constraintSet.connect(imageView.id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM)
            constraintSet.connect(imageView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP)

            /* if the first slot is empty we need a larger margin, and to connect to the barbell not the plate in front */
            if (firstSlotEmpty) {
                constraintSet.connect(
                    imageView.id,
                    ConstraintSet.START,
                    R.id.plate_start_guideline,
                    ConstraintSet.START,
                    FIRST_SLOT_MARGIN
                )
                firstSlotEmpty = false
            } else {
                constraintSet.connect(
                    imageView.id,
                    ConstraintSet.START,
                    previousPlateId,
                    ConstraintSet.END,
                    SLOT_MARGIN
                )
            }

            // apply and pray to the lord that everything is connected properly
            constraintSet.applyTo(constraintLayout)
        }
    }

    private fun updateBarbellLB(){
        val weightEditText = findViewById<EditText>(R.id.barbell_weight_edittext);
        val constraintLayout = findViewById<ConstraintLayout>(R.id.barbell_plates_constraint_layout)
        var resourceIdToSet: Int
        var previousPlateId: Int
        var plateId = 0
        var firstSlotEmpty = true

        var weight = try { weightEditText.text.toString().toDouble() } catch (e: NumberFormatException) { Log.d("updateBarbellLB", e.stackTrace.toString()); return }

        /* make sure we don't have any plates on there currently, in case we are removing weight */
        constraintLayout.removeAllViews()

        /* make sure too much doesn't crash the program */
        if(weight > MAX_WEIGHT_LB) {
            weightEditText.setText(MAX_WEIGHT_LB.toString())
            return
        }

        /* adjust for the weight of the bar */
        weight -= BARBELL_WEIGHT_LB

        /* adjust for just showing one half of the bar */
        weight /= 2

        while(weight > 0) {
            // first determine the icon we want to use
            if(weight >= PLATE1_LB && use25KG) {
                resourceIdToSet = R.drawable.ic_25kg
                weight -= PLATE1_LB
            } else if(weight >= PLATE2_LB) {
                resourceIdToSet = R.drawable.ic_20kg
                weight -= PLATE2_LB
            } else if (weight >= PLATE3_LB) {
                resourceIdToSet = R.drawable.ic_15kg
                weight -= PLATE3_LB
            } else if (weight >= PLATE4_LB) {
                resourceIdToSet = R.drawable.ic_10kg
                weight -= PLATE4_LB
            } else if(weight >= PLATE5_LB) {
                resourceIdToSet = R.drawable.ic_5kg
                weight -= PLATE5_LB
            } else if(weight >= PLATE6_LB) {
                resourceIdToSet = R.drawable.ic_2_5kg
                weight -= PLATE6_LB
            } else if(weight >= PLATE7_LB) {
                resourceIdToSet = R.drawable.ic_1_25kg
                weight -= PLATE7_LB
            } else {
                break // TODO display some additional weight needed message to the user
            }

            val imageView = ImageView(this)
            previousPlateId = plateId
            plateId = View.generateViewId()
            imageView.id = plateId

            /* setup image width and height */
            imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            imageView.setImageResource(resourceIdToSet)
            constraintLayout.addView(imageView)

            /* constraints */
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            /* connect image to top and bottom of the barbell */
            constraintSet.connect(imageView.id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM)
            constraintSet.connect(imageView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP)

            /* if the first slot is empty we need a larger margin, and to connect to the barbell not the plate in front */
            if (firstSlotEmpty) {
                constraintSet.connect(
                    imageView.id,
                    ConstraintSet.START,
                    R.id.plate_start_guideline,
                    ConstraintSet.START,
                    FIRST_SLOT_MARGIN
                )
                firstSlotEmpty = false
            } else {
                constraintSet.connect(
                    imageView.id,
                    ConstraintSet.START,
                    previousPlateId,
                    ConstraintSet.END,
                    SLOT_MARGIN
                )
            }

            // apply and pray to the lord that everything is connected properly
            constraintSet.applyTo(constraintLayout)
        }
    }
}
