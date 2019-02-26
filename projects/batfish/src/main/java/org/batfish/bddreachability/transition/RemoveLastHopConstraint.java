package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import net.sf.javabdd.BDD;
import org.batfish.bddreachability.LastHopOutgoingInterfaceManager;

/** A {@link Transition} that removes a last-hop constraint. */
final class RemoveLastHopConstraint implements Transition {
  private final LastHopOutgoingInterfaceManager _mgr;

  RemoveLastHopConstraint(LastHopOutgoingInterfaceManager mgr, String node) {
    checkArgument(
        mgr.isTrackedReceivingNode(node),
        "LastHopOutgoingInterfaceManager doeesn't track %s. Use IDENTITY instead",
        node);
    _mgr = mgr;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return _mgr.existsLastHop(bdd);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    assert !_mgr.hasLastHopConstraint(bdd);
    return bdd;
  }
}
