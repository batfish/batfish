package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import net.sf.javabdd.BDD;

/** A transition that branches to two other transitions */
public final class Branch implements Transition {
  private final BDD _guard;
  private final Transition _trueBranch;
  private final Transition _falseBranch;

  Branch(BDD guard, Transition trueBranch, Transition falseBranch) {
    checkArgument(!guard.isZero() && !guard.isOne(), "guard cannot be zero or one.");
    _guard = guard;
    _trueBranch = trueBranch;
    _falseBranch = falseBranch;
  }

  BDD getGuard() {
    return _guard;
  }

  Transition getTrueBranch() {
    return _trueBranch;
  }

  Transition getFalseBranch() {
    return _falseBranch;
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
  public int hashCode() {
    return Objects.hash(_guard, _trueBranch, _falseBranch);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Branch)) {
      return false;
    }
    Branch b = (Branch) o;
    return _guard.equals(b._guard)
        && _trueBranch.equals(b._trueBranch)
        && _falseBranch.equals(b._falseBranch);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Branch.class)
        .add("guardTopVar", _guard.var())
        .add("true", _trueBranch)
        .add("false", _falseBranch)
        .toString();
  }
}
