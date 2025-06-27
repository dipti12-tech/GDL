package com.app.gdl.presentation.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.CartItem
import com.app.gdl.databinding.RowShoppingcartBinding
import com.app.gdl.utils.CartManager
import com.bumptech.glide.Glide

class CartAdapter(private val items: MutableList<CartItem>, private val totalListener: CartTotalListener,
                  private val itemCountListener: CartItemCountListener
)
: RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var setQuantity = 1

    inner class CartViewHolder(val binding: RowShoppingcartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding =
            RowShoppingcartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        // holder.binding.selectedCategory.text = item.category
        holder.binding.selectedTitle.text = item.name
        val totalPrice = item.quantity * item.pricePerUnit
        Glide.with(holder.binding.selectedImg.context)
            .load(item.imageUrl)
            .into(holder.binding.selectedImg)

        Log.d("item.quantity", "onBindViewHolder: " + item.quantity)
        //quantity=item.quantity
        setQuantity = item.quantity
        holder.binding.tvQuantity.text = "${setQuantity} "

        holder.binding.selectedQuantity.text =
            "KES $totalPrice \n ${item.pricePerUnit} / ${item.unit}  "
        calculateTotals()

        /* holder.binding.btnDelete.setOnClickListener {
            CartManager.removeItem(item.inventoryId, item.unit)
            notifyItemRemoved(position)
        }*/
        holder.binding.btnDelete.setOnClickListener {
            CartManager.removeItem(item.inventoryId, item.unit)

            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                items.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, items.size)
                calculateTotals()
                Log.d("CartAdapter", "Updating item count: ${items.size}")
                itemCountListener.onCartItemCountChanged(items.size)

            }
        }


        holder.binding.btnPlus.setOnClickListener { incrementQuantity(holder, item) }
        holder.binding.btnMinus.setOnClickListener { decrementQuantity(holder, item) }

    }

    /* private fun incrementQuantity(holder: CartViewHolder, item: CartItem) {
        if (item.quantity < 10000) {
            setQuantity++
            //price will also increase setQuantity * price
            val totalPrice = setQuantity * item.pricePerUnit
            holder.binding.selectedQuantity.text = "KES $totalPrice \n ${item.pricePerUnit} / ${item.unit}  "
        }
        holder.binding.tvQuantity.text = "$setQuantity"
        calculateTotals()

    }

    private fun decrementQuantity(holder: CartViewHolder, item: CartItem) {
        if (setQuantity > 1) {
            setQuantity--
            val totalPrice = setQuantity * item.pricePerUnit
            holder.binding.selectedQuantity.text = "KES $totalPrice \n ${item.pricePerUnit} / ${item.unit}  "
        }*/
    private fun incrementQuantity(holder: CartViewHolder, item: CartItem) {
        if (item.quantity < 1000) {
            item.quantity++
            val totalPrice = item.quantity * item.pricePerUnit
            holder.binding.selectedQuantity.text =
                "KES $totalPrice \n ${item.pricePerUnit} / ${item.unit}"
            holder.binding.tvQuantity.text = "${item.quantity}"
            calculateTotals()
        }
    }

    private fun decrementQuantity(holder: CartViewHolder, item: CartItem) {
        if (item.quantity > 1) {
            item.quantity--
            val totalPrice = item.quantity * item.pricePerUnit
            holder.binding.selectedQuantity.text =
                "KES $totalPrice \n ${item.pricePerUnit} / ${item.unit}"
            holder.binding.tvQuantity.text = "${item.quantity}"
            calculateTotals()
        }



    holder.binding.tvQuantity.text = "$setQuantity"
        calculateTotals()

    }
    fun emptyData() {
        CartManager.clearCart()
        items.clear()
        notifyDataSetChanged()
        calculateTotals()
        Log.d("CartAdapter", "Updating item count: ${items.size}")
        itemCountListener.onCartItemCountChanged(0)

    }
    interface CartTotalListener {
        fun onCartTotalCalculated(subtotal: Double, grandTotal: Double)
    }
    private fun calculateTotals() {
        var subtotal = 0.0
        for (item in items) {
            subtotal += item.quantity * item.pricePerUnit
        }
        val grandTotal = subtotal // Add tax or delivery charge here if needed
        totalListener.onCartTotalCalculated(subtotal, grandTotal)
    }
    interface CartItemCountListener {
        fun onCartItemCountChanged(count: Int)
    }

    override fun getItemCount(): Int = items.size
}
