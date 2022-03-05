package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToBridgedL3;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2Vni;
import org.batfish.common.topology.bridge_domain.edge.BridgedL3ToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2ToPhysical;
import org.batfish.common.topology.bridge_domain.edge.L2VniToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.NonBridgedL3ToPhysical;
import org.batfish.common.topology.bridge_domain.edge.PhysicalToL2;

/** The identity {@link StateFunction}. */
public final class Identity
    implements BridgeDomainToL2.Function,
        BridgeDomainToL2Vni.Function,
        BridgeDomainToBridgedL3.Function,
        L2ToPhysical.Function,
        L2VniToBridgeDomain.Function,
        BridgedL3ToBridgeDomain.Function,
        PhysicalToL2.Function,
        NonBridgedL3ToPhysical.Function,
        FilterByOuterTag,
        FilterByVlanId,
        PopTag,
        TranslateVlan {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitIdentity(this, arg);
  }

  static @Nonnull Identity instance() {
    return INSTANCE;
  }

  private static final Identity INSTANCE = new Identity();
}
