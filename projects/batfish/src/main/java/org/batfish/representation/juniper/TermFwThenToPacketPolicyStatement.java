package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.JuniperConfiguration.toVrfName;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.LiteralVrfName;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.Statement;

/**
 * Converts <em>then</em> statements in the {@link FirewallFilter} to statements in the {@link
 * org.batfish.datamodel.packet_policy.PacketPolicy}
 *
 * <p><strong>Note:</strong>It can return {@code null} for statements that must be skipped or we
 * currently do not support. In particular, this visitor is stateful and will skip converting
 * statements after {@link FwThenNextTerm} has been encountered.
 */
@ParametersAreNonnullByDefault
public final class TermFwThenToPacketPolicyStatement implements FwThenVisitor<Statement> {

  private final String _vrfToUse;
  private boolean _skipRest;

  /** Create a new converter, with a default VRF to be used for FIB lookups */
  private TermFwThenToPacketPolicyStatement(String vrfToUse) {
    _vrfToUse = vrfToUse;
  }

  @Override
  public @Nullable Statement visitFwThenAccept(FwThenAccept accept) {
    return _skipRest ? null : new Return(new FibLookup(new LiteralVrfName(_vrfToUse)));
  }

  @Override
  public @Nullable Statement visitFwThenDiscard(FwThenDiscard discard) {
    return _skipRest ? null : new Return(Drop.instance());
  }

  @Override
  public @Nullable Statement visitFwThenNextIp(FwThenNextIp nextIp) {
    return null;
  }

  @Override
  public @Nullable Statement visitFwThenNextTerm(FwThenNextTerm accept) {
    _skipRest = true;
    return null;
  }

  @Override
  public @Nullable Statement visitFwThenNop(FwThenNop nop) {
    return null;
  }

  @Override
  public @Nullable Statement visitThenRoutingInstance(FwThenRoutingInstance routingInstance) {
    return _skipRest
        ? null
        : new Return(
            new FibLookup(new LiteralVrfName(toVrfName(routingInstance.getInstanceName()))));
  }

  /** Convert all "then" statements in the {@code term} to a list of packet policy statements */
  public static List<Statement> convert(FwTerm term, String vrfName) {
    TermFwThenToPacketPolicyStatement thenConverter =
        new TermFwThenToPacketPolicyStatement(vrfName);

    // Convert "then"s to action statements.
    return term.getThens().stream()
        .map(thenConverter::visit)
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }
}
