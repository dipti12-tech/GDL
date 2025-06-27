package com.app.gdl.presentation.ui.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.data.model.Category
import com.app.gdl.databinding.RowPastpopularlistBinding
import com.app.gdl.presentation.ui.activity.ProductByCategoryDetailsActivity
import com.app.gdl.presentation.ui.activity.ShoppingCartActivity
import com.app.gdl.presentation.ui.fragment.ProductByCategoryDetailsFragment
import com.bumptech.glide.Glide


class PastPopularAdapter :
    RecyclerView.Adapter<PastPopularAdapter.ViewHolder>() {

    private var categoryList = listOf<Category>()
    fun submitList(list: List<Category>) {
        categoryList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowPastpopularlistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PastPopularAdapter.ViewHolder {
        val binding =
            RowPastpopularlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        with(holder.binding) {
            productTitle.text = category.category_name
            Glide.with(productImage.context)
                .load(category.category_img_path)
                .into(productImage)

            addToCartButton.setOnClickListener {
              /*  val intent = Intent(root.context, ShoppingCartActivity::class.java)
                root.context.startActivity(intent)*/

               // addtocartlistener.addToCartClicked()



            }
            constraintDetails.setOnClickListener {
               /* val intent = Intent(root.context, ProductByCategoryDetailsActivity::class.java)
               root.context.startActivity(intent)*/
                /*val fragment = ProductByCategoryDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putString("inventory_id", product.InventoryID.value)
                    }
                }

                parentFragment.requireActivity()
                    .supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, fragment)
                    .addToBackStack(null)
                    .commit()*/
            }
        }
    }

    override fun getItemCount() = categoryList.size
}
