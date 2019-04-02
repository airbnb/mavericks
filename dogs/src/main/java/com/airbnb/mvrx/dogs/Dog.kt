package com.airbnb.mvrx.dogs

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Dog(
    @PrimaryKey val id: Long,
    val name: String,
    val weightLbs: Int,
    val breeds: String,
    val personality: String,
    val imageUrl: String,
    val description: String
)