package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Represents a {@code ScreenOption} checking TCP winnuke */
public final class TcpWinnuke implements ScreenOption {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String TCP_WINNUKE = "tcp winnuke";

  public static final TcpWinnuke INSTANCE = new TcpWinnuke();

  private static final AclLineMatchExpr ACL_LINE_MATCH_EXPR = buildAclLineMatchExpr();

  private TcpWinnuke() {}

  @Override
  public String getName() {
    return TCP_WINNUKE;
  }

  static AclLineMatchExpr buildAclLineMatchExpr() {
    HeaderSpace headerSpace =
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
            .setDstPorts(ImmutableList.of(new SubRange(139, 139)))
            .setTcpFlags(
                ImmutableList.of(
                    TcpFlagsMatchConditions.builder()
                        .setTcpFlags(TcpFlags.builder().setUrg(true).build())
                        .setUseUrg(true)
                        .build()))
            .build();
    return AclLineMatchExprs.match(headerSpace);
  }

  @Override
  public AclLineMatchExpr getAclLineMatchExpr() {
    return ACL_LINE_MATCH_EXPR;
  }
}
