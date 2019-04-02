package com.airbnb.mvrx.dogs

import android.app.Application

class DogApplication : Application() {
    val dogsRespository = DogRepository()
}