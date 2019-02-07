package org.batfish.bddreachability.transition;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDSourceManager;

/** Smart constructors of {@link Transition}. */
public final class Transitions {
  private Transitions() {}

  /** */
  public static final Transition IDENTITY = Identity.INSTANCE;

  /** */
  public static final Transition ZERO = Zero.INSTANCE;

  public static Transition compose(Transition... transitions) {
    if (Stream.of(transitions).anyMatch(t -> t == ZERO)) {
      return ZERO;
    }
    List<Transition> nonIdentityTransitions =
        Stream.of(transitions)
            .filter(transition -> transition != IDENTITY)
            .collect(ImmutableList.toImmutableList());
    if (nonIdentityTransitions.isEmpty()) {
      return IDENTITY;
    }
    if (nonIdentityTransitions.size() == 1) {
      return nonIdentityTransitions.get(0);
    }
    return new Composite(nonIdentityTransitions);
  }

  public static Transition constraint(BDD bdd) {
    return bdd.isOne() ? IDENTITY : bdd.isZero() ? ZERO : new Constraint(bdd);
  }

  public static Transition eraseAndSet(BDD var, BDD value) {
    return value.isOne() ? IDENTITY : new EraseAndSet(var, value);
  }

  public static Transition or() {
    throw new BatfishException("Don't call or() with no Transitions -- just use Zero instead.");
  }

  public static Transition or(Transition... transitions) {
    List<Transition> nonZeroTransitions =
        Stream.of(transitions)
            .filter(transition -> transition != ZERO)
            .collect(ImmutableList.toImmutableList());
    if (nonZeroTransitions.isEmpty()) {
      return ZERO;
    }
    if (nonZeroTransitions.size() == 1) {
      return nonZeroTransitions.get(0);
    }
    return new Or(nonZeroTransitions);
  }

  public static Transition addSourceInterfaceConstraint(BDDSourceManager mgr, String iface) {
    return mgr.isTrivial() ? IDENTITY : new AddSourceConstraint(mgr, iface);
  }

  public static Transition addOriginatingFromDeviceConstraint(BDDSourceManager mgr) {
    return mgr.isTrivial() ? IDENTITY : new AddSourceConstraint(mgr);
  }

  public static Transition removeSourceConstraint(BDDSourceManager mgr) {
    return mgr.isTrivial() ? IDENTITY : new RemoveSourceConstraint(mgr);
  }
}
