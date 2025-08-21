package com.app.gdl.presentation.ui.fragment

import MyOrderPlacedSuccessListAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gdl.data.model.CartItem
import com.app.gdl.databinding.FragmentOrderplacedBinding
import com.app.gdl.presentation.ui.activity.MainActivity
import com.app.gdl.presentation.viewmodel.MyOrdersViewModel
import com.app.gdl.utils.SharedPref
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderPlacedSuccessFragment : Fragment() {

    private var _binding: FragmentOrderplacedBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPref: SharedPref
    private val viewModel: MyOrdersViewModel by viewModels()

    companion object {
        fun newInstance(
            orderId: String,
            totalCost: String,
            custId: String,
            items: List<CartItem>,
        ) = OrderPlacedSuccessFragment().apply {
            arguments = Bundle().apply {
                putString("orderId", orderId)
                putString("totalCost", totalCost)
                putString("custId", custId)
                putSerializable("items", ArrayList(items))

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderplacedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = SharedPref(requireContext())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI() {
        if (sharedPref.isLoggedIn) {
            binding.llCreateAccount.visibility = View.GONE
        }
        if (sharedPref.userAdrress != null) {
            binding.shippingaddress.text = sharedPref.userAdrress

        }


        val orderId = arguments?.getString("orderId")
        val totalCost = arguments?.getString("totalCost")
        val custId = arguments?.getString("custId")

        binding.orderplaced.text = """
        Standard Shipping
        Arrives by: Will be updated
        $totalCost
        Payment: Cash on delivery
        Order #$orderId
    """.trimIndent()

        binding.btnContinue.setOnClickListener {
            startActivity(Intent(requireActivity(), MainActivity::class.java))
        }

        val cartItems = arguments?.getSerializable("items") as? ArrayList<CartItem>
        cartItems?.let {
            binding.recyclerItems.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = MyOrderPlacedSuccessListAdapter(it)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
