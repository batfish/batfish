package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Represents a {@code ScreenOption} checking TCP winnuke */
public final class TCPSynFin implements ScreenOption {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String TCP_SYN_FIN = "tcp syn fin";

  public static final TCPSynFin INSTANCE = new TCPSynFin();

  private TCPSynFin() {}

  @Override
  public String getName() {
    return TCP_SYN_FIN;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    HeaderSpace headerSpace =
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
            .setTcpFlags(
                ImmutableList.of(
                    TcpFlagsMatchConditions.builder()
                        .setTcpFlags(TcpFlags.builder().setSyn(false).setFin(false).build())
                        .setUseSyn(true)
                        .setUseFin(true)
                        .build()))
            .build();
    return AclLineMatchExprs.match(headerSpace);
  }
}
