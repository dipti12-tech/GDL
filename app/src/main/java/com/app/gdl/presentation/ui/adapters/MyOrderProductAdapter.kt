package com.app.gdl.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.Order
import com.app.gdl.databinding.RowOrderHistoryBinding
import java.text.NumberFormat
import java.util.Currency

class MyOrderProductAdapter(
        private val products: Order
    ) : RecyclerView.Adapter<MyOrderProductAdapter.ProductViewHolder>() {

        inner class ProductViewHolder(val binding: RowOrderHistoryBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = RowOrderHistoryBinding.inflate(inflater, parent, false)
            return ProductViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val product = products.order_details[position]
            with(holder.binding) {
                tvProductName.text = product.ProductName?.value ?: " "
                tvProductQty.text = product.OrderQty.value.toString()
                tvProductPrice.text =formatCurrency(product.ItemTotal.value)

            }
        }

        override fun getItemCount(): Int = products.order_details.size
    fun formatCurrency(amount: Double, currencyCode: String = "KES"): String {
        val formatter = NumberFormat.getCurrencyInstance()
        formatter.currency = Currency.getInstance(currencyCode)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        return formatter.format(amount).replace(formatter.currency.symbol, currencyCode + " ")
    }
    }
