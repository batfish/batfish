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

  RemoveOutgoingInterfaceConstraints(BDDOutgoingOriginalFlowFilterManager mgr) {
    _mgr = mgr;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    // Ensure BDD is correctly constrained, then clear outgoingOriginalFlowFilterConstraints
    assert bdd.and(_mgr.outgoingOriginalFlowFiltersConstraint()).equals(bdd);
    return _mgr.erase(bdd);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    // Ensure BDD's egress interface is not already constrained
    assert bdd.equals(_mgr.erase(bdd));
    return bdd;
  }
}
