package com.app.gdl.presentation.ui.fragment

import MyOrdersAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.R
import com.app.gdl.databinding.FragmentMyOrdersBinding
import com.app.gdl.presentation.viewmodel.MyOrdersViewModel
import com.app.gdl.utils.SharedPref
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyOrdersHistoryFragment : Fragment() {

    private var _binding: FragmentMyOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyOrdersViewModel by viewModels()
    var custId: String = ""
    lateinit var prefs: SharedPref

    companion object {
        private const val CUSTOMERID = "custId"

        fun newInstance(custId: String): MyOrdersHistoryFragment {
            return MyOrdersHistoryFragment().apply {
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
        _binding = FragmentMyOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        custId = arguments?.getString(CUSTOMERID).orEmpty()
        Log.d("CUSTOMER ID", "MyOrdersHistoryFragment: "+custId)
        binding.tvCategory.text = "Purchase History"
        binding.tvHome.setOnClickListener {
            val fragment = HomeFragment.newInstance(prefs.userAdrress.toString())
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_content, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.imgRightarrow.load("file:///android_asset/triangle_right.svg") {
            decoderFactory(SvgDecoder.Factory())
        }

        viewModel.orders.observe(viewLifecycleOwner) { response ->
            Log.d("ORDER HISTORY ", " RESPONSE"+response.list)
            if(response.list.size!=0){
                if (response.status == 1) {
                    binding.progressBar.visibility = View.GONE
                    binding.ordersRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = MyOrdersAdapter(response.list.reversed())
                    }
                }

            }
            else{
                binding.progressBar.visibility = View.GONE
                binding.ordersRecyclerView.visibility = View.GONE
                binding.noDataText.visibility = View.VISIBLE
                binding.noDataText.text = "No products found"
            }
        }

        lifecycleScope.launch {
            val customerId = custId.toIntOrNull()
            if (customerId != null) {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.fetchOrders(customerId)
            } else {
                Log.e("OrderFetch", "Invalid customer ID: '$custId'")
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Unable to load orders", Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
