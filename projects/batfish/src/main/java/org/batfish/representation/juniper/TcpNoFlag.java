package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Represents a {@code ScreenOption} checking no TCP flags */
public final class TcpNoFlag implements ScreenOption {

  private static final String TCP_NO_FLAG = "tcp no flag";

  public static final TcpNoFlag INSTANCE = new TcpNoFlag();

  private static final AclLineMatchExpr ACL_LINE_MATCH_EXPR =
      AclLineMatchExprs.match(
          HeaderSpace.builder()
              .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
              .setTcpFlags(
                  ImmutableList.of(
                      TcpFlagsMatchConditions.builder()
                          .setTcpFlags(
                              TcpFlags.builder()
                                  .setAck(false)
                                  .setUrg(false)
                                  .setPsh(false)
                                  .setRst(false)
                                  .setSyn(false)
                                  .setFin(false)
                                  .build())
                          .setUseAck(true)
                          .setUseUrg(true)
                          .setUsePsh(true)
                          .setUseRst(true)
                          .setUseSyn(true)
                          .setUseFin(true)
                          .build()))
              .build());

  private TcpNoFlag() {}

  @Override
  public String getName() {
    return TCP_NO_FLAG;
  }

  @Override
  public AclLineMatchExpr getAclLineMatchExpr() {
    return ACL_LINE_MATCH_EXPR;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TcpNoFlag;
  }

  @Override
  public int hashCode() {
    return TcpNoFlag.class.getCanonicalName().hashCode();
  }
}
