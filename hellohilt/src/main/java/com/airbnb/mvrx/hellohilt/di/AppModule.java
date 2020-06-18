package com.airbnb.mvrx.hellohilt.di;

import com.airbnb.mvrx.hellohilt.HelloViewModel;
import com.airbnb.mvrx.hellohilt.HelloViewModel_AssistedFactory;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.multibindings.IntoMap;

@Module
@InstallIn(ApplicationComponent.class)
public abstract class AppModule {

    @Binds
    @IntoMap
    @ViewModelKey(HelloViewModel.class)
    abstract AssistedViewModelFactory<?, ?> helloViewModelFactory(HelloViewModel_AssistedFactory factory);
}
