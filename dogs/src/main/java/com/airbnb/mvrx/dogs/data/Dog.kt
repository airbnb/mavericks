package com.airbnb.mvrx.dogs.data

data class Dog(
    val id: Long,
    val name: String,
    val breeds: String,
    val imageUrl: String,
    val description: String
) : StableItem {
    override val stableId = id
}
