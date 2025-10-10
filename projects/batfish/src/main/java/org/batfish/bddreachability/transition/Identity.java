package org.batfish.bddreachability.transition;

import java.io.Serial;
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
    return bdd.id();
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return bdd.id();
  }

  @Override
  public <T> T accept(TransitionVisitor<T> visitor) {
    return visitor.visitIdentity(this);
  }

  @Serial
  private Object readResolve() {
    return INSTANCE;
  }
}
