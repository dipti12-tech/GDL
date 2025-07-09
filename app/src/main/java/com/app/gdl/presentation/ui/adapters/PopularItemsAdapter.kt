package com.app.gdl.presentation.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.data.model.CartItem
import com.app.gdl.data.model.PriceItem
import com.app.gdl.data.model.ProductItem
import com.app.gdl.databinding.RowProductlistBinding
import com.app.gdl.presentation.ui.activity.ProductByCategoryDetailsActivity
import com.app.gdl.presentation.ui.activity.ShoppingCartActivity
import com.app.gdl.presentation.ui.adapters.ProductAdapter.OnProductListener
import com.app.gdl.presentation.ui.fragment.ProductByCategoryDetailsFragment
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.SharedPref
import com.bumptech.glide.Glide

class PopularItemsAdapter (
    private val listener: OnProductListener,
    private val addToCartListener : ProductAdapter.AddToCartListener,
): RecyclerView.Adapter<PopularItemsAdapter.ViewHolder>() {

    private var productList = listOf<ProductItem>()
    private var imageBasePath: String = ""
    private var imageBaseUrl: String = ""
    lateinit var prefs : SharedPref
   lateinit var context : Context
    private var priceMap: Map<String, Double> = emptyMap() // InventoryID -> price

    fun submitData(list: List<ProductItem>, imgPath: String) {
        productList = list
        imageBasePath = imgPath
        notifyDataSetChanged()
    }
    fun setPriceMap(prices: List<PriceItem>) {
        priceMap = prices.associate { it.InventoryID.value to it.Price.value }
        notifyDataSetChanged()
    }
    inner class ViewHolder(val binding: RowProductlistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PopularItemsAdapter.ViewHolder {
        prefs = SharedPref(parent.context)
        context=parent.context
        val binding =
            RowProductlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        Log.d("productList", "POPULAR: " + productList.size)
        with(holder.binding) {
            prefs.addInventoryId(context,product.InventoryID.value )
            Log.d("productList", "POPULAR ITEMS ME inventory ids kya hai: " +product.InventoryID.value)

            productTitle.text = product.CustomName.value ?: "Unnamed"
            productWeight.text =
                "${product.DimensionWeight.value ?: 0.0} ${product.WeightUOM.value ?: ""}"
          //  currentPrice.text = String.format("KES %.2f", product.item_price)
            // Show price from filteredPriceItems if logged in
            val inventoryId = product.InventoryID.value
            val displayPrice = if (prefs.isLoggedIn && priceMap.containsKey(inventoryId)) {
                priceMap[inventoryId] ?: product.item_price
            } else {
                product.item_price
            }
            currentPrice.text = String.format("KES %.2f", displayPrice)
            Log.d("displayPrice", "displayPrice: "+displayPrice)

            imageBaseUrl = imageBasePath  + product.InventoryID.value + "/images/"
            val imageUrl = "$imageBaseUrl${product.ImageUrl.value ?: ""}"
            Log.d("TAG", "onBindViewHolder: "+imageUrl)
            Glide.with(productImage.context)
                .load(imageUrl)
                // .placeholder(com.app.gdl.R.drawable.featureimage) // Add placeholder
                .into(productImage)
            addToCartButton.setOnClickListener {
               /* val intent = Intent(root.context, ShoppingCartActivity::class.java)
                root.context.startActivity(intent)*/

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
                addToCartListener.addToCartClicked()

            }
            constraintDetails.setOnClickListener {

                Log.d("inventory_id", "onBindViewHolder: "+product.InventoryID.value)
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

    override fun getItemCount() = productList.size
}