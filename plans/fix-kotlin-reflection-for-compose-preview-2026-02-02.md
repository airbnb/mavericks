# Fix Kotlin Reflection for Compose Preview Compatibility

## Problem
Jetpack Compose UI previews fail in Android Studio when using Kotlin 2.3 because:
1. Kotlin 2.3 reimplemented `kotlin-reflect` for K2 metadata format
2. Android Studio's `layoutlib` has a shaded copy of `kotlin-reflect` that lacks K2-compatible `META-INF/services/MetadataExtensions`
3. MvRx's `DataClassSetDsl.Setter` calls `KClass.isData` which triggers metadata reading and crashes

## Stack Trace
```
java.lang.IllegalStateException: No MetadataExtensions instances found in the classpath
  at _layoutlib_._internal_.kotlin.reflect.jvm.internal.impl.km.internal.extensions.MetadataExtensions
  ...
  at _layoutlib_._internal_.kotlin.reflect.jvm.internal.KClassImpl.isData(KClassImpl.kt:457)
  at com.airbnb.mvrx.mocking.DataClassSetDsl$Setter.<init>(DataClassSetDsl.kt:143)
```

## Solution
Reuse existing Java reflection utilities in `MavericksMutabilityHelper.kt` and extend them
to replace Kotlin reflection APIs that read metadata.

### Existing Utilities Reused
- `Class<*>.isData` from `MavericksMutabilityHelper.kt` - already uses Java reflection
- `copy$default` invocation pattern from `PersistState.kt` (production-tested bitmask approach)

### New Utilities Added to `MavericksMutabilityHelper.kt`
- `callCopy()` - calls `copy$default` with named parameters (using PersistState.kt's bitmask pattern)
- `calculateParameterCountOfCopyFunction()` - calculates param count from copy function
- `getPropertyValue()` - gets property value via getter/field access
- `getComponentMethod()` - gets componentN method handling mangled names

## Implementation Steps

- [x] Make `Class<*>.isData` accessible via `@InternalMavericksApi` annotation
- [x] Add `callCopy()` function using production-tested bitmask pattern from `PersistState.kt`
- [x] Add `calculateParameterCountOfCopyFunction()` helper (copied from PersistState.kt)
- [x] Add `defaultParameterValue` extension for placeholder values (copied from PersistState.kt)
- [x] Add `getPropertyValue()` function for property access
- [x] Add `getComponentMethod()` helper function
- [x] Modify `DataClassSetDsl.kt` to use shared functions
- [x] Add comprehensive tests (25 tests total, all passing)
  - 17 core utility tests in `mvrx-common`
  - 8 DataClassSetDsl integration tests in `mvrx-mocking`

## Files Modified

### `mvrx-common/src/main/java/com/airbnb/mvrx/DataClassJavaReflection.kt` (NEW)
Java reflection utilities for working with Kotlin data classes:
- `Class<*>.isData` - duck-typed data class detection
- `callCopy()` - calls `copy$default` with named parameters (using PersistState.kt's bitmask pattern)
- `calculateParameterCountOfCopyFunction()` - calculates param count from copy function
- `defaultParameterValue` - placeholder values for bitmask-ignored parameters
- `getPropertyValue()` - gets property values via getters/fields

### `mvrx-common/src/main/java/com/airbnb/mvrx/MavericksMutabilityHelper.kt`
- Unchanged (only mutability-related code remains)
- Uses `isData` from `DataClassJavaReflection.kt` (same package, no import needed)

### `mvrx/src/main/kotlin/com/airbnb/mvrx/PersistState.kt`
- Refactored `restorePersistedMavericksState` to use shared `callCopy()` function
- Removed duplicate `calculateParameterCountOfCopyFunction()` (now in DataClassJavaReflection.kt)
- Removed duplicate `defaultParameterValue` extension (now in DataClassJavaReflection.kt)

### `mvrx-mocking/src/main/kotlin/com/airbnb/mvrx/mocking/DataClassSetDsl.kt`
- Import shared functions from `com.airbnb.mvrx`
- Replace `clazz.isData` (Kotlin) with `clazz.java.isData` (Java)
- Replace `callCopy` from `KotlinReflectUtils` with shared `callCopy`
- Replace `memberProperties` with `getPropertyValue`

### `mvrx-common/src/test/kotlin/com/airbnb/mvrx/JavaReflectUtilsTest.kt` (NEW)
- 17 tests covering core utilities:
  - `isData` detection
  - `getPropertyValue` property access
  - `callCopy` copy operations

### `mvrx-mocking/src/test/kotlin/com/airbnb/mvrx/mocking/DataClassSetDslIntegrationTest.kt` (NEW)
- 8 tests covering DataClassSetDsl integration:
  - `set`, `setNull`, `setTrue`, `setFalse`, `setZero`, `setEmpty`
  - Nested and deeply nested property access

## Test Results
```
mvrx-common: tests="17" skipped="0" failures="0" errors="0"
mvrx-mocking: tests="8" skipped="0" failures="0" errors="0"
mvrx: tests="154" skipped="0" failures="0" errors="0" (includes PersistedStateTest)
Total: All tests passing
```

## JDK 22 Compatibility Fix

Updated project to support JDK 22:

### `versions.properties`
- Updated Robolectric from 4.10.3 to 4.16.1 (required for JDK 22 class file format - older ASM can't read newer class files)

### Test Manifest Overrides
Robolectric 4.16.1 brings in `androidx.test:monitor:1.8.0` and `androidx.test.espresso:espresso-idling-resource:3.7.0`,
which require minSdk 21. Since these are test-only dependencies and Robolectric runs on JVM (not actual Android devices),
we use `tools:overrideLibrary` in test manifests to keep the library's minSdk at 16.

Added test AndroidManifest.xml files with the override to:
- `mvrx/src/test/`
- `mvrx-compose/src/test/` (updated existing)
- `mvrx-hilt/src/test/`
- `mvrx-launcher/src/test/`
- `mvrx-mocking/src/test/`
- `mvrx-navigation/src/test/`
- `mvrx-rxjava2/src/test/`
