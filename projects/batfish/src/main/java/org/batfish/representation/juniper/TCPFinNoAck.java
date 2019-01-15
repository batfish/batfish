package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** Represents a {@code ScreenOption} checking TCP flags with fin set but ack unset */
public final class TCPFinNoAck implements ScreenOption {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String TCP_FIN_NO_ACK = "tcp fin no ack";

  public static final TCPFinNoAck INSTANCE = new TCPFinNoAck();

  private TCPFinNoAck() {}

  @Override
  public String getName() {
    return TCP_FIN_NO_ACK;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    HeaderSpace headerSpace =
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
            .setTcpFlags(
                ImmutableList.of(
                    TcpFlagsMatchConditions.builder()
                        .setTcpFlags(TcpFlags.builder().setAck(false).setFin(true).build())
                        .setUseAck(true)
                        .setUseFin(true)
                        .build()))
            .build();
    return AclLineMatchExprs.match(headerSpace);
  }
}
