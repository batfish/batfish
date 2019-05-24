package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

/** A transition that erases a variable and then constrains it to have a new value. */
public final class EraseAndSet implements Transition {
  private final BDD _eraseVars;
  private final BDD _setValue;

  EraseAndSet(BDD eraseVars, BDD setValue) {
    checkArgument(!setValue.isZero(), "Value is zero BDD. Use ZERO instead");
    checkArgument(
        (!eraseVars.isOne() && !eraseVars.isZero()),
        "No variables to erase. Use Constraint instead");
    _eraseVars = eraseVars;
    _setValue = setValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EraseAndSet)) {
      return false;
    }
    EraseAndSet other = (EraseAndSet) o;
    return _eraseVars.equals(other._eraseVars) && _setValue.equals(other._setValue);
  }

  BDD getEraseVars() {
    return _eraseVars;
  }

  BDD getSetValue() {
    return _setValue;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_eraseVars, _setValue);
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return _setValue.isOne() ? bdd.exist(_eraseVars) : bdd.exist(_eraseVars).and(_setValue);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return _setValue.isOne()
        ? bdd.exist(_eraseVars)
        : bdd.applyEx(_setValue, BDDFactory.and, _eraseVars);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(EraseAndSet.class)
        .add("eraseVars", _eraseVars)
        .add("valueVars", _setValue.support())
        .toString();
  }
}
