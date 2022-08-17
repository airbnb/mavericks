package com.airbnb.mvrx.dogs

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.airbnb.mvrx.dogs.data.Dog
import com.airbnb.mvrx.dogs.databinding.DogRowBinding
import com.airbnb.mvrx.dogs.utils.LayoutViewHolder
import com.airbnb.mvrx.dogs.views.DogsFragmentHandler
import com.airbnb.mvrx.dogs.views.HashItemCallback

class DogViewHolder(parent: ViewGroup) : LayoutViewHolder(parent, R.layout.dog_row) {
    val binding = DogRowBinding.bind(itemView)
}

class DogAdapter(private val fragment: DogsFragmentHandler) : ListAdapter<Dog, DogViewHolder>(HashItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        return DogViewHolder(parent).apply {
            binding.handler = fragment
        }
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.binding.dog = getItem(position)
    }
}
