# ------------------------
#
#  MvRx Config
#
# -----------------------


# Classes extending BaseMavericksViewModel are recreated using reflection, which assumes that a one argument
# constructor accepting a data class holding the state exists. Need to make sure to keep the constructor
# around. Additionally, a static create / inital state method will be generated in the case a
# companion object factory is used with JvmStatic. This is accessed via reflection.
-keepclassmembers class ** extends com.airbnb.mvrx.MavericksViewModel {
    public <init>(...);
    public static *** create(...);
    public static *** initialState(...);
}

# If a MvRxViewModelFactory is used without JvmStatic, keep create and initalState methods which
# are accessed via reflection.
-keepclassmembers class ** implements com.airbnb.mvrx.MavericksViewModelFactory {
     public <init>(...);
     public *** create(...);
     public *** initialState(...);
}

# The constructor as well as the copy$default() method and the component*() methods of the Kotlin data classes used as
# the state in MvRx are read via reflection which cause trouble with Proguard if they are not kept.
-keepclassmembers class ** implements com.airbnb.mvrx.MavericksState {
   public <init>(...);
   synthetic *** copy$default(...);
   public *** component*();
}
