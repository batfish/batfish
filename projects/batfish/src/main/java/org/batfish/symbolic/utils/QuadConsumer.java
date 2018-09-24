package org.batfish.symbolic.utils;

import java.util.Objects;

public interface QuadConsumer<A, B, C, D> {
  void accept(final A a, final B b, final C c, final D d);

  default QuadConsumer<A, B, C, D> andThen(
      final QuadConsumer<? super A, ? super B, ? super C, ? super D> after) {
    Objects.requireNonNull(after);
    return (A a, B b, C c, D d) -> {
      accept(a, b, c, d);
      after.accept(a, b, c, d);
    };
  }
}