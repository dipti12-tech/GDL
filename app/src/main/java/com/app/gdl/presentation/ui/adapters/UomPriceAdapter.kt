package com.app.gdl.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.data.model.PriceItem

class UomPriceAdapter(private val items: List<PriceItem>) :
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
        holder.txtUom.text = item.UOM.value
        holder.txtPrice.text = "KES ${item.Price.value}"
    }

    override fun getItemCount(): Int = items.size
}
