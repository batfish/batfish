package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Represents a {@code ScreenOption} checking TCP flags with fin set but ack unset */
public final class TcpFinNoAck implements ScreenOption {

  private static final String TCP_FIN_NO_ACK = "tcp fin no ack";

  public static final TcpFinNoAck INSTANCE = new TcpFinNoAck();

  private static final AclLineMatchExpr ACL_LINE_MATCH_EXPR =
      AclLineMatchExprs.match(
          HeaderSpace.builder()
              .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
              .setTcpFlags(
                  ImmutableList.of(
                      TcpFlagsMatchConditions.builder()
                          .setTcpFlags(TcpFlags.builder().setAck(false).setFin(true).build())
                          .setUseAck(true)
                          .setUseFin(true)
                          .build()))
              .build());

  private TcpFinNoAck() {}

  @Override
  public String getName() {
    return TCP_FIN_NO_ACK;
  }

  @Override
  public AclLineMatchExpr getAclLineMatchExpr() {
    return ACL_LINE_MATCH_EXPR;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TcpFinNoAck;
  }

  @Override
  public int hashCode() {
    return TcpFinNoAck.class.getCanonicalName().hashCode();
  }
}
