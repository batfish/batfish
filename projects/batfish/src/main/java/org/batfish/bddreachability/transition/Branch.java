package org.batfish.bddreachability.transition;

import net.sf.javabdd.BDD;

/** A transition that branches to two other transitions */
public final class Branch implements Transition {
  private final BDD _guard;
  private final BDD _notGuard; // cached result of _guard.not()
  private final Transition _trueBranch;
  private final Transition _falseBranch;

  Branch(BDD guard, Transition trueBranch, Transition falseBranch) {
    _guard = guard;
    _notGuard = guard.not();
    _trueBranch = trueBranch;
    _falseBranch = falseBranch;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    BDD trueIn = bdd.and(_guard);
    BDD falseIn = bdd.and(_notGuard);

    BDD trueOut = trueIn.isZero() ? trueIn : _trueBranch.transitForward(trueIn);
    BDD falseOut = falseIn.isZero() ? falseIn : _falseBranch.transitForward(falseIn);

    return trueOut.or(falseOut);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    BDD trueBranchIn = _guard.and(_trueBranch.transitBackward(bdd));
    BDD falseBranchIn = _notGuard.and(_falseBranch.transitBackward(bdd));
    return trueBranchIn.or(falseBranchIn);
  }
}
