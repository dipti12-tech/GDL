package com.app.gdl.presentation.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.data.model.CartItem
import com.app.gdl.data.model.Product
import com.app.gdl.data.model.ProductItem
import com.app.gdl.databinding.RowProductlistBinding
import com.app.gdl.presentation.ui.activity.SignInActivity
import com.app.gdl.presentation.ui.activity.SignUpActivity
import com.app.gdl.utils.AuthPromptDialog
import com.app.gdl.utils.SharedPref
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Currency

class ProductAdapter(
    private val listener: OnProductListener,
    private val addtocartlistener: AddToCartListener
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var productList = listOf<ProductItem>()
    private var productCustomList = listOf<Product>()

    private var imageBasePath: String = ""
    private var fromCustomList: String = ""
    lateinit var prefs: SharedPref
    lateinit var context: Context

    fun submitData(list: List<ProductItem>, imgPath: String) {
        productList = list
        imageBasePath = imgPath
        notifyDataSetChanged()
    }

    fun submitCustomData(fromCustomlist: String, list: List<Product>, imgPath: String) {
        this.fromCustomList = fromCustomlist
        productCustomList = list
        imageBasePath = imgPath
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowProductlistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductAdapter.ViewHolder {
        prefs = SharedPref(parent.context)
        context = parent.context
        val binding =
            RowProductlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /* // Get screen width
         val displayMetrics = parent.context.resources.displayMetrics
         val screenWidth = displayMetrics.widthPixels

         // Set item width to ~85% of screen width
         val layoutParams = binding.root.layoutParams
         layoutParams.width = (screenWidth * 0.85).toInt()
         binding.root.layoutParams = layoutParams*/
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding

        if (fromCustomList == "FROMCustomList" && productCustomList.isNotEmpty()) {
            val item = productCustomList[position]

            with(binding) {
                productTitle.text = item.CustomName?.value ?: "Unnamed"
                productWeight.text = "${item.BaseUOM?.value}"
                item.item_price?.value?.let { price ->
                    currentPrice.text = formatCurrency(price)
                    currentPrice.visibility = View.VISIBLE
                }

                val imageUrl = buildImageUrl(item.InventoryID?.value, item.ImageUrl?.value)

                Glide.with(productImage.context).load(imageUrl)
                    .placeholder(R.drawable.default_placeholder).into(productImage)

                addToCartButton.setOnClickListener {
                    if (prefs.isLoggedIn) {
                        val cartItem = CartItem(
                            inventoryId = item.InventoryID?.value.orEmpty(),
                            name = productTitle.text.toString(),
                            imageUrl = imageUrl,
                            unit = item.WeightUOM?.value.orEmpty(),
                            quantity = 1,
                            pricePerUnit = item.item_price?.value ?: 0.0,
                            BaseUOM = item.BaseUOM?.value.toString(),
                            category = item.CustomCategory?.value.orEmpty()
                        )
                        addtocartlistener.addToCartClicked(cartItem)
                        Toast.makeText(
                            context,
                            "Added ${item.CustomName?.value.orEmpty()} to cart",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showAuthPromptDialog()
                    }
                }

                constraintDetails.setOnClickListener {
                    val inventoryId = item.InventoryID?.value.orEmpty()
                    if (inventoryId.isNotEmpty()) {
                        Log.d("inventory_id", "Clicked:ProductAdapter $inventoryId")
                        listener.onProductDataClicked(inventoryId)
                    } else {
                        Log.w("inventory_id", "Inventory ID is null or empty")
                    }
                }
            }

        } else if (productList.isNotEmpty()) {
            val item = productList[position]

            with(binding) {
                productTitle.text = item.CustomName.value ?: "Unnamed"
                productWeight.text = "${item.BaseUOM?.value}"

                item.item_price.value.let { price ->
                    currentPrice.text = formatCurrency(price)
                    currentPrice.visibility = View.VISIBLE
                }

                val imageUrl = buildImageUrl(item.InventoryID.value, item.ImageUrl.value)

                Glide.with(productImage.context).load(imageUrl)
                    .placeholder(R.drawable.default_placeholder).into(productImage)

                addToCartButton.setOnClickListener {
                    if (prefs.isLoggedIn) {
                        val cartItem = CartItem(
                            inventoryId = item.InventoryID.value,
                            name = productTitle.text.toString(),
                            imageUrl = imageUrl,
                            unit = item.WeightUOM.value,
                            quantity = 1,
                            pricePerUnit = item.item_price?.value ?: 0.0,
                            BaseUOM = item.BaseUOM.value,
                            category = item.CustomCategory.value
                        )
                        addtocartlistener.addToCartClicked(cartItem)

                        Toast.makeText(
                            context,
                            "Added ${item.CustomName.value} to cart",
                            Toast.LENGTH_SHORT
                        ).apply {
                            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
                            show()
                        }
                    } else {
                        showAuthPromptDialog()
                    }
                }

                constraintDetails.setOnClickListener {
                    val inventoryId = item.InventoryID.value
                    if (inventoryId.isNotEmpty()) {
                        Log.d("inventory_id", "Clicked:ProductAdapter $inventoryId")
                        listener.onProductDataClicked(inventoryId)
                    } else {
                        Log.w("inventory_id", "Inventory ID is null or empty")
                    }
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

    private fun buildImageUrl(inventoryId: String?, imageName: String?): String {
        return "$imageBasePath${inventoryId.orEmpty()}/images/${imageName.orEmpty()}"
    }

    private fun showAuthPromptDialog() {
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

    interface OnProductListener {
        fun onProductDataClicked(inventoryId: String)
    }

    interface AddToCartListener {
        fun addToCartClicked(item: CartItem)
    }

    override fun getItemCount(): Int {
        return if (fromCustomList == "FROMCustomList" && productCustomList.isNotEmpty()) {
            productCustomList.size
        } else {
            productList.size
        }
    }

}

