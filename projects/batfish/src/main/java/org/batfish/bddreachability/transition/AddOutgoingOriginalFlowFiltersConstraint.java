package org.batfish.bddreachability.transition;

import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDOutgoingOriginalFlowFilterManager;
import org.batfish.datamodel.Interface;

/**
 * {@link Transition} that adds a constraint tracking which interfaces' {@link
 * Interface#getOutgoingOriginalFlowFilter() outgoingOriginalFlowFilters} would permit the current
 * (pre-transformation) flow. Later when an egress interface is selected, (post-transformation)
 * flows will already be partitioned based on whether their original flows were permitted by the
 * interface's outgoingOriginalFlowFilter, thanks to this constraint.
 */
public class AddOutgoingOriginalFlowFiltersConstraint implements Transition {
  private final BDDOutgoingOriginalFlowFilterManager _mgr;

  AddOutgoingOriginalFlowFiltersConstraint(BDDOutgoingOriginalFlowFilterManager mgr) {
    // Can't swap this out for an identity transition if the manager is trivial, because even in
    // that case transiting backwards still needs to clear the egress interface constraint.
    _mgr = mgr;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    // Ensure the BDD is unconstrained for outgoing interface
    assert bdd.equals(_mgr.erase(bdd));
    return bdd.and(_mgr.outgoingOriginalFlowFiltersConstraint());
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return _mgr.erase(bdd.and(_mgr.outgoingOriginalFlowFiltersConstraint()));
  }
}
