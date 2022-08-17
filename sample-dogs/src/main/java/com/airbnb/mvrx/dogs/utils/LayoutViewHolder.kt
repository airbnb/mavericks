package com.airbnb.mvrx.dogs.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class LayoutViewHolder(parent: ViewGroup, @LayoutRes layoutRes: Int) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
)
