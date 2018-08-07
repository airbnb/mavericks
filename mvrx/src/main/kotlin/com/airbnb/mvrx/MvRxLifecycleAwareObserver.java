package com.airbnb.mvrx;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.Lifecycle.State;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.LambdaObserver;

@SuppressWarnings("unused")
public final class MvRxLifecycleAwareObserver<T> extends AtomicReference<Disposable> implements LifecycleObserver, Observer<T>, Disposable {

    public static final State DEFAULT_ACTIVE_STATE = State.STARTED;

    private final Observer<T> sourceObserver;
    private LifecycleOwner owner;

    @Nullable private Disposable sourceDisposable;
    @Nullable private T lastUndeliveredValue;
    @Nullable private T lastValue;
    private final AtomicBoolean locked = new AtomicBoolean(true);
    private final State activeState;
    private final boolean alwaysDeliverLastValueWhenUnlocked;

    public static class Builder<T> {
        private Consumer<? super T> onNext = Functions.emptyConsumer();
        private Consumer<? super Throwable> onError = Functions.ON_ERROR_MISSING;
        private Action onComplete = Functions.EMPTY_ACTION;
        private Consumer<? super Disposable> onSubscribe = Functions.emptyConsumer();
        private final LifecycleOwner owner;
        private Lifecycle.State activeState = DEFAULT_ACTIVE_STATE;
        private boolean alwaysDeliverValueWhenUnlocked = false;

        public Builder(LifecycleOwner owner) {
            this.owner = owner;
        }

        public Builder(LifecycleOwner owner, Lifecycle.State activeState) {
            this.owner = owner;
            this.activeState = activeState;
        }

        public Builder<T> alwaysDeliverValueWhenUnlocked() {
            return alwaysDeliverValueWhenUnlocked(true);
        }

        public Builder<T> alwaysDeliverValueWhenUnlocked(boolean shouldDeliver) {
            alwaysDeliverValueWhenUnlocked = shouldDeliver;
            return this;
        }

        public Builder<T> onNext(Consumer<? super T> onNext) {
            this.onNext = onNext;
            return this;
        }

        public Builder<T> onError(Consumer<? super Throwable> onError) {
            this.onError = onError;
            return this;
        }

        public Builder<T> onSubscribe(Consumer<? super Disposable> onSubscribe) {
            this.onSubscribe = onSubscribe;
            return this;
        }

        public Builder<T> onComplete(Action onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        public MvRxLifecycleAwareObserver<T> build() {
            return new MvRxLifecycleAwareObserver<>(
                    owner,
                    activeState,
                    new LambdaObserver<T>(onNext, onError, onComplete, onSubscribe),
                    alwaysDeliverValueWhenUnlocked
            );
        }
    }

    public static <T> Builder<T> builder(LifecycleOwner owner) {
        return new Builder<>(owner);
    }

    public static <T> Builder<T> builder(LifecycleOwner owner, Lifecycle.State activeState) {
        return new Builder<>(owner, activeState);
    }

    public static <T> MvRxLifecycleAwareObserver<T> from(LifecycleOwner owner, Observer<T> consumer) {
        return new MvRxLifecycleAwareObserver<>(owner, consumer);
    }

    /**
     * Create a LifecycleObserver from a {@link Consumer}.
     */
    public static <T> MvRxLifecycleAwareObserver<T> from(LifecycleOwner owner, Consumer<T> consumer) {
        return MvRxLifecycleAwareObserver.<T>builder(owner).onNext(consumer).build();
    }

    public MvRxLifecycleAwareObserver(LifecycleOwner owner, Observer<T> consumer) {
        this(owner, DEFAULT_ACTIVE_STATE, consumer, false);
    }

    public MvRxLifecycleAwareObserver(
            LifecycleOwner owner,
            Lifecycle.State activeState,
            Observer<T> sourceObserver,
            boolean alwaysDeliverLastValueWhenUnlocked) {
        super();
        this.alwaysDeliverLastValueWhenUnlocked = alwaysDeliverLastValueWhenUnlocked;
        this.sourceObserver = sourceObserver;
        this.owner = owner;
        this.activeState = activeState;
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (DisposableHelper.setOnce(this, d)) {
            owner.getLifecycle().addObserver(this);
            sourceObserver.onSubscribe(this);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        owner.getLifecycle().removeObserver(this);
        owner = null;
        if (!isDisposed()) {
            dispose();
        }
    }

    @OnLifecycleEvent(Event.ON_ANY)
    void onLifecycleEvent() {
        updateLock();
    }

    private void updateLock() {
        if (owner != null && owner.getLifecycle().getCurrentState().isAtLeast(activeState)) {
            unlock();
        } else {
            lock();
        }
    }

    private void unlock() {
        if (!locked.getAndSet(false)) {
            return;
        }
        if (!isDisposed() ) {
            if (alwaysDeliverLastValueWhenUnlocked && lastValue != null) {
                sourceObserver.onNext(lastValue);
                lastUndeliveredValue = null;
            } else if (lastUndeliveredValue != null) {
                sourceObserver.onNext(lastUndeliveredValue);
                lastUndeliveredValue = null;
            }
        }
    }

    private void lock() {
        locked.set(true);
    }

    @Override
    public void onNext(T t) {
        if (!locked.get()) {
            sourceObserver.onNext(t);
        } else {
            lastUndeliveredValue = t;
        }
        lastValue = t;
    }

    @Override
    public void onError(Throwable e) {
        if (!isDisposed()) {
            lazySet(DisposableHelper.DISPOSED);
            sourceObserver.onError(e);
        }
    }

    @Override
    public void onComplete() {
        sourceObserver.onComplete();
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return get() == DisposableHelper.DISPOSED;
    }
}