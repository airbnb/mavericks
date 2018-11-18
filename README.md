[中文文档](https://github.com/luozejiaqun/DiMvRx/blob/master/README-CN.md)
# DiMvRx

Simplify [MvRx](https://github.com/airbnb/MvRx), make it quite convenient to work with Dagger2.  
Changes include:  
1. Remove all the reflections of MvRx when `debugMode` set to false.
2. Don NOT need to extend from `BaseMvRxActivity` and `BaseMvRxFragment`.
3. You can provide your own `ViewModelProvider.Factory` when create ViewModels. With the help of Dagger2, you can implement `ViewModelProvider.Factory` quite simple.
4. You can share ViewModel through parent Fragment which might be helpful when you use `ViewPager`.
5. No need to implement `MvRxViewModelFactory` in ViewModel.
6. No need to use `State`'s secondary constructor to initialize the state.

This is what it looks like:
```kotlin

data class HelloWorldState(val title: String = "Hello World") : MvRxState

class HelloWorldViewModel() : MyBaseMvRxViewModel<HelloWorldState>(HelloWorldState()) {
    fun getMoreExcited() = setState { copy(title = "$title!") }
}

class HelloWorldFragment : BaseFragment() {
    private val viewModel: HelloWorldViewModel by fragmentViewModel()

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        header {
            title(state.title)
        }
        basicRow { 
            onClick { viewModel.getMoreExcited() }
        }
    }
}

abstract class BaseFragment : Fragment(), MvRxView, ViewModelFactoryOwner {
    //ViewModelFactory is a class implements ViewModelProvider.Factory
    @Inject override lateinit var viewModelFactory: ViewModelFactory
    
    override fun onCreate(savedInstanceState: Bundle?) {
        //inject viewModelFactory with dagger
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }
}
```
See the sample for more infomation.

## Installation

Just copy the code and make it your own way. This library is just a sample to show how I integate MvRx with Dagger2. You can find your way.