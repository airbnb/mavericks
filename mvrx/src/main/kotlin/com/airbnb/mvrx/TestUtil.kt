package com.airbnb.mvrx


class ViewModelMocker<S : Any>(val stateProvider: () -> S)
// TODO What is the best interface for getting the state? A callback, or a simple setter?
// TODO Should initial state be mocked?
// TODO Should this class invalidate the view when the state is changed?
