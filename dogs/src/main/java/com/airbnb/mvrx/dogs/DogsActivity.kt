package com.airbnb.mvrx.dogs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.BaseMvRxActivity
import kotlinx.android.synthetic.main.activity_dogs.toolbar

class DogsActivity : BaseMvRxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dogs)
        toolbar.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }
}
