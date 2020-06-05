package org.batfish.bddreachability.transition;

import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDOutgoingOriginalFlowFilterManager;

/**
 * {@link Transition} to remove constraints on flows' egress interfaces. Such constraints may be
 * imposed by e.g. {@link AddOutgoingOriginalFlowFiltersConstraint}.
 */
@ParametersAreNonnullByDefault
public final class RemoveOutgoingInterfaceConstraints implements Transition {
  private final BDDOutgoingOriginalFlowFilterManager _mgr;

  private RemoveOutgoingInterfaceConstraints(BDDOutgoingOriginalFlowFilterManager mgr) {
    // If manager is trivial, should use identity transition instead. This depends on the invariant
    // that the manager's permittedByOriginalFlowEgressFilter and deniedByOriginalFlowEgressFilter
    // methods will return ONE and ZERO respectively for trivial managers.
    assert !mgr.isTrivial();
    _mgr = mgr;
  }

  static Transition removeOutgoingInterfaceConstraints(BDDOutgoingOriginalFlowFilterManager mgr) {
    return mgr.isTrivial() ? Identity.INSTANCE : new RemoveOutgoingInterfaceConstraints(mgr);
  }

  @Override
  public BDD transitForward(BDD bdd) {
    // Ensure BDD is correctly constrained, then clear outgoingOriginalFlowFilterConstraints
    assert !_mgr.erase(bdd).equals(bdd);
    return _mgr.erase(bdd);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    // Ensure BDD's egress interface is not already constrained
    assert bdd.equals(_mgr.erase(bdd));
    return bdd;
  }
}
