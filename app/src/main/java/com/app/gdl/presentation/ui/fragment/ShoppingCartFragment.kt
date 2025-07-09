package com.app.gdl.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gdl.databinding.ActivityShoppingcartBinding
import com.app.gdl.presentation.ui.adapters.CartAdapter
import com.app.gdl.utils.CartManager

class ShoppingCartFragment : Fragment(){

    private var _binding: ActivityShoppingcartBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CartAdapter

    companion object {
        fun newInstance() = ShoppingCartFragment()
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

        Log.d("CartManager", "Cart size: ${CartManager.getItems().size}")
        (activity as? CartAdapter.CartItemCountListener)?.onCartItemCountChanged(CartManager.getItems().size)
        binding.selectionItems.text="${CartManager.getItems().size}  Selections"

        adapter = CartAdapter(
            CartManager.getItems().toMutableList(),
            object : CartAdapter.CartTotalListener {
                override fun onCartTotalCalculated(subtotal: Double, grandTotal: Double) {
                    binding.subTotal.text = "Sub-Total   KES %.2f".format(subtotal)
                    binding.grandTotal.text = "Grand Total   KES %.2f".format(grandTotal)
                }
            },
            object : CartAdapter.CartItemCountListener {
                override fun onCartItemCountChanged(count: Int) {
                    binding.selectionItems.text="$count  Selections"
                    Log.d("Fragment", "Forwarding item count to Activity: $count")
                    (activity as? CartAdapter.CartItemCountListener)?.onCartItemCountChanged(count)                }
            }
        )

        binding.recycleviewUOM.adapter = adapter

        binding.btnEmptyCart.setOnClickListener {
            adapter.emptyData()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
