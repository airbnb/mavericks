package com.airbnb.mvrx.dogs.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.airbnb.mvrx.dogs.R
import com.squareup.picasso.Picasso

class DogImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    fun setUrl(url: String?) {
        if (url == null) {
            Picasso.get().cancelRequest(this)
            setImageDrawable(null)
            return
        }

        Picasso.get()
            .load(url)
            .placeholder(R.color.loading)
            .into(this)
    }
}
