package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import net.sf.javabdd.BDD;
import org.batfish.bddreachability.LastHopOutgoingInterfaceManager;

/** A {@link Transition} that adds a last-hop outgoing interface constraint. */
public final class AddLastHopConstraint implements Transition {
  private final LastHopOutgoingInterfaceManager _mgr;
  private final BDD _lastHopConstraint;

  AddLastHopConstraint(
      LastHopOutgoingInterfaceManager mgr,
      String sendingNode,
      String sendingIface,
      String recvNode,
      String recvIface) {
    checkArgument(
        mgr.isTrackedReceivingNode(recvNode),
        "LastHopOutgoingInterfaceManager doeesn't track %s. Use IDENTITY instead",
        recvNode);
    _lastHopConstraint =
        mgr.getLastHopOutgoingInterfaceBdd(sendingNode, sendingIface, recvNode, recvIface);
    _mgr = mgr;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    assert !_mgr.hasLastHopConstraint(bdd);
    return bdd.and(_lastHopConstraint);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return _mgr.existsLastHop(bdd);
  }
}
