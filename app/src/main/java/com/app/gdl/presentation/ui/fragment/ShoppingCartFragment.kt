package com.app.gdl.presentation.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.R
import com.app.gdl.data.model.OrderItem
import com.app.gdl.data.model.OrderRequest
import com.app.gdl.databinding.ActivityShoppingcartBinding
import com.app.gdl.presentation.ui.activity.MainActivity
import com.app.gdl.presentation.ui.activity.MapPickerActivity
import com.app.gdl.presentation.ui.activity.SignUpActivity
import com.app.gdl.presentation.ui.adapters.CartAdapter
import com.app.gdl.presentation.viewmodel.OrderViewModel
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.SharedPref
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ShoppingCartFragment : Fragment() {

    private var _binding: ActivityShoppingcartBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CartAdapter
    private val viewModel: OrderViewModel by viewModels()
    lateinit var prefs: SharedPref
    private var finalAddress: String? = null
    private var headingAddress: String? = null
    private var addressType: String = ""
    var lat: Double = 0.0
    var lng: Double = 0.0
    var customerCity: String = ""
    var grandtotal: String = ""
    var orderId: String = ""
    private val mapResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val address = it.data?.getStringExtra("address")
                headingAddress = it.data?.getStringExtra("headingAddress")
                finalAddress = address
                addressType = it.data?.getStringExtra("addressType").toString()
                lat = it.data?.getDoubleExtra("lat", 0.0) ?: 0.0
                lng = it.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            }
        }

    companion object {
        fun newInstance() = ShoppingCartFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SharedPref(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityShoppingcartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recycleviewUOM.layoutManager = LinearLayoutManager(requireContext())

        (activity as? CartAdapter.CartItemCountListener)?.onCartItemCountChanged(CartManager.getItems().size)
        if (CartManager.getItems().isEmpty()) {
            binding.selectionItems.visibility = View.GONE
            binding.btnEmptyCart.visibility = View.GONE
            binding.addressCard.visibility = View.GONE
        } else {
            binding.selectionItems.text = "${CartManager.getItems().size}  Selections"
            binding.btnEmptyCart.visibility = View.VISIBLE
            binding.addressCard.visibility = View.VISIBLE


        }
        if (prefs.isLoggedIn) {
            binding.llCreateAccount.visibility = View.GONE
        }
        binding.tvCurrent.text = "Shopping Cart"
        binding.tvHome.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.headerSignup.setOnClickListener {
            startActivity(Intent(context, SignUpActivity::class.java))
        }
        binding.imgLogo.load("file:///android_asset/artoftech_logo.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        binding.imgRightarrow.load("file:///android_asset/triangle_right.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        binding.btnContinueShopping.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.putExtra("addressUser", "")
            intent.putExtra("from", "shop")
            startActivity(intent)
        }

        adapter = CartAdapter(
            CartManager.getItems().toMutableList(),
            object : CartAdapter.CartTotalListener {
                override fun onCartTotalCalculated(subtotal: Double, grandTotal: Double) {
                    binding.subTotal.text = "Sub-Total   KES %.2f".format(subtotal)
                    binding.grandTotal.text = "Grand Total   KES %.2f".format(grandTotal)
                    grandtotal = "Grand Total   KES %.2f".format(grandTotal)
                }
            },
            object : CartAdapter.CartItemCountListener {
                override fun onCartItemCountChanged(count: Int) {
                    binding.selectionItems.text = "$count  Selections"
                    (activity as? CartAdapter.CartItemCountListener)?.onCartItemCountChanged(count)
                    if (CartManager.getItems().isEmpty()) {
                        binding.selectionItems.visibility = View.GONE
                        binding.btnEmptyCart.visibility = View.GONE
                        binding.addressCard.visibility = View.GONE

                    } else {
                        binding.selectionItems.text = "${CartManager.getItems().size}  Selections"
                        binding.btnEmptyCart.visibility = View.VISIBLE
                        binding.addressCard.visibility = View.VISIBLE


                    }
                }
            }
        )

        binding.recycleviewUOM.adapter = adapter

        binding.btnEmptyCart.setOnClickListener {
            adapter.emptyData()
            binding.selectionItems.visibility = View.GONE
            binding.btnEmptyCart.visibility = View.GONE
            binding.addressCard.visibility = View.GONE

        }
        if (prefs.userAdrress != null) {
            binding.tvLocality.text = prefs.userAdrress
        }
        binding.btnChangeLocation.setOnClickListener {
            val intent = Intent(requireActivity(), MapPickerActivity::class.java)
            mapResultLauncher.launch(intent)
        }
        binding.btnCheckout.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE

            val request = createOrderRequest()
            viewModel.placeOrder(request)
            val json = Gson().toJson(request)
        }
        viewModel.orderResponse.observe(viewLifecycleOwner) { result ->
            result.onSuccess { orderResponse ->

                binding.progressBar.visibility = View.GONE

                if (orderResponse.acu_order_number.toInt() != 0) {
                    orderId = orderResponse.acu_order_number
                } else {
                    orderId = orderResponse.cfa_order_id
                }
                if (orderResponse.status != 0) {
                    val totalCost = grandtotal

                    binding.selectionItems.visibility = View.GONE
                    binding.btnEmptyCart.visibility = View.GONE
                    binding.addressCard.visibility = View.GONE

                    val fragment = OrderPlacedSuccessFragment.newInstance(
                        orderId,
                        totalCost,
                        prefs.custid.toString(),
                        CartManager.getItems()
                    )
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment)
                        .addToBackStack(null)
                        .commit()
                    CartManager.clearCart()
                    adapter.emptyData()
                } else {
                    Toast.makeText(requireContext(), " Order Failed ", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun createOrderRequest(): OrderRequest {
        val customerData = prefs.getCustomerDetailsFromPrefs(requireContext())
        val customerId = customerData?.customer_id
        prefs.custid = customerId.toString()
        val customerName = customerData?.first_name + customerData?.last_name
        val customerAddress = customerData?.address?.firstOrNull { it.default == 1 }?.text
            ?: customerData?.address?.firstOrNull()?.text.orEmpty()
        customerCity = prefs.cityForOrder.toString()

        val customerPhone = customerData?.phone
        val orderDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val orderItems = CartManager.getItems().map {
            OrderItem(
                InventoryID = it.inventoryId,
                ProductName = it.name,
                OrderQty = it.quantity,
                UnitPrice = it.pricePerUnit
            )
        }

        return OrderRequest(
            customer_id = customerId ?: 0,
            customer_name = customerName ?: "",
            customer_address = customerAddress.toString(),
            customer_city = customerCity.toString(),
            customer_phone = customerPhone.toString(),
            warehouse = prefs.near_warehouse.toString(),
            price_class = prefs.default_price.toString(),
            order_date = orderDate,
            order_details = orderItems
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
