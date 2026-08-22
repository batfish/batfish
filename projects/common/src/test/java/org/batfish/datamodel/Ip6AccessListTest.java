package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests IPv6 access-list matching semantics. */
public final class Ip6AccessListTest {

  private static Flow6 tcpFlow(
      String src,
      String dst,
      int srcPort,
      int dstPort) {
    return Flow6.builder()
        .setIngressNode("n1")
        .setSrcIp(Ip6.parse(src))
        .setDstIp(Ip6.parse(dst))
        .setIpProtocol(IpProtocol.TCP)
        .setSrcPort(srcPort)
        .setDstPort(dstPort)
        .setPacketLength(80)
        .build();
  }

  @Test
  public void testFirstMatchAndImplicitDeny() {
    Ip6AccessListLine denySsh =
        Ip6AccessListLine.builder()
            .setName("deny ssh")
            .setAction(LineAction.DENY)
            .setProtocol(IpProtocol.TCP)
            .setDstPrefix(
                Prefix6.parse(
                    "2001:db8:20::/64"))
            .setDstPorts(
                SubRange.singleton(22))
            .build();

    Ip6AccessListLine permitHttps =
        Ip6AccessListLine.builder()
            .setName("permit https")
            .setAction(LineAction.PERMIT)
            .setSrcPrefix(
                Prefix6.parse(
                    "2001:db8:10::/64"))
            .setDstPrefix(
                Prefix6.parse(
                    "2001:db8:20::/64"))
            .setProtocol(IpProtocol.TCP)
            .setDstPorts(
                SubRange.singleton(443))
            .build();

    Ip6AccessList acl =
        Ip6AccessList.builder()
            .setName("V6-IN")
            .setLines(
                denySsh,
                permitHttps)
            .build();

    FilterResult denied =
        acl.filter(
            tcpFlow(
                "2001:db8:10::1",
                "2001:db8:20::1",
                12345,
                22));

    assertThat(
        denied.getAction(),
        equalTo(LineAction.DENY));
    assertThat(
        denied.getMatchLine(),
        equalTo(0));

    FilterResult permitted =
        acl.filter(
            tcpFlow(
                "2001:db8:10::1",
                "2001:db8:20::1",
                12345,
                443));

    assertThat(
        permitted.getAction(),
        equalTo(LineAction.PERMIT));
    assertThat(
        permitted.getMatchLine(),
        equalTo(1));

    FilterResult implicitDeny =
        acl.filter(
            tcpFlow(
                "2001:db8:30::1",
                "2001:db8:20::1",
                12345,
                443));

    assertThat(
        implicitDeny.getAction(),
        equalTo(LineAction.DENY));
    assertThat(
        implicitDeny.getMatchLine(),
        equalTo(null));
  }

  @Test
  public void testPortRange() {
    Ip6AccessList acl =
        Ip6AccessList.builder()
            .setName("WEB")
            .setLines(
                Ip6AccessListLine.builder()
                    .setAction(
                        LineAction.PERMIT)
                    .setProtocol(
                        IpProtocol.TCP)
                    .setDstPorts(
                        new SubRange(
                            8000, 8999))
                    .build())
            .build();

    assertThat(
        acl.filter(
                tcpFlow(
                    "2001:db8:1::1",
                    "2001:db8:2::1",
                    40000,
                    8443))
            .getAction(),
        equalTo(LineAction.PERMIT));

    assertThat(
        acl.filter(
                tcpFlow(
                    "2001:db8:1::1",
                    "2001:db8:2::1",
                    40000,
                    9443))
            .getAction(),
        equalTo(LineAction.DENY));
  }

  @Test
  public void testIcmpv6Match() {
    Ip6AccessList acl =
        Ip6AccessList.builder()
            .setName("ICMP6")
            .setLines(
                Ip6AccessListLine.builder()
                    .setAction(
                        LineAction.PERMIT)
                    .setProtocol(
                        IpProtocol.IPV6_ICMP)
                    .setIcmpType(128)
                    .setIcmpCode(0)
                    .build())
            .build();

    Flow6 echoRequest =
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
        acl.filter(echoRequest)
            .getAction(),
        equalTo(LineAction.PERMIT));

    Flow6 echoReply =
        echoRequest.toBuilder()
            .setIcmpType(129)
            .build();

    assertThat(
        acl.filter(echoReply)
            .getAction(),
        equalTo(LineAction.DENY));
  }
}
