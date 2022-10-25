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

# If a companion object of a class is implementing com.airbnb.mvrx.MavericksViewModelFactory it is used to 
# instatiate ViewModels.
# This is done by looking up classes defined as subclasses of the ViewModel class itself that implement
# com.airbnb.mvrx.MavericksViewModelFactory and thus we need to ensure that the companion objects definition
# stays within the ViewModel class during Proguard/Dexguard/R8 optimization. Which was not the case anymore with
# R8 3.3.x with enabeld full mode 
# We can however rename or even shrink away the ViewModel/Companion class if they are not used.
# This ensures we keep the companion objectet implementing that interface
-keep,allowshrinking,allowobfuscation class **$Companion implements com.airbnb.mvrx.MavericksViewModelFactory
# This ensures we keep the class that conmtains the Companion object we kept one line up.
-if class **$Companion implements com.airbnb.mvrx.MavericksViewModelFactory
-keep,allowshrinking,allowobfuscation class <1>

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
