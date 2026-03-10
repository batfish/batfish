package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;

/**
 * Matches a {@link org.batfish.datamodel.bgp.community.Community} iff it is a vpn-distinguisher
 * extended community.
 */
public final class VpnDistinguisherExtendedCommunities extends CommunityMatchExpr {

  public static @Nonnull VpnDistinguisherExtendedCommunities instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof VpnDistinguisherExtendedCommunities;
  }

  @Override
  public int hashCode() {
    return 0x6C7B5BC3; // randomly generated
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitVpnDistinguisherExtendedCommunities(this, arg);
  }

  private static final VpnDistinguisherExtendedCommunities INSTANCE =
      new VpnDistinguisherExtendedCommunities();

  @JsonCreator
  private static @Nonnull VpnDistinguisherExtendedCommunities create() {
    return INSTANCE;
  }

  private VpnDistinguisherExtendedCommunities() {}
}
