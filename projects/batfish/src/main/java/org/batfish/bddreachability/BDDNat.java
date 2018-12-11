package org.batfish.bddreachability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;

/**
 * {@link BDD} representation of a source or destination NAT. The specified value should be a
 * constraint on the variable.
 */
final class BDDNat {
  final @Nonnull BDD _condition;
  final @Nullable BDD _pool;

  public BDDNat(@Nonnull BDD condition, @Nullable BDD pool) {
    _condition = condition;
    _pool = pool;
  }
}
