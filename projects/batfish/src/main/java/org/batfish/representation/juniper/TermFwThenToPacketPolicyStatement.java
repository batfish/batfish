package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.Statement;

/**
 * Converts <i>then</i> statements in the {@link FirewallFilter} to statements in the {@link
 * PacketPolicy}
 *
 * <p><em>Note:</em>It can return {@code null} for statements that must be skipped or we currently
 * do not support. In particular, this visitor is stateful and will skip converting statements after
 * {@link FwThenNextTerm} has been encountered.
 */
public final class TermFwThenToPacketPolicyStatement implements FwThenVisitor<Statement> {

  private final String _vrfToUse;
  private boolean _skipRest;

  /** Create a new converter, with a default VRF to be used for FIB lookups */
  public TermFwThenToPacketPolicyStatement(String vrfToUse) {
    _vrfToUse = vrfToUse;
  }

  @Override
  public Statement visitFwThenAccept(FwThenAccept accept) {
    return _skipRest ? null : new Return(new FibLookup(_vrfToUse));
  }

  @Override
  public Statement visitFwThenDiscard(FwThenDiscard discard) {
    return _skipRest ? null : new Return(Drop.instance());
  }

  @Override
  @Nullable
  public Statement visitFwThenNextIp(FwThenNextIp nextIp) {
    return null;
  }

  @Override
  public Statement visitFwThenNextTerm(FwThenNextTerm accept) {
    _skipRest = true;
    return null;
  }

  @Override
  public Statement visitFwThenNop(FwThenNop nop) {
    return null;
  }

  @Override
  public Statement visitThenRoutingInstance(FwThenRoutingInstance routingInstance) {
    return _skipRest ? null : new Return(new FibLookup(routingInstance.getInstanceName()));
  }
}
