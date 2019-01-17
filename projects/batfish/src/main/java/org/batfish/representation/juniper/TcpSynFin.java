package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Represents a {@code ScreenOption} checking both syn and fin flags are set in TCP packets */
public final class TcpSynFin implements ScreenOption {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String TCP_SYN_FIN = "tcp syn fin";

  public static final TcpSynFin INSTANCE = new TcpSynFin();

  private static final AclLineMatchExpr ACL_LINE_MATCH_EXPR = buildAclLineMatchExpr();

  private TcpSynFin() {}

  @Override
  public String getName() {
    return TCP_SYN_FIN;
  }

  static AclLineMatchExpr buildAclLineMatchExpr() {
    HeaderSpace headerSpace =
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
            .setTcpFlags(
                ImmutableList.of(
                    TcpFlagsMatchConditions.builder()
                        .setTcpFlags(TcpFlags.builder().setSyn(true).setFin(true).build())
                        .setUseSyn(true)
                        .setUseFin(true)
                        .build()))
            .build();
    return AclLineMatchExprs.match(headerSpace);
  }

  @Override
  public AclLineMatchExpr getAclLineMatchExpr() {
    return ACL_LINE_MATCH_EXPR;
  }
}
