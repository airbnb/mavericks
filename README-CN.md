[中文文档](https://github.com/luozejiaqun/DiMvRx/blob/master/README-CN.md)
# DiMvRx

简化了 [MvRx](https://github.com/airbnb/MvRx), 使其可以更加方便的使用Dagger2.  
主要做了如下改变:  
1. 当`debugMode` 为false时，不再需要反射，也就是线上版本不需要反射。
2. 不再需要继承`BaseMvRxActivity` 和 `BaseMvRxFragment`。
3. 当创建ViewModel时可以提供自己的`ViewModelProvider.Factory`. 如果使用Dagger2可以很方便地实现`ViewModelProvider.Factory`接口。
4. 通过父Fragment共享ViewModel，当使用`ViewPager`时可能会用到。
5. ViewModel不需要实现`MvRxViewModelFactory`来创建自身，都可以通过Dagger2的方式实现。
6. 不需要通过`State`的二级构造函数去初始化State，因为State的构建不再是通过反射的方式。
7. 还有很多别的小改动。

示例:
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
查看给出的两个例子了解更多使用方法。

## 使用方式

直接拷贝源码即可。该库只是展示来我在实践中如何将MvRx和Dagger2进行整合的，你可以对其进行修改，使其符合你的方式。