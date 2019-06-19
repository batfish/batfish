package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import net.sf.javabdd.BDD;
import org.batfish.bddreachability.LastHopOutgoingInterfaceManager;

/**
 * A {@link Transition} that constrains the last-hop variable to be the value indicating no last hop
 * (i.e. for originating entering an interface).
 */
public final class AddNoLastHopConstraint implements Transition {
  private final LastHopOutgoingInterfaceManager _mgr;
  private final BDD _noLastHopConstraint;

  AddNoLastHopConstraint(LastHopOutgoingInterfaceManager mgr, String recvNode, String recvIface) {
    checkArgument(
        mgr.isTrackedReceivingNode(recvNode),
        "LastHopOutgoingInterfaceManager doesn't track %s. Use IDENTITY instead",
        recvNode);
    _noLastHopConstraint = mgr.getNoLastHopOutgoingInterfaceBdd(recvNode, recvIface);
    _mgr = mgr;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    assert !_mgr.hasLastHopConstraint(bdd);
    return bdd.and(_noLastHopConstraint);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    /* Unlike BDDSourceManager transitions, we don't require the LastHopOutgoingInterfaceManager
     * variable to be constrained inside the node.
     */
    return _mgr.existsLastHop(bdd);
  }
}
