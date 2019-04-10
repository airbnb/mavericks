package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.args
import com.airbnb.mvrx.withState
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_dog_detail.breedsView
import kotlinx.android.synthetic.main.fragment_dog_detail.descriptionView
import kotlinx.android.synthetic.main.fragment_dog_detail.image
import kotlinx.android.synthetic.main.fragment_dog_detail.loveButton
import kotlinx.android.synthetic.main.fragment_dog_detail.nameView

class DogDetailFragment : BaseMvRxFragment() {

    private val viewModel: DogViewModel by activityViewModel()
    private val dogId: Long by args()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dog_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loveButton.setOnClickListener {
            viewModel.loveDog(dogId)
            findNavController().popBackStack()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        val dog = state.dog(dogId) ?: throw IllegalStateException("Cannot find dog with id $dogId")
        Picasso.with(requireContext())
            .load(dog.imageUrl)
            .into(image)
        nameView.text = dog.name
        breedsView.text = dog.breeds
        descriptionView.text = dog.description
    }

    companion object {
        fun arg(dogId: Long): Bundle {
            val args = Bundle()
            args.putLong(MvRx.KEY_ARG, dogId)
            return args
        }
    }
}