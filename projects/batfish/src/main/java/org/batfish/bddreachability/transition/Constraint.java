package org.batfish.bddreachability.transition;

import net.sf.javabdd.BDD;

/** A transition that applies a constraint. */
public class Constraint implements Transition {
  private final BDD _constraint;

  Constraint(BDD constraint) {
    _constraint = constraint;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return _constraint.and(bdd);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return _constraint.and(bdd);
  }
}
