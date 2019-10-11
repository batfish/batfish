package org.batfish.representation.juniper;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Represents a {@code ScreenOption} checking unknown protocol */
public final class IpUnknownProtocol implements ScreenOption {

  private static final int LARGEST_VALID_PROTOCOL = 143;
  private static final String IP_UNKNOWN_PROTOCOL = "ip unknown-protocol";

  public static final IpUnknownProtocol INSTANCE = new IpUnknownProtocol();

  private static final AclLineMatchExpr ACL_LINE_MATCH_EXPR =
      AclLineMatchExprs.match(
          HeaderSpace.builder()
              .setNotIpProtocols(
                  IntStream.range(0, LARGEST_VALID_PROTOCOL)
                      .mapToObj(IpProtocol::fromNumber)
                      .collect(Collectors.toList()))
              .build());

  private IpUnknownProtocol() {}

  @Override
  public String getName() {
    return IP_UNKNOWN_PROTOCOL;
  }

  @Override
  public AclLineMatchExpr getAclLineMatchExpr() {
    return ACL_LINE_MATCH_EXPR;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof IpUnknownProtocol;
  }

  @Override
  public int hashCode() {
    return IpUnknownProtocol.class.getCanonicalName().hashCode();
  }
}
