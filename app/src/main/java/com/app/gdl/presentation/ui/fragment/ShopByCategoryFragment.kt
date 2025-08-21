package com.app.gdl.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.app.gdl.R
import com.app.gdl.data.model.Category
import com.app.gdl.data.model.ProductListItem
import com.app.gdl.databinding.FragmentTileshopbycategoryBinding
import com.app.gdl.presentation.ui.adapters.FeatureAdapter
import com.app.gdl.presentation.ui.adapters.ShopByCategoryAdapter
import com.app.gdl.utils.SharedPref
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ShopByCategoryFragment : Fragment(), FeatureAdapter.OnProductClickListener {


    private var _binding: FragmentTileshopbycategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var shopByCategoryAdapter: ShopByCategoryAdapter
    private lateinit var prefs: SharedPref

    companion object {
        private const val CUSTOMERID = "custId"

        fun newInstance(custId: String): ShopByCategoryFragment {
            return ShopByCategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(CUSTOMERID, custId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SharedPref(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTileshopbycategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserver()
    }

    private fun setObserver() {
        val savedJson = prefs.getString("CATEGORY_LIST", "")
        val categoryType = object : TypeToken<List<Category>>() {}.type
        val savedList: List<Category> = Gson().fromJson(savedJson, categoryType)
        shopByCategoryAdapter = ShopByCategoryAdapter(this)
        binding.shopbycategoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = shopByCategoryAdapter
        }
        shopByCategoryAdapter.submitList(savedList)
    }

    override fun onProductClicked(
        customlist: String,
        position: Int,
        categoryId: String,
        categoryName: String,
        topBanners: List<ProductListItem>
    ) {
        val fragment =
            ProductByCategoryFragment.newInstance(
                customlist,
                position,
                categoryId,
                categoryName,
                topBanners
            )
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }
}
