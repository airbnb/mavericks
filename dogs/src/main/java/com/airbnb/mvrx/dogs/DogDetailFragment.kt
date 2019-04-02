package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.args
import com.airbnb.mvrx.withState
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_dog_detail.breeds
import kotlinx.android.synthetic.main.fragment_dog_detail.image
import kotlinx.android.synthetic.main.fragment_dog_detail.name

class DogDetailFragment : BaseMvRxFragment() {

    private val dogId: Long by args()
    private val viewModel: DogsViewModel by activityViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dog_detail, container, false)
    }

    override fun invalidate() = withState(viewModel) { state ->
        val dog = state.dogs()?.firstOrNull { it.id == dogId } ?: throw IllegalArgumentException("Unknown dog $dogId")
        Picasso.with(requireContext())
            .load(dog.imageUrl)
            .into(image)
        name.text = dog.name
        breeds.text = dog.breeds
    }

    companion object {
        fun args(id: Long): Bundle {
            val args = Bundle()
            args.putLong(MvRx.KEY_ARG, id)
            return args
        }
    }
}