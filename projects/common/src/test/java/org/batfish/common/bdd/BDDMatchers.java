package org.batfish.common.bdd;

import net.sf.javabdd.BDD;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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

  private static final class Intersects extends BaseMatcher<BDD> {
    private final BDD _other;

    Intersects(BDD other) {
      _other = other;
    }

    @Override
    public boolean matches(Object o) {
      return ((BDD) o).andSat(_other);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("BDD intersects other");
    }
  }

  private static final class IsOne extends BaseMatcher<BDD> {
    @Override
    public boolean matches(Object o) {
      return ((BDD) o).isOne();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("BDD::isOne() returns true");
    }
  }

  private static final class IsZero extends BaseMatcher<BDD> {
    @Override
    public boolean matches(Object o) {
      return ((BDD) o).isZero();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("BDD::isZero() returns true");
    }
  }
}
