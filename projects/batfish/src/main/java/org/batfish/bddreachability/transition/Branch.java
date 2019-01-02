package org.batfish.bddreachability.transition;

import net.sf.javabdd.BDD;

/** A transition that branches to two other transitions */
public final class Branch implements Transition {
  private final BDD _trueGuard;
  private final BDD _falseGuard;
  private final Transition _trueBranch;
  private final Transition _falseBranch;

  Branch(BDD trueGuard, Transition trueBranch, Transition falseBranch) {
    _trueGuard = trueGuard;
    _falseGuard = trueGuard.not();
    _trueBranch = trueBranch;
    _falseBranch = falseBranch;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    BDD trueIn = bdd.and(_trueGuard);
    BDD falseIn = bdd.and(_falseGuard);

    BDD trueOut = trueIn.isZero() ? trueIn : _trueBranch.transitForward(trueIn);
    BDD falseOut = falseIn.isZero() ? falseIn : _falseBranch.transitForward(falseIn);

    return trueOut.or(falseOut);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    BDD trueBranchIn = _trueGuard.and(_trueBranch.transitBackward(bdd));
    BDD falseBranchIn = _falseGuard.and(_falseBranch.transitBackward(bdd));
    return trueBranchIn.or(falseBranchIn);
  }
}
