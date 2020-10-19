package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

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

  RemoveOutgoingInterfaceConstraints(BDDOutgoingOriginalFlowFilterManager mgr) {
    // If manager is trivial, should use identity transition instead. This depends on the invariant
    // that the manager's permittedByOriginalFlowEgressFilter and deniedByOriginalFlowEgressFilter
    // methods will return ONE and ZERO respectively for trivial managers.
    checkArgument(
        !mgr.isTrivial(),
        "RemoveOutgoingInterfaceConstraints for a trivial BDDOutgoingOriginalFlowFilterManager."
            + " Use Identity instead");
    _mgr = mgr;
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
