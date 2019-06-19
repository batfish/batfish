package org.batfish.bddreachability.transition;

import net.sf.javabdd.BDD;

/** A transition that permits all flows unmodified. */
public final class Identity implements Transition {
  public static Identity INSTANCE = new Identity();

  private Identity() {}

  @Override
  public String toString() {
    return "IDENTITY";
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return bdd;
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return bdd;
  }
}
