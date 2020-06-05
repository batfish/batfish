package org.batfish.bddreachability.transition;

import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDOutgoingOriginalFlowFilterManager;

/**
 * {@link Transition} that adds a constraint tracking which interfaces' {@link
 * org.batfish.datamodel.Interface#getOutgoingOriginalFlowFilter() outgoingOriginalFlowFilters}
 * would permit the current (pre-transformation) flow. Later when an egress interface is selected,
 * (post-transformation) flows will already be partitioned based on whether their original flows
 * were permitted by the interface's outgoingOriginalFlowFilter, thanks to this constraint.
 */
@ParametersAreNonnullByDefault
public final class AddOutgoingOriginalFlowFiltersConstraint implements Transition {
  private final BDDOutgoingOriginalFlowFilterManager _mgr;

  private AddOutgoingOriginalFlowFiltersConstraint(BDDOutgoingOriginalFlowFilterManager mgr) {
    // If manager is trivial, should use identity transition instead. This depends on the invariant
    // that egress interface constraints are NOT applied in backwards traversals on nodes with
    // trivial managers.
    assert !mgr.isTrivial();
    _mgr = mgr;
  }

  public static Transition addOutgoingOriginalFlowFiltersConstraint(
      BDDOutgoingOriginalFlowFilterManager mgr) {
    return mgr.isTrivial() ? Identity.INSTANCE : new AddOutgoingOriginalFlowFiltersConstraint(mgr);
  }

  @Override
  public BDD transitForward(BDD bdd) {
    // Ensure the BDD is unconstrained for outgoing interface
    assert bdd.equals(_mgr.erase(bdd));
    return bdd.and(_mgr.outgoingOriginalFlowFiltersConstraint());
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    // No assertion that the BDD is constrained, because there are cases where it legitimately
    // shouldn't be (for example if the final state is NO_ROUTE in this node).
    return _mgr.erase(bdd.and(_mgr.outgoingOriginalFlowFiltersConstraint()));
  }
}
