package org.batfish.common.topology.broadcast;

import java.util.Optional;

/** Preserves data as-is. */
public final class Preserve<D> implements Edge<D, D> {
  @SuppressWarnings("unchecked")
  public static <D> Preserve<D> get() {
    return (Preserve<D>) INSTANCE;
  }

  @Override
  public Optional<D> traverse(D data) {
    return Optional.of(data);
  }

  private Preserve() {} // prevent instantiation

  private static final Preserve<Void> INSTANCE = new Preserve<>();
}
