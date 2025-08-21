import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.Order
import com.app.gdl.databinding.ItemMyorderBinding
import com.app.gdl.presentation.ui.adapters.MyOrderProductAdapter
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Currency

class MyOrdersAdapter(
    private val orders: List<Order>
) : RecyclerView.Adapter<MyOrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemMyorderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMyorderBinding.inflate(inflater, parent, false)
        return OrderViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        with(holder.binding) {
            val formatted = formatDateTime(order.order_date.toString())
            tvOrderPlacedTitle.text = "Order placed ${formatted}"
            tvOrderNumber.text = "Order #${order.order_id}"
            val displayStatus = getDisplayStatus(order.erp_order_status)
            tvStatusValue.text = displayStatus
            val productAdapter = MyOrderProductAdapter(order)
            rvOrderProducts.layoutManager = LinearLayoutManager(holder.itemView.context)
            rvOrderProducts.adapter = productAdapter


            val totalAmount = order.order_details.sumOf {
                it.OrderQty.value * it.UnitPrice.value
            }
            subtotal.text = formatCurrency(totalAmount)
            grandTotal.text = formatCurrency(totalAmount)
        }
    }

    override fun getItemCount(): Int = orders.size

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateTime(date: String): String {
        val zonedDateTime = ZonedDateTime.parse(date)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
        return zonedDateTime.format(formatter)
    }

    private val statusMap = mapOf(
        "InCMS" to "Order Placed",
        "On Hold" to "Order Placed",
        "Open" to "Order Placed",
        "Pending Approval" to "Order Placed",
        "Pending Processing" to "Order Confirmed",
        "Awaiting Payment" to "Awaiting Payment",
        "Shipping" to "Order Shipped",
        "Invoiced" to "Order Shipped",
        "Completed" to "Order Delivered",
        "Cancelled" to "Order Cancelled",
        "Rejected" to "Order Rejected"
    )

    private fun getDisplayStatus(status: String): String {
        return statusMap[status] ?: status // fallback if not found
    }

    fun formatCurrency(amount: Double, currencyCode: String = "KES"): String {
        val formatter = NumberFormat.getCurrencyInstance()
        formatter.currency = Currency.getInstance(currencyCode)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        return formatter.format(amount).replace(formatter.currency.symbol, currencyCode + " ")
    }
}
