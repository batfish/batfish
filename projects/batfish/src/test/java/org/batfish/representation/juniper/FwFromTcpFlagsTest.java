package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromTcpFlags} */
public class FwFromTcpFlagsTest {

  @Test
  public void testToAclLineMatchExpr_tcpEstablished() {
    assertEquals(
        FwFromTcpFlags.TCP_ESTABLISHED.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setTcpFlags(
                    ImmutableList.of(
                        TcpFlagsMatchConditions.builder()
                            .setTcpFlags(TcpFlags.builder().setAck(true).build())
                            .setUseAck(true)
                            .build(),
                        TcpFlagsMatchConditions.builder()
                            .setTcpFlags(TcpFlags.builder().setRst(true).build())
                            .setUseRst(true)
                            .build()))
                .build(),
            TraceElement.of("Matched tcp-flags tcp-established")));
  }

  @Test
  public void testToAclLineMatchExpr_tcpInitial() {
    assertEquals(
        FwFromTcpFlags.TCP_INITIAL.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setTcpFlags(
                    ImmutableList.of(
                        TcpFlagsMatchConditions.builder()
                            .setTcpFlags(TcpFlags.builder().setAck(true).setSyn(true).build())
                            .setUseAck(true)
                            .setUseSyn(true)
                            .build()))
                .build(),
            TraceElement.of("Matched tcp-flags tcp-initial")));
  }

  @Test
  public void testToAclLineMatchExpr_tcpFlags() {
    List<TcpFlagsMatchConditions> tcpFlags =
        ImmutableList.of(
            TcpFlagsMatchConditions.ACK_TCP_FLAG, TcpFlagsMatchConditions.RST_TCP_FLAG);
    FwFromTcpFlags from = FwFromTcpFlags.fromTcpFlags(tcpFlags);

    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder().setIpProtocols(IpProtocol.TCP).setTcpFlags(tcpFlags).build(),
            TraceElement.of(
                String.format(
                    "Matched tcp-flags %s %s",
                    TcpFlagsMatchConditions.ACK_TCP_FLAG.toString(),
                    TcpFlagsMatchConditions.RST_TCP_FLAG.toString()))));
  }
}
