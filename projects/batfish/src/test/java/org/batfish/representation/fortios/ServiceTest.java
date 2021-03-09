package org.batfish.representation.fortios;

import static org.batfish.representation.fortios.Service.DEFAULT_PROTOCOL_NUMBER;
import static org.batfish.representation.fortios.Service.DEFAULT_SOURCE_PORT_RANGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TraceElement;
import org.junit.Test;

public class ServiceTest {

  private static final BddTestbed BDD_TESTBED;
  private static final IpAccessListToBdd ACL_TO_BDD;
  private static final HeaderSpaceToBDD HS_TO_BDD;

  static {
    BDD_TESTBED = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());
    ACL_TO_BDD = BDD_TESTBED.getAclToBdd();
    HS_TO_BDD = BDD_TESTBED.getHsToBdd();
  }

  @Test
  public void testToMatchExpr_tcpUdpSctp_defaults() {
    // Default service matches all TCP/UDP/SCTP
    Service service = new Service("name");
    BDD expected =
        HS_TO_BDD.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.SCTP)
                .setSrcPorts(DEFAULT_SOURCE_PORT_RANGE.getSubRanges())
                .build());
    assertThat(ACL_TO_BDD.toBdd(service.toMatchExpr()), equalTo(expected));
    // behavior is the same if protocol is explicit
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    assertThat(ACL_TO_BDD.toBdd(service.toMatchExpr()), equalTo(expected));
  }

  @Test
  public void testToMatchExpr_tcpUdpSctp_oneCustom() {
    IntegerSpace tcpSrcPorts = IntegerSpace.of(1);
    IntegerSpace tcpDstPorts = IntegerSpace.of(2);
    Service service = new Service("name");
    service.setTcpPortRangeSrc(tcpSrcPorts);
    service.setTcpPortRangeDst(tcpDstPorts);
    BDD tcp =
        HS_TO_BDD.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setSrcPorts(tcpSrcPorts.getSubRanges())
                .setDstPorts(tcpDstPorts.getSubRanges())
                .build());
    BDD expected =
        HS_TO_BDD
            .toBDD(
                HeaderSpace.builder()
                    .setIpProtocols(IpProtocol.UDP, IpProtocol.SCTP)
                    .setSrcPorts(DEFAULT_SOURCE_PORT_RANGE.getSubRanges())
                    .build())
            .or(tcp);
    assertThat(ACL_TO_BDD.toBdd(service.toMatchExpr()), equalTo(expected));
    // behavior is the same if protocol is explicit
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    //    assertThat(ACL_TO_BDD.toBdd(service.toMatchExpr()), equalTo(expected));
  }

  @Test
  public void testToMatchExpr_tcpUdpSctp_allCustom() {
    IntegerSpace tcpSrcPorts = IntegerSpace.of(1);
    IntegerSpace tcpDstPorts = IntegerSpace.of(2);
    IntegerSpace udpSrcPorts = IntegerSpace.of(3);
    IntegerSpace udpDstPorts = IntegerSpace.of(4);
    IntegerSpace sctpSrcPorts = IntegerSpace.of(5);
    IntegerSpace sctpDstPorts = IntegerSpace.of(6);
    Service service = new Service("name");
    service.setTcpPortRangeSrc(tcpSrcPorts);
    service.setTcpPortRangeDst(tcpDstPorts);
    service.setUdpPortRangeSrc(udpSrcPorts);
    service.setUdpPortRangeDst(udpDstPorts);
    service.setSctpPortRangeSrc(sctpSrcPorts);
    service.setSctpPortRangeDst(sctpDstPorts);
    BDD tcp =
        HS_TO_BDD.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setSrcPorts(tcpSrcPorts.getSubRanges())
                .setDstPorts(tcpDstPorts.getSubRanges())
                .build());
    BDD udp =
        HS_TO_BDD.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.UDP)
                .setSrcPorts(udpSrcPorts.getSubRanges())
                .setDstPorts(udpDstPorts.getSubRanges())
                .build());
    BDD sctp =
        HS_TO_BDD.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.SCTP)
                .setSrcPorts(sctpSrcPorts.getSubRanges())
                .setDstPorts(sctpDstPorts.getSubRanges())
                .build());
    BDD expected = tcp.or(udp).or(sctp);
    assertThat(ACL_TO_BDD.toBdd(service.toMatchExpr()), equalTo(expected));
    // behavior is the same if protocol is explicit
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    assertThat(ACL_TO_BDD.toBdd(service.toMatchExpr()), equalTo(expected));
  }

  @Test
  public void testToMatchExpr_icmp_defaults() {
    Service service = new Service("name");
    service.setProtocol(Service.Protocol.ICMP);
    assertThat(
        ACL_TO_BDD.toBdd(service.toMatchExpr()),
        equalTo(HS_TO_BDD.toBDD(HeaderSpace.builder().setIpProtocols(IpProtocol.ICMP).build())));
  }

  @Test
  public void testToMatchExpr_icmp6_defaults() {
    Service service = new Service("name");
    service.setProtocol(Service.Protocol.ICMP6);
    assertThat(
        ACL_TO_BDD.toBdd(service.toMatchExpr()),
        equalTo(
            HS_TO_BDD.toBDD(HeaderSpace.builder().setIpProtocols(IpProtocol.IPV6_ICMP).build())));
  }

  @Test
  public void testToMatchExpr_icmp_custom() {
    int icmpCode = 1;
    int icmpType = 2;
    Service service = new Service("name");
    service.setProtocol(Service.Protocol.ICMP);
    service.setIcmpCode(icmpCode);
    service.setIcmpType(icmpType);
    assertThat(
        ACL_TO_BDD.toBdd(service.toMatchExpr()),
        equalTo(
            HS_TO_BDD.toBDD(
                HeaderSpace.builder()
                    .setIpProtocols(IpProtocol.ICMP)
                    .setIcmpCodes(icmpCode)
                    .setIcmpTypes(icmpType)
                    .build())));
  }

  @Test
  public void testToMatchExpr_icmp6_custom() {
    int icmpCode = 1;
    int icmpType = 2;
    Service service = new Service("name");
    service.setProtocol(Service.Protocol.ICMP6);
    service.setIcmpCode(icmpCode);
    service.setIcmpType(icmpType);
    assertThat(
        ACL_TO_BDD.toBdd(service.toMatchExpr()),
        equalTo(
            HS_TO_BDD.toBDD(
                HeaderSpace.builder()
                    .setIpProtocols(IpProtocol.IPV6_ICMP)
                    .setIcmpCodes(icmpCode)
                    .setIcmpTypes(icmpType)
                    .build())));
  }

  @Test
  public void testToMatchExpr_ip_default() {
    Service service = new Service("name");
    service.setProtocol(Service.Protocol.IP);
    assertThat(
        ACL_TO_BDD.toBdd(service.toMatchExpr()),
        equalTo(
            HS_TO_BDD.toBDD(
                HeaderSpace.builder()
                    .setIpProtocols(IpProtocol.fromNumber(DEFAULT_PROTOCOL_NUMBER))
                    .build())));
  }

  @Test
  public void testToMatchExpr_ip_custom() {
    int protocolNumber = 88;
    Service service = new Service("name");
    service.setProtocol(Service.Protocol.IP);
    service.setProtocolNumber(protocolNumber);
    assertThat(
        ACL_TO_BDD.toBdd(service.toMatchExpr()),
        equalTo(
            HS_TO_BDD.toBDD(
                HeaderSpace.builder()
                    .setIpProtocols(IpProtocol.fromNumber(protocolNumber))
                    .build())));
  }

  @Test
  public void testToMatchExpr_traceElement() {
    String svcName = "name";
    Service service = new Service(svcName);
    assertThat(
        service.toMatchExpr().getTraceElement(),
        equalTo(TraceElement.of("Matched service " + svcName)));

    // With comment
    String comment = "you can't go there";
    service.setComment(comment);
    assertThat(
        service.toMatchExpr().getTraceElement(),
        equalTo(TraceElement.of(String.format("Matched service %s: %s", svcName, comment))));
  }
}
