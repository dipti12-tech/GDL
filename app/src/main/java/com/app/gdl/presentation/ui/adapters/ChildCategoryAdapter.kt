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
import com.app.gdl.data.model.Product
import com.app.gdl.databinding.RowPoupularcategoriesBinding
import com.app.gdl.presentation.ui.activity.SignInActivity
import com.app.gdl.presentation.ui.activity.SignUpActivity
import com.app.gdl.utils.AuthPromptDialog
import com.app.gdl.utils.SharedPref
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Currency

class ChildCategoryAdapter(
    private val listener: OnProductListener,
    private val addtocartlistener: AddToCartListener
) : RecyclerView.Adapter<ChildCategoryAdapter.ViewHolder>() {

    private var productList = listOf<Product>()

    private var imageBasePath: String = ""
    lateinit var prefs: SharedPref
    lateinit var context: Context

    fun submitData(list: List<Product>, imgPath: String) {
        productList = list
        imageBasePath = imgPath
        notifyDataSetChanged()
    }


    inner class ViewHolder(val binding: RowPoupularcategoriesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildCategoryAdapter.ViewHolder {
        prefs = SharedPref(parent.context)
        context = parent.context
        val binding =
            RowPoupularcategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            if (productList.size != 0) {
                productTitle.text = product.CustomName?.value ?: "Unnamed"
                productWeight.text = "${product.BaseUOM?.value}"
                val price = product.item_price?.value
                if (price != null) {
                    currentPrice.text = formatCurrency(price)
                    currentPrice.visibility = View.VISIBLE
                }
                val imageUrl = "${product.ImageUrl?.value ?: ""}"
                Glide.with(productImage.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_placeholder)
                    .into(productImage)

                addToCartButton.setOnClickListener {
                    if (prefs.isLoggedIn) {
                        val cartItem = CartItem(
                            inventoryId = product.InventoryID?.value.toString(),
                            name = productTitle.text.toString(),
                            imageUrl = imageUrl,
                            unit = product.WeightUOM?.value.toString(),
                            quantity = 1,
                            pricePerUnit = product.item_price?.value ?: 0.0,
                            BaseUOM = product.BaseUOM?.value.toString(),
                            category = product.CustomCategory?.value.toString()
                        )
                        addtocartlistener.addToCartClicked(cartItem)

                        Toast.makeText(
                            context,
                            "Added ${product.CustomName?.value.toString()} to cart",
                            Toast.LENGTH_SHORT
                        ).apply {
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
                    val inventoryId = product.InventoryID?.value
                    if (inventoryId?.isNotEmpty() == true) {
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

    interface OnProductListener {
        fun onProductDataClicked(inventoryId: String)
    }

    interface AddToCartListener {
        fun addToCartClicked(cartItem: CartItem)
    }


    override fun getItemCount() = productList.size
}