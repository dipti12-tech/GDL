package com.app.gdl.presentation.ui.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.CartItem
import com.app.gdl.data.model.ProductItem
import com.app.gdl.databinding.RowProductlistBinding
import com.app.gdl.utils.CartManager
import com.bumptech.glide.Glide

class ProductAdapter(
    private val listener: OnProductListener,
    private val addtocartlistener: AddToCartListener
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var productList = listOf<ProductItem>()
    private var imageBasePath: String = ""
    private var imageBaseUrl: String = ""

    fun submitData(list: List<ProductItem>, imgPath: String) {
        productList = list
        imageBasePath = imgPath
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowProductlistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductAdapter.ViewHolder {
        val binding =
            RowProductlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        Log.d("productList", "PRODUCT: " + productList.size)
        with(holder.binding) {
            Log.d("item_price", "onBindViewHolder: " + product.item_price.toString())
            productTitle.text = product.CustomName.value ?: "Unnamed"
            productWeight.text =
                "${product.DimensionWeight.value ?: 0.0} ${product.WeightUOM.value ?: ""}"
            currentPrice.text = String.format("KES %.2f", product.item_price)

            imageBaseUrl = imageBasePath + product.InventoryID.value + "/images/"
            val imageUrl = "$imageBaseUrl${product.ImageUrl.value ?: ""}"
            Log.d("TAG", "onBindViewHolder: " + imageUrl)
            Glide.with(productImage.context)
                .load(imageUrl)
                // .placeholder(com.app.gdl.R.drawable.featureimage) // Add placeholder
                .into(productImage)
            addToCartButton.setOnClickListener {
                val selectedItem = CartItem(
                    inventoryId = product.InventoryID.value,
                    name = productTitle.text.toString(),
                    imageUrl = imageUrl,
                    unit = product.WeightUOM.value,
                    quantity = 1,
                    pricePerUnit = product.item_price,
                    category = product.CustomCategory.value
                )
                CartManager.addItem(selectedItem)
                addtocartlistener.addToCartClicked()
            }
            constraintDetails.setOnClickListener {
                /* val intent = Intent(root.context, ProductByCategoryDetailsActivity::class.java)
                 intent.putExtra("inventory_id", product.InventoryID.value)
                 root.context.startActivity(intent)*/
                Log.d("inventory_id", "onBindViewHolder: " + product.InventoryID.value)
                val inventoryId = product.InventoryID.value
                if (inventoryId.isNotEmpty()) {
                    Log.d("inventory_id", "Clicked: $inventoryId")
                    listener.onProductDataClicked(inventoryId)
                } else {
                    Log.w("inventory_id", "Inventory ID is null or empty")
                }

            }

        }
    }

    interface OnProductListener {
        fun onProductDataClicked(inventoryId: String)
    }

    interface AddToCartListener {
        fun addToCartClicked()
    }

    override fun getItemCount() = productList.size
}

