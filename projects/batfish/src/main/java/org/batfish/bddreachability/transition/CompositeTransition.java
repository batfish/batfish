package org.batfish.bddreachability.transition;

import net.sf.javabdd.BDD;

/** The functional composition of two transitions */
final class CompositeTransition implements Transition {
  private final Transition _first;
  private final Transition _second;

  CompositeTransition(Transition first, Transition second) {
    _first = first;
    _second = second;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return _second.transitForward(_first.transitForward(bdd));
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return _first.transitBackward(_second.transitBackward(bdd));
  }
}
