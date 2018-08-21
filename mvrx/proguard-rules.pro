# ------------------------
#
#  MvRx Config
#
# -----------------------

# MvRxViewModels loads the Companion class via reflection and thus we need to make sure we keep
# the name of the Companion object.
-keepclassmembers class ** extends com.airbnb.android.lib.mvrx.MvRxViewModel {
    ** Companion;
}

# Classes extending MvRxViewModel get recreated using refleciton, which assumes that a one argument constructor accepting a data class holding
# the state exists. Need to make sure to keep the constructor arround.
-keepclassmembers class * extends com.airbnb.android.lib.mvrx.MvRxViewModel {
    public <init>(...);
}

# Members of the Kotlin data classes used as the state in MvRx are read via Kotlin reflection which cause trouble with Proguard if they are not
# kept.
-keepclassmembers class * implements com.airbnb.mvrx.MvRxState {
   *;
}