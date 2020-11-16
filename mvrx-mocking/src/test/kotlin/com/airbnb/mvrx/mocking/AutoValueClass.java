package com.airbnb.mvrx.mocking;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class AutoValueClass {
    abstract String name();

    abstract int numberOfLegs();

    static Builder builder() {
        return new AutoValue_AutoValueClass.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder setName(String value);

        abstract Builder setNumberOfLegs(int value);

        abstract AutoValueClass build();
    }
}
