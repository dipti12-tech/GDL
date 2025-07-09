package com.app.gdl.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gdl.R
import com.app.gdl.data.model.CartItem
import com.app.gdl.data.model.PriceItem
import com.app.gdl.data.model.User
import com.app.gdl.databinding.ActivityProductdetailsBinding
import com.app.gdl.presentation.ui.adapters.ImageSliderAdapter
import com.app.gdl.presentation.ui.adapters.ImageThumbnailAdapter
import com.app.gdl.presentation.ui.adapters.ProductAdapter
import com.app.gdl.presentation.ui.adapters.UomPriceAdapter
import com.app.gdl.presentation.viewmodel.DefaultPriceViewModel
import com.app.gdl.presentation.viewmodel.ProductDetailViewModel
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.SharedPref
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductByCategoryDetailsFragment : Fragment(), ProductAdapter.AddToCartListener{

    private var _binding: ActivityProductdetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()
    private val defaultPriceViewModel: DefaultPriceViewModel by viewModels()

    private var quantity = 1
    private var selectedUom: PriceItem? = null
    private var mainImageUrl: String = ""
    private var category:String =""
    private var priceItems: List<PriceItem> = emptyList()
     private lateinit var sharedPref :SharedPref
    var priceclass=""
    private  var customer: User? = null
    private var isProductDetailsLoaded = false
    private var isPricesLoaded = false

    companion object {
        fun newInstance(inventoryId: String) = ProductByCategoryDetailsFragment().apply {
            arguments = Bundle().apply {
                Log.d("inventoryId***", "inventoryId: $inventoryId")
                putString("inventory_id", inventoryId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityProductdetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref= SharedPref(requireContext())
        val inventoryId = arguments?.getString("inventory_id").orEmpty()
        Log.d("inventoryId", "ProductByCategoryDetailsFragment: $inventoryId")
        binding.progressBar.visibility = View.VISIBLE
        isProductDetailsLoaded = false
        isPricesLoaded = false

        /*binding.spinnerSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position) as String
                Log.d("SpinnerSelection", "Selected UOM: $selectedItem")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional
            }
        }*/
        customer = sharedPref.getCustomerFromPrefs(requireContext())
        priceclass = customer?.address?.get(0)?.price_class.toString()
        defaultPriceViewModel.defaultPrice.observe(viewLifecycleOwner) { response ->
            isPricesLoaded = true
            checkIfLoadingIsComplete()
            val groupedByInventoryID: Map<String, List<PriceItem>> = response.list.groupBy { it.InventoryID.value }
            val inventoryItems = groupedByInventoryID[inventoryId] ?: emptyList()

            val adapter = UomPriceAdapter(inventoryItems)
            binding.recycleviewUOM.layoutManager = GridLayoutManager(requireContext(), 2)
            binding.recycleviewUOM.adapter = adapter

            priceItems = groupedByInventoryID[inventoryId] ?: emptyList()

        //    val uomList = priceItems.map { it.UOM.value }
            val uomList = priceItems.map { it.UOM.value }.toSet().toList()

            if(uomList.isNotEmpty()){
              Log.d("uomList@@", "onViewCreated: "+uomList.size +uomList.get(0).toString())
              val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uomList)
              spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
              binding.spinnerSize.adapter = spinnerAdapter
          }

            binding.spinnerSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedUom = priceItems[position]
                    Log.d("SpinnerSelection", "Selected UOM: ${selectedUom?.UOM?.value}, Price: ${selectedUom?.Price?.value}")
                   // binding.tvQuantity.text = "$quantity  ${selectedUom?.UOM?.value}"
                   // quantity=0
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedUom = null
                }
            }

        }

       /* if(sharedPref.isLoggedIn==true && priceclass.isNotEmpty()){
            defaultPriceViewModel.fetchDefaultPrice(priceclass)
        }else{
            defaultPriceViewModel.fetchDefaultPrice("")
        }*/
        defaultPriceViewModel.fetchDefaultPrice("")

        viewModel.fetchProductDetail(inventoryId)

        binding.btnPlus.setOnClickListener { incrementQuantity(selectedUom?.UOM?.value) }
        binding.btnMinus.setOnClickListener { decrementQuantity(selectedUom?.UOM?.value) }

        binding.btnAddToCart.setOnClickListener {

            if(sharedPref.isLoggedIn){
                if (selectedUom == null) {
                    Log.w("AddToCart", "Please select a UOM first")
                    return@setOnClickListener
                }

                val selectedItem = CartItem(
                    inventoryId = inventoryId,
                    name = binding.tvTitle.text.toString(),
                    imageUrl = mainImageUrl,
                    unit =  binding.spinnerSize.selectedItem as String,
                    quantity = quantity,
                    pricePerUnit = selectedUom!!.Price.value,
                    category = category

                )
                Log.d("TAG", "onViewCreated: "+quantity)
                CartManager.addItem(selectedItem)
                addToCartClicked()
            }else{
                //show the popup must logged in
                Toast.makeText(requireContext(), "User is not Logged In", Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.products.observe(viewLifecycleOwner, Observer { response ->
            val item = response.details.firstOrNull() ?: return@Observer

            binding.tvTitle.text = item.CustomName?.value ?: "No Name"
            binding.tvDescription.text = item.CustomDescription?.value ?: "No Description"

            val basePath = response.s3_img_path
            val productId = item.InventoryID?.value ?: ""
            val mainImage = item.ImageUrl?.value
            val additionalImages = item.Images?.value ?: emptyList()

            Log.d("mainImage", "onViewCreated: "+mainImage)

            val allImages = mutableListOf<String>()
           /* mainImage?.let {
                allImages.add(it)
               // Log.d("additionalImages", "onViewCreated: "+additionalImages.)

                mainImageUrl = "$basePath$productId/images/$it"
            }*/

            allImages.addAll(additionalImages.filter { it.contains("_thumbnail") })

            val imageUrls = allImages.map { "$basePath$productId/images/$it" }

            val pagerAdapter = ImageSliderAdapter(imageUrls)
            binding.viewPagerImages.adapter = pagerAdapter

            binding.recyclerThumbnails.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            binding.recyclerThumbnails.adapter =
                ImageThumbnailAdapter(imageUrls.filter { it.contains("_thumbnail") }) { selectedImageUrl ->
                    val index = imageUrls.indexOf(selectedImageUrl)
                    if (index != -1) {
                        binding.viewPagerImages.setCurrentItem(index, true)
                    }
                }
            isProductDetailsLoaded = true
            checkIfLoadingIsComplete()
        })
    }

    private fun incrementQuantity(uom: String?) {
        if (quantity < 1000) {
            quantity++
        }
        binding.tvQuantity.text = "$quantity"
    }

    private fun decrementQuantity(uom: String?) {
        if (quantity > 1) {
            quantity--
        }
        binding.tvQuantity.text = "$quantity"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun checkIfLoadingIsComplete() {
        if (isProductDetailsLoaded && isPricesLoaded) {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun addToCartClicked() {
        val fragment = ShoppingCartFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }

}
