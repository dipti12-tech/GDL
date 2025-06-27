package com.app.gdl.presentation.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.Category
import com.app.gdl.databinding.RowFeaturelistBinding
import com.bumptech.glide.Glide

class FeatureAdapter(private val listener: OnProductClickListener
) :
    RecyclerView.Adapter<FeatureAdapter.ViewHolder>() {

    private var categoryList = listOf<Category>()
    private  var url: String =""
    fun submitList(list: List<Category>) {
        categoryList = list
        notifyDataSetChanged()
    }
    inner class ViewHolder(val binding: RowFeaturelistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowFeaturelistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        with(holder.binding) {
            featureName.text = category.category_name
            featureDescription.text = category.category_desc
            url=category.category_img_path.replace(".jpeg","_large.jpeg")
            Log.d("Cat", "PATH: "+url)

            Glide.with(featureItem.context)
                .load(url)
                .into(featureItem)

            btnShopNow.setOnClickListener {
              /*  val intent = Intent(root.context, ProductByCategoryActivity::class.java)
                intent.putExtra("categoryId", category.cat_id.toString())
                intent.putExtra("categoryName", category.category_name)
                root.context.startActivity(intent)*/
                listener.onProductClicked(category.cat_id.toString(), category.category_name)
            }
        }
    }
    interface OnProductClickListener {
        fun onProductClicked(categoryId: String, categoryName: String)
    }


    override fun getItemCount() = categoryList.size
}

