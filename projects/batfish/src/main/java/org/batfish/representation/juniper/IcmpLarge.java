package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Represents a {@code ScreenOption} checking large icmp packets */
public final class IcmpLarge implements ScreenOption {

  private static final String ICMP_LARGE = "icmp large";
  private static final int LARGEST_ICMP_PACKET_LENGTH = 1024;

  public static final IcmpLarge INSTANCE = new IcmpLarge();

  private static final AclLineMatchExpr ACL_LINE_MATCH_EXPR =
      AclLineMatchExprs.match(
          HeaderSpace.builder()
              .setIpProtocols(ImmutableList.of(IpProtocol.ICMP))
              .setNotPacketLengths(ImmutableList.of(new SubRange(0, LARGEST_ICMP_PACKET_LENGTH)))
              .build());

  private IcmpLarge() {}

  @Override
  public String getName() {
    return ICMP_LARGE;
  }

  @Override
  public AclLineMatchExpr getAclLineMatchExpr() {
    return ACL_LINE_MATCH_EXPR;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof IcmpLarge;
  }

  @Override
  public int hashCode() {
    return IcmpLarge.class.getCanonicalName().hashCode();
  }
}
