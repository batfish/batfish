package org.batfish.common.topology.bridge_domain.function;

import java.util.Optional;
import org.batfish.common.topology.bridge_domain.edge.Edge;

/** Preserves data as-is. */
public final class Identity<D> implements Edge<D, D> {
  @SuppressWarnings("unchecked")
  public static <D> Identity<D> get() {
    return (Identity<D>) INSTANCE;
  }

  @Override
  public Optional<D> traverse(D data) {
    return Optional.of(data);
  }

  private Identity() {} // prevent instantiation

  private static final Identity<Void> INSTANCE = new Identity<>();
}
