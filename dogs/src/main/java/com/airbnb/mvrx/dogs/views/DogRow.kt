package com.airbnb.mvrx.dogs.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.mvrx.dogs.R
import com.airbnb.mvrx.dogs.data.Dog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dog_row.view.breedsView
import kotlinx.android.synthetic.main.dog_row.view.image
import kotlinx.android.synthetic.main.dog_row.view.nameView

class DogRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.dog_row, this, true)
    }

    fun setDog(dog: Dog) {
        Picasso.with(context)
            .load(dog.imageUrl)
            .into(image)
        nameView.text = dog.name
        breedsView.text = dog.breeds
    }

    fun setClickListener(listener: View.OnClickListener?) {
        setOnClickListener(listener)
    }
}