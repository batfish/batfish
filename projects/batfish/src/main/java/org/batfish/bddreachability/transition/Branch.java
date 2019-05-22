package org.batfish.bddreachability.transition;

import com.google.common.base.MoreObjects;
import net.sf.javabdd.BDD;

/** A transition that branches to two other transitions */
public final class Branch implements Transition {
  private final BDD _guard;
  private final Transition _trueBranch;
  private final Transition _falseBranch;

  Branch(BDD guard, Transition trueBranch, Transition falseBranch) {
    _guard = guard;
    _trueBranch = trueBranch;
    _falseBranch = falseBranch;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    BDD trueIn = bdd.and(_guard);
    BDD falseIn = bdd.diff(_guard);

    BDD trueOut = trueIn.isZero() ? trueIn : _trueBranch.transitForward(trueIn);
    BDD falseOut = falseIn.isZero() ? falseIn : _falseBranch.transitForward(falseIn);

    return trueOut.or(falseOut);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    BDD trueBranchIn = _guard.and(_trueBranch.transitBackward(bdd));
    BDD falseBranchIn = _guard.less(_falseBranch.transitBackward(bdd));
    return trueBranchIn.or(falseBranchIn);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Branch.class)
        .add("true", _trueBranch)
        .add("false", _falseBranch)
        .toString();
  }
}
