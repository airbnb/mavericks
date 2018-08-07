package com.airbnb.mvrx;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Flowable;
import io.reactivex.FlowableOperator;
import io.reactivex.FlowableTransformer;
import io.reactivex.internal.fuseable.SimplePlainQueue;
import io.reactivex.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.AtomicThrowable;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Allows stopping and resuming the flow of the main source when a secondary flow
 * signals false and true respectively.
 *
 * @param <T> the main source's value type
 *
 * @since 0.7.2
 *
 *
 * This was copied from:
 * https://github.com/akarnokd/RxJava2Extensions/blob/master/src/main/java/hu/akarnokd/rxjava2/operators/FlowableValve.java
 */
final class FlowableValve<T> extends Flowable<T> implements FlowableOperator<T, T>, FlowableTransformer<T, T> {

  final Publisher<? extends T> source;

  final Publisher<Boolean> other;

  final boolean defaultOpen;

  final int bufferSize;

  FlowableValve(Publisher<Boolean> other) {
    this(null, other, true, Flowable.bufferSize());
  }

  FlowableValve(Publisher<? extends T> source, Publisher<Boolean> other, boolean defaultOpen, int bufferSize) {
    this.source = source;
    this.other = other;
    this.defaultOpen = defaultOpen;
    this.bufferSize = bufferSize;
  }

  @Override
  protected void subscribeActual(Subscriber<? super T> s) {
    source.subscribe(apply(s));
  }

  @Override
  public Subscriber<? super T> apply(Subscriber<? super T> observer) {
    ValveMainSubscriber<T> parent = new ValveMainSubscriber<T>(observer, bufferSize, defaultOpen);
    observer.onSubscribe(parent);
    other.subscribe(parent.other);
    return parent;
  }

  @Override
  public Publisher<T> apply(Flowable<T> upstream) {
    return new FlowableValve<T>(upstream, other, defaultOpen, bufferSize);
  }

  static final class ValveMainSubscriber<T>
          extends AtomicInteger
          implements Subscriber<T>, Subscription {

    private static final long serialVersionUID = -2233734924340471378L;

    final Subscriber<? super T> actual;

    final AtomicReference<Subscription> s;

    final AtomicLong requested;

    final SimplePlainQueue<T> queue;

    final OtherSubscriber other;

    final AtomicThrowable error;

    volatile boolean done;

    volatile boolean gate;

    volatile boolean cancelled;

    ValveMainSubscriber(Subscriber<? super T> actual, int bufferSize, boolean defaultOpen) {
      this.actual = actual;
      this.queue = new SpscLinkedArrayQueue<T>(bufferSize);
      this.gate = defaultOpen;
      this.other = new OtherSubscriber();
      this.requested = new AtomicLong();
      this.error = new AtomicThrowable();
      this.s = new AtomicReference<Subscription>();
    }

    @Override
    public void onSubscribe(Subscription s) {
      SubscriptionHelper.deferredSetOnce(this.s, requested, s);
    }

    @Override
    public void onNext(T t) {
      queue.offer(t);
      drain();
    }

    @Override
    public void onError(Throwable t) {
      if (error.addThrowable(t)) {
        drain();
      } else {
        RxJavaPlugins.onError(t);
      }
    }

    @Override
    public void onComplete() {
      done = true;
      drain();
    }

    @Override
    public void request(long n) {
      SubscriptionHelper.deferredRequest(s, requested, n);
    }

    @Override
    public void cancel() {
      cancelled = true;
      SubscriptionHelper.cancel(s);
      SubscriptionHelper.cancel(other);
    }

    void drain() {
      if (getAndIncrement() != 0) {
        return;
      }

      int missed = 1;

      SimplePlainQueue<T> q = queue;
      Subscriber<? super T> a = actual;
      AtomicThrowable error = this.error;

      for (;;) {
        for (;;) {
          if (cancelled) {
            q.clear();
            return;
          }

          if (error.get() != null) {
            Throwable ex = error.terminate();
            q.clear();
            SubscriptionHelper.cancel(s);
            SubscriptionHelper.cancel(other);
            a.onError(ex);
            return;
          }

          if (!gate) {
            break;
          }

          boolean d = done;
          T v = q.poll();
          boolean empty = v == null;

          if (d && empty) {
            SubscriptionHelper.cancel(other);
            a.onComplete();
            return;
          }

          if (empty) {
            break;
          }

          a.onNext(v);
        }

        missed = addAndGet(-missed);
        if (missed == 0) {
          break;
        }
      }
    }

    void change(boolean state) {
      gate = state;
      if (state) {
        drain();
      }
    }

    void innerError(Throwable ex) {
      onError(ex);
    }

    void innerComplete() {
      innerError(new IllegalStateException("The valve source completed unexpectedly."));
    }

    final class OtherSubscriber extends AtomicReference<Subscription> implements Subscriber<Boolean> {

      private static final long serialVersionUID = -3076915855750118155L;

      @Override
      public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.setOnce(this, s)) {
          s.request(Long.MAX_VALUE);
        }
      }

      @Override
      public void onNext(Boolean t) {
        change(t);
      }

      @Override
      public void onError(Throwable t) {
        innerError(t);
      }

      @Override
      public void onComplete() {
        innerComplete();
      }
    }
  }
}