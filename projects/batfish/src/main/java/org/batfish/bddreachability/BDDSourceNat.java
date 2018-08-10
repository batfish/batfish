package org.batfish.bddreachability;

import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;

final class BDDSourceNat {
  final @Nonnull BDD _condition;
  final @Nonnull BDD _updateSrcIp;

  public BDDSourceNat(@Nonnull BDD condition, @Nonnull BDD updateSrcIp) {
    _condition = condition;
    _updateSrcIp = updateSrcIp;
  }
}
