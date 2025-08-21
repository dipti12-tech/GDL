import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.data.model.CartItem
import com.app.gdl.databinding.RowOrderplacedlistBinding
import com.bumptech.glide.Glide

class MyOrderPlacedSuccessListAdapter(
    private val items: List<CartItem>
) : RecyclerView.Adapter<MyOrderPlacedSuccessListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowOrderplacedlistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            binding.selectedTitle.text = item.name
            binding.tvQuantity.text = "${item.quantity}"
            val displayPrice = String.format("KES %.2f", item.pricePerUnit)
            Glide.with(binding.selectedImg.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.default_placeholder)
                .error(R.drawable.error_image)
                .into(binding.selectedImg)
            binding.selectedQuantity.text =
                "$displayPrice \n ${item.pricePerUnit} / ${item.BaseUOM}  "

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowOrderplacedlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}

