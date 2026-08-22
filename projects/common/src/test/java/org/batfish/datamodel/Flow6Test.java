package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests IPv6 packet-flow model. */
public final class Flow6Test {

  @Test
  public void testTcpFlowAndSerialization() {
    Flow6 flow =
        Flow6.builder()
            .setIngressNode("n1")
            .setIngressVrf("blue")
            .setIngressInterface("Ethernet1")
            .setSrcIp(
                Ip6.parse(
                    "2001:db8:1::10"))
            .setDstIp(
                Ip6.parse(
                    "2001:db8:2::20"))
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(12345)
            .setDstPort(443)
            .setDscp(10)
            .setEcn(2)
            .setPacketLength(80)
            .build();

    assertThat(
        flow.getIngressNode(),
        equalTo("n1"));
    assertThat(
        flow.getIngressVrf(),
        equalTo("blue"));
    assertThat(
        flow.getIpProtocol(),
        equalTo(IpProtocol.TCP));
    assertThat(
        flow.getDstPort(),
        equalTo(443));

    assertThat(
        BatfishObjectMapper.clone(
            flow,
            Flow6.class),
        equalTo(flow));
  }

  @Test
  public void testIcmpv6Flow() {
    Flow6 flow =
        Flow6.builder()
            .setIngressNode("n1")
            .setSrcIp(
                Ip6.parse(
                    "2001:db8:1::1"))
            .setDstIp(
                Ip6.parse(
                    "2001:db8:2::1"))
            .setIpProtocol(
                IpProtocol.IPV6_ICMP)
            .setIcmpType(128)
            .setIcmpCode(0)
            .build();

    assertThat(
        flow.getIcmpType(),
        equalTo(128));
    assertThat(
        flow.getIcmpCode(),
        equalTo(0));
  }

  @Test
  public void testTcpRequiresPorts() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Flow6.builder()
                .setIngressNode("n1")
                .setIpProtocol(
                    IpProtocol.TCP)
                .build());
  }

  @Test
  public void testIcmpv6RequiresTypeAndCode() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Flow6.builder()
                .setIngressNode("n1")
                .setIpProtocol(
                    IpProtocol.IPV6_ICMP)
                .build());
  }

  @Test
  public void testNonPortProtocolClearsPorts() {
    Flow6 flow =
        Flow6.builder()
            .setIngressNode("n1")
            .setIpProtocol(
                IpProtocol.OSPF)
            .setSrcPort(1234)
            .setDstPort(5678)
            .build();

    assertThat(
        flow.getSrcPort(),
        equalTo(null));
    assertThat(
        flow.getDstPort(),
        equalTo(null));
  }
}
