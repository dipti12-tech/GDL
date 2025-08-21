package com.app.gdl.presentation.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.ProductListItem
import com.app.gdl.databinding.RowFeaturelistBinding
import com.app.gdl.utils.SlotType
import com.bumptech.glide.Glide

class FeatureAdapter(
    private val listener: OnProductClickListener,
) : RecyclerView.Adapter<FeatureAdapter.ViewHolder>() {

    private var categoryList = listOf<ProductListItem>()

    fun submitList(list: List<ProductListItem>) {
        categoryList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowFeaturelistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowFeaturelistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        val slotType = SlotType.fromCode(category.slot)

        with(holder.binding) {
            featureName.text = category.list_name
            if (category.list_desc != null) {
                featureDescription.visibility = View.VISIBLE
                featureDescription.text = category.list_desc
            } else {
                featureDescription.visibility = View.INVISIBLE
            }
            val imageUrl = category.list_img_path.replace(".jpeg", "_large.jpeg")
            Glide.with(featureItem.context)
                .load(imageUrl)
                .into(featureItem)

            when (slotType) {
                SlotType.TBL -> {
                    root.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    Log.d("FeatureAdapter", "Slot: TBL - Showing left aligned")
                }

                SlotType.TBR -> {
                    root.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    Log.d("FeatureAdapter", "Slot: TBR - Showing right aligned")
                }

                SlotType.MS -> {
                    root.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    Log.d("FeatureAdapter", "Slot: MS - Showing main section")
                }

                SlotType.BBLS -> {
                    root.layoutDirection = View.LAYOUT_DIRECTION_LTR
                }

                SlotType.BBRS -> {
                    root.layoutDirection = View.LAYOUT_DIRECTION_RTL
                }

                SlotType.UNKNOWN -> {
                    root.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    Log.w("FeatureAdapter", "Slot unknown")
                }

            }

            btnShopNow.setOnClickListener {
                listener.onProductClicked(
                    "FROMCustomList",
                    position, category.list_id.toString(), category.list_name, categoryList
                )

            }
        }
    }

    interface OnProductClickListener {
        fun onProductClicked(
            customlist: String,
            position: Int,
            categoryId: String,
            categoryName: String,
            list: List<ProductListItem>
        )
    }

    override fun getItemCount(): Int = categoryList.size
}
