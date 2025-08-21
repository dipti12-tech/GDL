package com.app.gdl.presentation.ui.adapters

import ItemPrice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import java.text.NumberFormat
import java.util.Currency

class UomPriceAdapter(private val items: List<ItemPrice>) :
    RecyclerView.Adapter<UomPriceAdapter.UomViewHolder>() {

    inner class UomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtUom: TextView = itemView.findViewById(R.id.txtUom)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_productdetailsuom, parent, false)
        return UomViewHolder(view)
    }

    override fun onBindViewHolder(holder: UomViewHolder, position: Int) {
        val item = items[position]
        holder.txtUom.text = item.UOM?.value
        holder.txtPrice.text = item.Price?.value?.toDouble()?.let { formatCurrency(it) }
    }

    fun formatCurrency(amount: Double, currencyCode: String = "KES"): String {
        val formatter = NumberFormat.getCurrencyInstance()
        formatter.currency = Currency.getInstance(currencyCode)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        return formatter.format(amount).replace(formatter.currency.symbol, currencyCode + " ")
    }


    override fun getItemCount(): Int = items.size
}
