package net.sf.javabdd;

import com.google.common.base.Objects;

public final class BDDVarPair {
  private final int _oldVar;
  private final int _newVar;

  public BDDVarPair(BDD oldVar, BDD newVar) {
    this(oldVar.var(), newVar.var());
  }

  public BDDVarPair(int oldVar, int newVar) {
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
}
