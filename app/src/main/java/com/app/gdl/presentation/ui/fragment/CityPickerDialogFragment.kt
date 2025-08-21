package com.app.gdl.presentation.ui.dialog

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.app.gdl.R
import com.app.gdl.data.model.Warehouse
import com.app.gdl.presentation.viewmodel.WarehouseViewModel
import com.app.gdl.utils.SharedPref

class CityPickerDialogFragment(
    private val cities: List<String>,
    private val prefs: SharedPref,
    private val warehousesList: List<Warehouse>,
    private val onCitySelected: (String) -> Unit
) : DialogFragment() {

    private val warehouseViewModel: WarehouseViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        val dialog = dialog ?: return
        val window = dialog.window ?: return

        val metrics = resources.displayMetrics
        val marginDp = 24
        val marginPx = (marginDp * metrics.density).toInt()
        val width = metrics.widthPixels - (marginPx * 2)

        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.layout_popup_city_picker, null)
        dialog.setContentView(view)

        val spinner = view.findViewById<Spinner>(R.id.spinnerCity)
        val cityList = if (cities.isEmpty()) prefs.savedCities else cities

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cityList)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    val selected = cityList[position]
                    Toast.makeText(requireContext(), "Selected: $selected", Toast.LENGTH_SHORT).show()
                    prefs.cityForOrder = selected
                    prefs.userAdrress = selected
                    prefs.citySelected = true
                    onCitySelected(selected)
                    warehouseViewModel.onCitySelected(selected,warehousesList)
                    dismiss()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        dialog.setCanceledOnTouchOutside(false)
        isCancelable = false
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.CENTER)

        return dialog
    }
}
