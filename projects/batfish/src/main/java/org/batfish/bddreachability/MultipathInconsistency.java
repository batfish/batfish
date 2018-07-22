package org.batfish.bddreachability;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.z3.expr.StateExpr;

/*
 * Internal class representing a multipath inconsistency.
 */
class MultipathInconsistency {
  private final BDD _bdd;
  private final StateExpr _originateState;
  private final Set<StateExpr> _finalStates;

  MultipathInconsistency(StateExpr originateState, Set<StateExpr> finalStates, BDD bdd) {
    this._originateState = originateState;
    this._finalStates = ImmutableSet.copyOf(finalStates);
    this._bdd = bdd;
  }

  public BDD getBDD() {
    return _bdd;
  }

  public StateExpr getOriginateState() {
    return _originateState;
  }

  public Set<StateExpr> getFinalStates() {
    return _finalStates;
  }
}
