package com.app.gdl.presentation.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.data.model.CartItem
import com.app.gdl.data.model.PriceItem
import com.app.gdl.data.model.ProductItem
import com.app.gdl.databinding.RowProductlistBinding
import com.app.gdl.presentation.ui.activity.SignInActivity
import com.app.gdl.presentation.ui.activity.SignUpActivity
import com.app.gdl.presentation.ui.adapters.ProductAdapter.OnProductListener
import com.app.gdl.utils.AuthPromptDialog
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.SharedPref
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Currency

class PopularItemsAdapter(
    private val listener: OnProductListener,
    private val addToCartListener: ProductAdapter.AddToCartListener,
) : RecyclerView.Adapter<PopularItemsAdapter.ViewHolder>() {

    private var productList = listOf<ProductItem>()
    private var imageBasePath: String = ""
    private var imageBaseUrl: String = ""
    lateinit var prefs: SharedPref
    lateinit var context: Context
    private var priceMap: Map<String, Double> = emptyMap() // InventoryID -> price

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
    ): PopularItemsAdapter.ViewHolder {
        prefs = SharedPref(parent.context)
        context = parent.context
        val binding =
            RowProductlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val layoutParams = ViewGroup.MarginLayoutParams(
            (screenWidth * 0.40).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.marginEnd = (8 * Resources.getSystem().displayMetrics.density).toInt()

        binding.root.layoutParams = layoutParams
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        with(holder.binding) {
            prefs.addInventoryId(context, product.InventoryID.value)
            productTitle.text = product.CustomName.value ?: "Unnamed"
            productWeight.text = product.BaseUOM.value
            val price = product.item_price?.value

            if (price != null) {
                currentPrice.text = formatCurrency(price)
                currentPrice.visibility = View.VISIBLE
            }

            imageBaseUrl = imageBasePath + product.InventoryID.value + "/images/"
            val imageUrl = "$imageBaseUrl${product.ImageUrl.value ?: ""}"
            Glide.with(productImage.context)
                .load(imageUrl)
            .placeholder(R.drawable.default_placeholder) // Add placeholder
                .into(productImage)
            addToCartButton.setOnClickListener {
                if (prefs.isLoggedIn) {
                    val selectedItem = CartItem(
                        inventoryId = product.InventoryID.value,
                        name = productTitle.text.toString(),
                        imageUrl = imageUrl,
                        unit = product.WeightUOM.value,
                        quantity = 1,
                        pricePerUnit = product.item_price.value,
                        BaseUOM = product.BaseUOM.value,
                        category = product.CustomCategory.value
                    )
                   addToCartListener.addToCartClicked(selectedItem)

                    Toast.makeText(context, "Added ${product.CustomName.value.toString()} to cart", Toast.LENGTH_SHORT).apply {
                        setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
                        show()
                    }
                } else {
                    AuthPromptDialog(
                        activity = context,
                        txtString = "Please log in/sign up to add items to your cart",
                        onRegisterClicked = {
                            context.startActivity(Intent(context, SignUpActivity::class.java))
                        },
                        onSignInClicked = {
                            context.startActivity(Intent(context, SignInActivity::class.java))
                        }
                    ).show()
                }

            }
            constraintDetails.setOnClickListener {
                val inventoryId = product.InventoryID.value
                if (inventoryId.isNotEmpty()) {
                    Log.d("inventory_id", "Clicked:PopularItemsAdapter $inventoryId")
                    listener.onProductDataClicked(inventoryId)
                } else {
                    Log.w("inventory_id", "Inventory ID is null or empty")
                }
            }
        }
    }

    fun formatCurrency(amount: Double, currencyCode: String = "KES"): String {
        val formatter = NumberFormat.getCurrencyInstance()
        formatter.currency = Currency.getInstance(currencyCode)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        return formatter.format(amount).replace(formatter.currency.symbol, currencyCode + " ")
    }

    override fun getItemCount() = productList.size
}