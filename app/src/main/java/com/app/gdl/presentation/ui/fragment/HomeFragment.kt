package com.app.gdl.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gdl.databinding.FragmentHomeBinding
import com.app.gdl.presentation.ui.adapters.FeatureAdapter
import com.app.gdl.presentation.ui.adapters.PopularCategoryAdapter
import com.app.gdl.presentation.ui.adapters.ProductAdapter
import com.app.gdl.presentation.ui.adapters.ShopByCategoryAdapter
import com.app.gdl.presentation.viewmodel.CategoryViewModel
import com.app.gdl.presentation.viewmodel.PopularViewModel
import com.app.gdl.presentation.viewmodel.ProductViewModel
import com.app.gdl.presentation.viewmodel.ShopCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: CategoryViewModel by viewModels()
    private val shopviewModel: ShopCategoryViewModel by viewModels()
    private val popularviewModel: PopularViewModel by viewModels()
    private val productviewModel: ProductViewModel by viewModels()

    private lateinit var adapter: FeatureAdapter
    private lateinit var shopByCategoryAdapter: ShopByCategoryAdapter
    //private lateinit var PopularAdapter: PopularCategoryAdapter
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
        viewModel.categories.observe(viewLifecycleOwner) { response ->
            adapter.submitList(response.category_list)
        }

        shopviewModel.allcategories.observe(viewLifecycleOwner) { response ->
            Log.d("shopviewModelsize", "onViewCreated: response" + response.category_list.size)
            shopByCategoryAdapter.submitList(response.category_list)
        }
       /* popularviewModel.getpopularcategories.observe(viewLifecycleOwner) { response ->
            Log.d("response**", response.status.toString())
            PopularAdapter.submitList(response.category_list)
        }*/

        productviewModel.products.observe(viewLifecycleOwner) { response ->
            Log.d("response**PRODUCTs", response.list.size.toString()+response.s3_img_path+"status"+response.status)
            productAdapter.submitData(response.list,response.s3_img_path ?: "")
        }
        viewModel.fetchCategories()
        shopviewModel.fetchAllCategories()
        popularviewModel.fetchGetpopularCategories()
        productviewModel.fetchProducts()
    }

    fun setUi() {

        adapter = FeatureAdapter()
        binding.featureRecycleview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.featureRecycleview.adapter = adapter

        shopByCategoryAdapter = ShopByCategoryAdapter()
        binding.categoryRecyclerView.layoutManager = GridLayoutManager(activity, 3)
        binding.categoryRecyclerView.adapter = shopByCategoryAdapter

     /*   PopularAdapter = PopularCategoryAdapter()
        binding.productsRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.productsRecyclerView.adapter = PopularAdapter
*/

        //here add popular category product list depends on popular category
        productAdapter = ProductAdapter()
        binding.productsRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.productsRecyclerView.adapter = productAdapter
    }
}