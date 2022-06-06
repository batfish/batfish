package org.batfish.bddreachability.transition;

import net.sf.javabdd.BDD;

/** A transition that allows nothing. */
public final class Zero implements Transition {
  public static final Zero INSTANCE = new Zero();

  private Zero() {}

  @Override
  public String toString() {
    return "ZERO";
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return bdd.getFactory().zero();
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return bdd.getFactory().zero();
  }

  @Override
  public <T> T accept(TransitionVisitor<T> visitor) {
    return visitor.visitZero(this);
  }

  private Object readResolve() {
    return INSTANCE;
  }
}
