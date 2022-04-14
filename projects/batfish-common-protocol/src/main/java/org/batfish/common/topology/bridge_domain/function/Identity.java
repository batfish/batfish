package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L1ToL2;
import org.batfish.common.topology.bridge_domain.edge.L2ToL1;
import org.batfish.common.topology.bridge_domain.edge.L2VniToNonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2VniToVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L3ToL1;
import org.batfish.common.topology.bridge_domain.edge.L3ToNonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L3ToVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL2Vni;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL3;

/** The identity {@link StateFunction}. */
public final class Identity
    implements L2ToL1.Function,
        L2VniToVlanAwareBridgeDomain.Function,
        L2VniToNonVlanAwareBridgeDomain.Function,
        L3ToVlanAwareBridgeDomain.Function,
        L3ToNonVlanAwareBridgeDomain.Function,
        NonVlanAwareBridgeDomainToL2Vni.Function,
        NonVlanAwareBridgeDomainToL3.Function,
        NonVlanAwareBridgeDomainToL2.Function,
        L1ToL2.Function,
        L3ToL1.Function,
        FilterByOuterTag,
        FilterByVlanId,
        PopTag,
        TranslateVlan {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitIdentity(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof Identity;
  }

  @Override
  public int hashCode() {
    return 0xA21565C2; // randomly generated
  }

  static @Nonnull Identity instance() {
    return INSTANCE;
  }

  private static final Identity INSTANCE = new Identity();
}
