package org.batfish.representation.fortios;

import static org.batfish.representation.fortios.FortiosPolicyConversions.toMatchExpr;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchServiceGroupTraceElement;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchServiceTraceElement;
import static org.batfish.representation.fortios.Service.DEFAULT_SOURCE_PORT_RANGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.junit.Test;

public class FortiosConfigurationTest {

  private final BddTestbed _bddTestbed = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());
  private final BDD _one = _bddTestbed.getPkt().getFactory().one();
  private final IpAccessListToBdd _aclToBdd = _bddTestbed.getAclToBdd();
  private final HeaderSpaceToBDD _hsToBdd = _bddTestbed.getHsToBdd();

  @Test
  public void testToMatchExpr_tcpUdpSctp_oneCustom() {
    IntegerSpace tcpDstPorts = IntegerSpace.of(1);
    Service service = new Service("name", new BatfishUUID(1));
    service.setTcpPortRangeDst(tcpDstPorts);
    BDD tcp =
        _hsToBdd.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setSrcPorts(DEFAULT_SOURCE_PORT_RANGE.getSubRanges())
                .setDstPorts(tcpDstPorts.getSubRanges())
                .build());
    assertConverts(service, tcp);
    // behavior is the same if protocol is explicit
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    assertConverts(service, tcp);
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
    BDD expected = _hsToBdd.toBDD(tcp).or(_hsToBdd.toBDD(udp)).or(_hsToBdd.toBDD(sctp));
    assertConverts(service, expected);
    // behavior is the same if protocol is explicit
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    assertConverts(service, expected);
  }

  @Test
  public void testToMatchExpr_icmp_defaults() {
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.ICMP);
    HeaderSpace expected = HeaderSpace.builder().setIpProtocols(IpProtocol.ICMP).build();
    assertConverts(service, _hsToBdd.toBDD(expected));
  }

  @Test
  public void testToMatchExpr_icmp6_defaults() {
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.ICMP6);
    HeaderSpace expected = HeaderSpace.builder().setIpProtocols(IpProtocol.IPV6_ICMP).build();
    assertConverts(service, _hsToBdd.toBDD(expected));
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
    assertConverts(service, _hsToBdd.toBDD(expected));
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
    assertConverts(service, _hsToBdd.toBDD(expected));
  }

  @Test
  public void testToMatchExpr_ip_default() {
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.IP);
    assertConverts(service, _one);
  }

  @Test
  public void testToMatchExpr_ip_custom() {
    int protocolNumber = 88;
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.IP);
    service.setProtocolNumber(protocolNumber);
    HeaderSpace expected =
        HeaderSpace.builder().setIpProtocols(IpProtocol.fromNumber(protocolNumber)).build();
    assertConverts(service, _hsToBdd.toBDD(expected));
  }

  @Test
  public void testToMatchExpr_serviceGroup() {
    IntegerSpace tcpSrcPorts = IntegerSpace.of(1);
    IntegerSpace tcpDstPorts = IntegerSpace.of(2);
    IntegerSpace udpSrcPorts = IntegerSpace.of(3);
    IntegerSpace udpDstPorts = IntegerSpace.of(4);
    IntegerSpace sctpSrcPorts = IntegerSpace.of(5);
    IntegerSpace sctpDstPorts = IntegerSpace.of(6);
    Service serviceTcp = new Service("service_tcp", new BatfishUUID(1));
    serviceTcp.setTcpPortRangeSrc(tcpSrcPorts);
    serviceTcp.setTcpPortRangeDst(tcpDstPorts);

    Service serviceUdp = new Service("service_udp", new BatfishUUID(2));
    serviceUdp.setUdpPortRangeSrc(udpSrcPorts);
    serviceUdp.setUdpPortRangeDst(udpDstPorts);

    Service serviceSctp = new Service("service_sctp", new BatfishUUID(3));
    serviceSctp.setSctpPortRangeSrc(sctpSrcPorts);
    serviceSctp.setSctpPortRangeDst(sctpDstPorts);

    ServiceGroup serviceGroupChild = new ServiceGroup("service_group_child", new BatfishUUID(4));
    serviceGroupChild.setMember(ImmutableSet.of("service_tcp", "service_udp"));

    ServiceGroup serviceGroupParent = new ServiceGroup("service_group_parent", new BatfishUUID(5));
    serviceGroupParent.setMember(ImmutableSet.of("service_group_child", "service_sctp"));

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

    // SCTP directly from parent, TCP and UDP indirectly from child
    BDD expected = _hsToBdd.toBDD(tcp).or(_hsToBdd.toBDD(udp)).or(_hsToBdd.toBDD(sctp));
    assertConverts(
        serviceGroupParent,
        ImmutableSet.of(serviceTcp, serviceUdp, serviceSctp, serviceGroupChild, serviceGroupParent),
        expected);
  }

  @Test
  public void testToMatchExpr_serviceGroupTraceElement() {
    String filename = "filename";
    FortiosConfiguration c = new FortiosConfiguration();
    c.setWarnings(new Warnings());
    c.setFilename(filename);

    Service service1 = new Service("service1", new BatfishUUID(1));
    service1.setProtocol(Service.Protocol.ICMP);
    Service service2 = new Service("service2", new BatfishUUID(2));
    service2.setProtocol(Service.Protocol.ICMP);
    Service service3 = new Service("service3", new BatfishUUID(3));
    service3.setProtocol(Service.Protocol.ICMP);
    ServiceGroup serviceGroupChild = new ServiceGroup("service_group_child", new BatfishUUID(4));
    serviceGroupChild.setMember(ImmutableSet.of("service1", "service2"));
    ServiceGroup serviceGroupParent = new ServiceGroup("service_group_parent", new BatfishUUID(5));
    serviceGroupParent.setMember(ImmutableSet.of("service_group_child", "service3"));

    assertThat(
        toMatchExpr(
                serviceGroupParent,
                ImmutableMap.of(
                    service1.getName(),
                    service1,
                    service2.getName(),
                    service2,
                    service3.getName(),
                    service3,
                    serviceGroupChild.getName(),
                    serviceGroupChild,
                    serviceGroupParent.getName(),
                    serviceGroupParent),
                filename)
            .getTraceElement(),
        equalTo(matchServiceGroupTraceElement(serviceGroupParent, filename)));
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
    assertThat(
        toMatchExpr(service, null, filename).getTraceElement(),
        equalTo(matchServiceTraceElement(service, filename)));
  }

  /**
   * Asserts that when converted, the given {@link Service} will exactly match the provided {@link
   * BDD}.
   */
  private void assertConverts(Service service, BDD expected) {
    assertThat(_aclToBdd.toBdd(toMatchExpr(service, null, null)), equalTo(expected));
  }

  /**
   * Asserts that when converted, the given {@link ServiceGroup} will exactly match the provided
   * {@link BDD}.
   */
  private void assertConverts(
      ServiceGroup serviceGroup, Set<ServiceGroupMember> allServiceGroupMembers, BDD expected) {
    assertThat(
        _aclToBdd.toBdd(
            toMatchExpr(
                serviceGroup,
                allServiceGroupMembers.stream()
                    .collect(
                        ImmutableMap.toImmutableMap(
                            ServiceGroupMember::getName, Functions.identity())),
                null)),
        equalTo(expected));
  }
}
