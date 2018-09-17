package org.batfish.common.bdd;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDMatchersImpl.Intersects;
import org.batfish.common.bdd.BDDMatchersImpl.IsOne;
import org.batfish.common.bdd.BDDMatchersImpl.IsZero;
import org.hamcrest.Matcher;

public final class BDDMatchers {
  private BDDMatchers() {}

  public static Matcher<BDD> intersects(BDD other) {
    return new Intersects(other);
  }

  public static Matcher<BDD> isOne() {
    return new IsOne();
  }

  public static Matcher<BDD> isZero() {
    return new IsZero();
  }
}
