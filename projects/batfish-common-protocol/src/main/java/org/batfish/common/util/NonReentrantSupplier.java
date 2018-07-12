package org.batfish.common.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/** A {@link Supplier} that throws an exception if two simultaneous calls are attempted. */
public class NonReentrantSupplier<T> implements Supplier<T>, com.google.common.base.Supplier<T> {
  public static final class NonReentrantSupplierException extends RuntimeException {
    public static final long serialVersionUID = 1;
  }

  private final Supplier<T> _inner;

  private final AtomicBoolean _semaphore;

  public NonReentrantSupplier(Supplier<T> inner) {
    _inner = inner;
    _semaphore = new AtomicBoolean(false);
  }

  @Override
  public T get() {
    acquire();
    try {
      return _inner.get();
    } finally {
      release();
    }
  }

  private void acquire() {
    if (!_semaphore.compareAndSet(false, true)) {
      throw new NonReentrantSupplierException();
    }
  }

  private void release() {
    if (!_semaphore.compareAndSet(true, false)) {
      throw new IllegalStateException("Releasing unacquired semaphore");
    }
  }
}
