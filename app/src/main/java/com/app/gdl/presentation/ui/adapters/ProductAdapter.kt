package com.app.gdl.presentation.ui.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.Category
import com.app.gdl.data.model.ProductItem
import com.app.gdl.databinding.RowProductlistBinding
import com.app.gdl.presentation.ui.activity.ProductByCategoryDetailsActivity
import com.app.gdl.presentation.ui.activity.ShoppingCartActivity
import com.bumptech.glide.Glide


class ProductAdapter (
): RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

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
        Log.d("productList", "onBindViewHolder: " + productList.size)
        with(holder.binding) {
            productTitle.text = product.CustomName.value ?: "Unnamed"
            productWeight.text =
                "${product.DimensionWeight.value ?: 0.0} ${product.WeightUOM.value ?: ""}"

            imageBaseUrl = imageBasePath  + product.InventoryID.value + "/images/"
            val imageUrl = "$imageBaseUrl${product.ImageUrl.value ?: ""}"
            Log.d("TAG", "onBindViewHolder: "+imageUrl)
            Glide.with(productImage.context)
                .load(imageUrl)
               // .placeholder(com.app.gdl.R.drawable.featureimage) // Add placeholder
                .into(productImage)
            addToCartButton.setOnClickListener {
                val intent = Intent(root.context, ShoppingCartActivity::class.java)
                root.context.startActivity(intent)
            }
            constraintDetails.setOnClickListener {
                val intent = Intent(root.context, ProductByCategoryDetailsActivity::class.java)
                intent.putExtra("inventory_id", product.InventoryID.value)
                root.context.startActivity(intent)
            }

        }
    }

    override fun getItemCount() = productList.size
}

