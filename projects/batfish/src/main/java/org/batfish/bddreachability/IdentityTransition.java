package org.batfish.bddreachability;

import net.sf.javabdd.BDD;

/** A transition that permits all flows unmodified. */
final class IdentityTransition implements Transition {
  public static IdentityTransition INSTANCE = new IdentityTransition();

  private IdentityTransition() {}

  @Override
  public BDD transitForward(BDD bdd) {
    return bdd;
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return bdd;
  }
}
