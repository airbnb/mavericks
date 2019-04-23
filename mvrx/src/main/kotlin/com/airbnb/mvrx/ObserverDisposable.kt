package com.airbnb.mvrx

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

internal interface ObserverDisposable<T : Any> : Observer<T>, Disposable