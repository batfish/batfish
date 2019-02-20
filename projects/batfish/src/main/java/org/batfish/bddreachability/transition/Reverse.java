package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;

import net.sf.javabdd.BDD;

final class Reverse implements Transition {
  private final Transition _inner;

  Reverse(Transition inner) {
    checkArgument(
        inner != IDENTITY && inner != ZERO && !(inner instanceof Constraint),
        "Reverse is not allowed for bijective Transitions. Use the transition itself.");
    _inner = inner;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return _inner.transitBackward(bdd);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return _inner.transitForward(bdd);
  }
}
