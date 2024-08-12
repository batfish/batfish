package net.sf.javabdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Set;

/**
 * A pair of BDD variables (i.e. created by {@link BDDFactory#ithVar(int)}). For building {@link
 * BDDPairing BDDPairings} via {@link BDDFactory#getPair(Set)}. .
 */
public final class BDDVarPair implements Serializable {
  private final int _oldVar;
  private final int _newVar;

  /**
   * Build a {@link BDDVarPair} for two single-variable BDDs (i.e. BDDs returned from {@link
   * BDDFactory#ithVar(int)}.
   */
  public BDDVarPair(BDD oldVar, BDD newVar) {
    this(bddVarId(oldVar), bddVarId(newVar));
  }

  public BDDVarPair(int oldVar, int newVar) {
    // disallow identity pairings -- they are no-ops, but would break caching in BDDFactory#getPair.
    checkArgument(oldVar != newVar, "Cannot pair a variable with itself");
    this._oldVar = oldVar;
    this._newVar = newVar;
  }

  public int getOldVar() {
    return _oldVar;
  }

  public int getNewVar() {
    return _newVar;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BDDVarPair)) {
      return false;
    }
    BDDVarPair that = (BDDVarPair) o;
    return _oldVar == that._oldVar && _newVar == that._newVar;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_oldVar, _newVar);
  }

  private static int bddVarId(BDD bdd) {
    checkArgument(bdd.isVar(), "bdd is not a single variable");
    return bdd.var();
  }
}
