package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.BaseMvRxFragment
import kotlinx.android.synthetic.main.fragment_dogs.adoptButton

class DogDetailFragment : BaseMvRxFragment() {

//    private val viewModel: DogsViewModel by activityViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dog_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adoptButton.setOnClickListener {
            //            viewModel.loveDog(dogId)
            findNavController().popBackStack()
        }
    }

    override fun invalidate() {

    }

//    override fun invalidate() = withState(viewModel) { state ->
//        val dog = state.dog(dogId)
//        Picasso.with(requireContext())
//            .load(dog.imageUrl)
//            .into(image)
//        nameView.text = dog.name
//        breedsView.text = dog.breeds
//        descriptionView.text = dog.description
//    }
}