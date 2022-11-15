package com.ikhokha.techcheck.utils.adapters

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide.with
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import com.ikhokha.techcheck.R
import com.ikhokha.techcheck.data.entities.Product
import com.ikhokha.techcheck.databinding.BasketItemBinding
import com.ikhokha.techcheck.repositories.STORAGE_BASE_URL

class BasketAdapter(private val listener: OnItemClickedListener, val app: Application, val fbStorage: FirebaseStorage)
    : ListAdapter<Product, BasketAdapter.ViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BasketItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val product = getItem(pos)
        holder.bind(product)
    }

    inner class ViewHolder(private val binding: BasketItemBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) listener.onItemClick(getItem(position))
            }
        }

        fun bind(product: Product) {
            binding.apply {
                with(app)
                    .load(fbStorage.getReferenceFromUrl("${STORAGE_BASE_URL}${product.image}"))
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(AppCompatResources.getDrawable(app, R.drawable.progress_animation))
                    .into(image)
                description.text = product.description
                price.text = app.getString(R.string.product_price, product.price)
                quantity.text = "x ${product.quantity}"
            }
        }
    }

    interface OnItemClickedListener  {
        fun onItemClick(product: Product)
    }

    class DiffCallback: DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) =
            (oldItem.image == newItem.image && oldItem.description == newItem.description)

        override fun areContentsTheSame(oldItem: Product, newItem: Product) =
            oldItem == newItem
    }
}