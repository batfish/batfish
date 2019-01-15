package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Represents a {@code ScreenOption} checking TCP winnuke */
public final class TCPWinnuke implements ScreenOption {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String TCP_WINNUKE = "tcp winnuke";

  public static final TCPWinnuke INSTANCE = new TCPWinnuke();

  private TCPWinnuke() {}

  @Override
  public String getName() {
    return TCP_WINNUKE;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    HeaderSpace headerSpace =
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
            .setDstPorts(ImmutableList.of(new SubRange(139, 139)))
            .setTcpFlags(
                ImmutableList.of(TcpFlagsMatchConditions.builder().setUseUrg(true).build()))
            .build();
    return AclLineMatchExprs.match(headerSpace);
  }
}
