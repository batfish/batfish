package org.batfish.common.util;

import java.util.function.Supplier;

/**
 * A {@link Supplier} that throws an exception if two simultaneous calls are attempted from within
 * the same thread (i.e. the supplier is recursive). The goal is to detect the recursion, and under
 * the assumption that it's an infinite recursion, throw an exception.
 */
public class NonRecursiveSupplier<T> implements Supplier<T>, com.google.common.base.Supplier<T> {
  public static final class NonRecursiveSupplierException extends RuntimeException {
    public static final long serialVersionUID = 1;
  }

  private final Supplier<T> _inner;

  /*
   * True if we are currently inside a call to _inner.get(). If so, and we call again, then we
   * have reentered.
   */
  private boolean _inInner;

  public NonRecursiveSupplier(Supplier<T> inner) {
    _inner = inner;
    _inInner = false;
  }

  /*
   * Synchronizing ensures only one thread can call get at at time. However, that thread may
   * call get recursively without blocking (since it already has the lock).
   */
  @Override
  public synchronized T get() {
    acquire();
    try {
      return _inner.get();
    } finally {
      release();
    }
  }

  private void acquire() {
    if (_inInner) {
      throw new NonRecursiveSupplierException();
    }
    _inInner = true;
  }

  private void release() {
    if (!_inInner) {
      throw new IllegalStateException("Releasing unacquired semaphore");
    }
    _inInner = false;
  }
}
