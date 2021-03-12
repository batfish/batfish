package org.batfish.representation.fortios;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.representation.fortios.Service.DEFAULT_SOURCE_PORT_RANGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import net.sf.javabdd.BDD;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TraceElement;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class FortiosConfigurationTest {

  private static final BddTestbed BDD_TESTBED;
  private static final BDD ZERO;
  private static final BDD ONE;
  private static final IpAccessListToBdd ACL_TO_BDD;
  private static final HeaderSpaceToBDD HS_TO_BDD;

  static {
    BDD_TESTBED = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());
    ZERO = BDD_TESTBED.getPkt().getFactory().zero();
    ONE = BDD_TESTBED.getPkt().getFactory().one();
    ACL_TO_BDD = BDD_TESTBED.getAclToBdd();
    HS_TO_BDD = BDD_TESTBED.getHsToBdd();
  }

  @Test
  public void testToMatchExpr_tcpUdpSctp_defaults() {
    // Default service with no dst ports specified matches nothing and files warning
    String svcName = "name";
    FortiosConfiguration c = new FortiosConfiguration();
    Service service = new Service(svcName, new BatfishUUID(1));
    Warnings w = new Warnings(true, true, true);
    c.setWarnings(w);
    assertThat(ACL_TO_BDD.toBdd(c.toMatchExpr(service)), equalTo(ZERO));
    assertThat(
        w.getRedFlagWarnings(),
        contains(hasText(String.format("Service %s does not match any packets", svcName))));

    // behavior is the same if protocol is explicit
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    assertThat(ACL_TO_BDD.toBdd(c.toMatchExpr(service)), equalTo(ZERO));
  }

  @Test
  public void testToMatchExpr_tcpUdpSctp_oneCustom() {
    IntegerSpace tcpDstPorts = IntegerSpace.of(1);
    Service service = new Service("name", new BatfishUUID(1));
    service.setTcpPortRangeDst(tcpDstPorts);
    BDD tcp =
        HS_TO_BDD.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setSrcPorts(DEFAULT_SOURCE_PORT_RANGE.getSubRanges())
                .setDstPorts(tcpDstPorts.getSubRanges())
                .build());
    assertConvertsWithoutWarnings(service, tcp);
    // behavior is the same if protocol is explicit
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    assertConvertsWithoutWarnings(service, tcp);
  }

  @Test
  public void testToMatchExpr_tcpUdpSctp_allCustom() {
    IntegerSpace tcpSrcPorts = IntegerSpace.of(1);
    IntegerSpace tcpDstPorts = IntegerSpace.of(2);
    IntegerSpace udpSrcPorts = IntegerSpace.of(3);
    IntegerSpace udpDstPorts = IntegerSpace.of(4);
    IntegerSpace sctpSrcPorts = IntegerSpace.of(5);
    IntegerSpace sctpDstPorts = IntegerSpace.of(6);
    Service service = new Service("name", new BatfishUUID(1));
    service.setTcpPortRangeSrc(tcpSrcPorts);
    service.setTcpPortRangeDst(tcpDstPorts);
    service.setUdpPortRangeSrc(udpSrcPorts);
    service.setUdpPortRangeDst(udpDstPorts);
    service.setSctpPortRangeSrc(sctpSrcPorts);
    service.setSctpPortRangeDst(sctpDstPorts);
    HeaderSpace tcp =
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.TCP)
            .setSrcPorts(tcpSrcPorts.getSubRanges())
            .setDstPorts(tcpDstPorts.getSubRanges())
            .build();
    HeaderSpace udp =
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.UDP)
            .setSrcPorts(udpSrcPorts.getSubRanges())
            .setDstPorts(udpDstPorts.getSubRanges())
            .build();
    HeaderSpace sctp =
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.SCTP)
            .setSrcPorts(sctpSrcPorts.getSubRanges())
            .setDstPorts(sctpDstPorts.getSubRanges())
            .build();
    BDD expected = HS_TO_BDD.toBDD(tcp).or(HS_TO_BDD.toBDD(udp)).or(HS_TO_BDD.toBDD(sctp));
    assertConvertsWithoutWarnings(service, expected);
    // behavior is the same if protocol is explicit
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    assertConvertsWithoutWarnings(service, expected);
  }

  @Test
  public void testToMatchExpr_icmp_defaults() {
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.ICMP);
    HeaderSpace expected = HeaderSpace.builder().setIpProtocols(IpProtocol.ICMP).build();
    assertConvertsWithoutWarnings(service, HS_TO_BDD.toBDD(expected));
  }

  @Test
  public void testToMatchExpr_icmp6_defaults() {
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.ICMP6);
    HeaderSpace expected = HeaderSpace.builder().setIpProtocols(IpProtocol.IPV6_ICMP).build();
    assertConvertsWithoutWarnings(service, HS_TO_BDD.toBDD(expected));
  }

  @Test
  public void testToMatchExpr_icmp_custom() {
    int icmpCode = 1;
    int icmpType = 2;
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.ICMP);
    service.setIcmpCode(icmpCode);
    service.setIcmpType(icmpType);
    HeaderSpace expected =
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.ICMP)
            .setIcmpCodes(icmpCode)
            .setIcmpTypes(icmpType)
            .build();
    assertConvertsWithoutWarnings(service, HS_TO_BDD.toBDD(expected));
  }

  @Test
  public void testToMatchExpr_icmp6_custom() {
    int icmpCode = 1;
    int icmpType = 2;
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.ICMP6);
    service.setIcmpCode(icmpCode);
    service.setIcmpType(icmpType);
    HeaderSpace expected =
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.IPV6_ICMP)
            .setIcmpCodes(icmpCode)
            .setIcmpTypes(icmpType)
            .build();
    assertConvertsWithoutWarnings(service, HS_TO_BDD.toBDD(expected));
  }

  @Test
  public void testToMatchExpr_ip_default() {
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.IP);
    assertConvertsWithoutWarnings(service, ONE);
  }

  @Test
  public void testToMatchExpr_ip_custom() {
    int protocolNumber = 88;
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.IP);
    service.setProtocolNumber(protocolNumber);
    HeaderSpace expected =
        HeaderSpace.builder().setIpProtocols(IpProtocol.fromNumber(protocolNumber)).build();
    assertConvertsWithoutWarnings(service, HS_TO_BDD.toBDD(expected));
  }

  @Test
  public void testToMatchExpr_traceElement() {
    String svcName = "name";
    String filename = "filename";
    Service service = new Service(svcName, new BatfishUUID(1));
    FortiosConfiguration c = new FortiosConfiguration();
    c.setWarnings(new Warnings());
    c.setFilename(filename);
    service.setProtocol(Service.Protocol.ICMP);
    TraceElement.Builder expectedTe =
        TraceElement.builder()
            .add("Matched service ")
            .add(
                service.getName(),
                new VendorStructureId(
                    filename,
                    FortiosStructureType.SERVICE_CUSTOM.getDescription(),
                    service.getName()));
    assertThat(c.toMatchExpr(service).getTraceElement(), equalTo(expectedTe.build()));

    // With comment
    String comment = "you can't go there";
    service.setComment(comment);
    expectedTe.add(String.format("(%s)", comment));
    assertThat(c.toMatchExpr(service).getTraceElement(), equalTo(expectedTe.build()));
  }

  /**
   * Asserts that when converted, the given {@link Service} will exactly match the provided {@link
   * BDD}, without generating conversion warnings.
   */
  private void assertConvertsWithoutWarnings(Service service, BDD expected) {
    Warnings w = new Warnings();
    FortiosConfiguration c = new FortiosConfiguration();
    c.setWarnings(w);
    assertThat(ACL_TO_BDD.toBdd(c.toMatchExpr(service)), equalTo(expected));
    assertThat(w.getRedFlagWarnings(), empty());
  }
}
