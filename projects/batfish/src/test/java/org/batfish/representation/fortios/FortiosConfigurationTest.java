package org.batfish.representation.fortios;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.representation.fortios.FortiosConfiguration.computeVrfName;
import static org.batfish.representation.fortios.FortiosConfiguration.convertAccessList;
import static org.batfish.representation.fortios.FortiosPolicyConversions.toIpSpace;
import static org.batfish.representation.fortios.FortiosPolicyConversions.toMatchExpr;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchServiceGroupTraceElement;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchServiceTraceElement;
import static org.batfish.representation.fortios.Service.DEFAULT_SOURCE_PORT_RANGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.vendor.VendorStructureId;
import org.junit.jupiter.api.Test;

class FortiosConfigurationTest {

  private final BddTestbed _bddTestbed = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());
  private final BDD _one = _bddTestbed.getPkt().getFactory().one();
  private final IpAccessListToBdd _aclToBdd = _bddTestbed.getAclToBdd();
  private final HeaderSpaceToBDD _hsToBdd = _bddTestbed.getHsToBdd();

  @Test
  void testToMatchExpr_tcpUdpSctp_oneCustom() {
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
  void testToMatchExpr_tcpUdpSctp_allCustom() {
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
  void testToMatchExpr_icmp_defaults() {
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.ICMP);
    HeaderSpace expected = HeaderSpace.builder().setIpProtocols(IpProtocol.ICMP).build();
    assertConverts(service, _hsToBdd.toBDD(expected));
  }

  @Test
  void testToMatchExpr_icmp6_defaults() {
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.ICMP6);
    assertConverts(service, _bddTestbed.getPkt().getFactory().zero());
  }

  @Test
  void testToMatchExpr_icmp_custom() {
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
  void testToMatchExpr_ip_default() {
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.IP);
    assertConverts(service, _one);
  }

  @Test
  void testToMatchExpr_ip_custom() {
    int protocolNumber = 88;
    Service service = new Service("name", new BatfishUUID(1));
    service.setProtocol(Service.Protocol.IP);
    service.setProtocolNumber(protocolNumber);
    HeaderSpace expected =
        HeaderSpace.builder().setIpProtocols(IpProtocol.fromNumber(protocolNumber)).build();
    assertConverts(service, _hsToBdd.toBDD(expected));
  }

  @Test
  void testToMatchExpr_serviceGroup() {
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
  void testToMatchExpr_serviceGroupTraceElement() {
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
  void testToMatchExpr_traceElement() {
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

  private static Address createAddressWithIps(Ip ip1, Ip ip2) {
    Address address = new Address("name", new BatfishUUID(1));
    address.getTypeSpecificFields().setIp1(ip1);
    address.getTypeSpecificFields().setIp2(ip2);
    return address;
  }

  @Test
  void testToIpSpace_ipmaskDefault() {
    Address address = new Address("name", new BatfishUUID(1));
    assertConvertsWithoutWarnings(address, Prefix.ZERO.toIpSpace());
  }

  @Test
  void testToIpSpace_ipmask() {
    Ip ip = Ip.parse("1.1.1.1");
    Ip mask = Ip.parse("255.255.255.0");
    Address address = createAddressWithIps(ip, mask);
    assertConvertsWithoutWarnings(address, Prefix.create(ip, mask).toIpSpace());
  }

  @Test
  void testToIpSpace_ipmaskInvalidMask() {
    // 1.1.1.0 isn't a valid subnet mask
    Address address = createAddressWithIps(Ip.parse("1.1.1.0"), Ip.parse("1.1.1.0"));
    assertThrows(IllegalStateException.class, () -> toIpSpace(address, new Warnings()));
  }

  @Test
  void testToIpSpace_iprangeDefault() {
    // Must set end IP
    Ip endIp = Ip.parse("1.1.1.255");
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.IPRANGE);
    address.getTypeSpecificFields().setIp2(endIp);
    assertConvertsWithoutWarnings(address, IpRange.range(Ip.ZERO, endIp));
  }

  @Test
  void testToIpSpace_iprange() {
    Ip startIp = Ip.parse("1.1.1.0");
    Ip endIp = Ip.parse("1.1.1.255");
    Address address = createAddressWithIps(startIp, endIp);
    address.setType(Address.Type.IPRANGE);
    assertConvertsWithoutWarnings(address, IpRange.range(startIp, endIp));
  }

  @Test
  void testToIpSpace_iprangeNoEndIp() {
    // 1.1.1.0 isn't a valid subnet mask
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.IPRANGE);
    assertThrows(IllegalStateException.class, () -> toIpSpace(address, new Warnings()));
  }

  @Test
  void testToIpSpace_iprangeEndIpTooLow() {
    Address address = createAddressWithIps(Ip.parse("2.2.2.2"), Ip.parse("1.1.1.1"));
    address.setType(Address.Type.IPRANGE);
    assertThrows(IllegalArgumentException.class, () -> toIpSpace(address, new Warnings()));
  }

  @Test
  void testToIpSpace_wildcardDefault() {
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.WILDCARD);
    assertConvertsWithoutWarnings(address, IpWildcard.ANY.toIpSpace());
  }

  @Test
  void testToIpSpace_wildcard() {
    Ip mask = Ip.parse("255.0.255.128"); // FortiOS format (bits that are set matter)
    IpWildcard wildcard = IpWildcard.ipWithWildcardMask(Ip.parse("1.1.1.1"), mask.inverted());
    Address address = createAddressWithIps(wildcard.getIp(), mask);
    address.setType(Address.Type.WILDCARD);
    assertConvertsWithoutWarnings(address, wildcard.toIpSpace());
  }

  @Test
  void testToIpSpace_unsupportedTypes() {
    Stream.of(
            Address.Type.INTERFACE_SUBNET,
            Address.Type.DYNAMIC,
            Address.Type.FQDN,
            Address.Type.GEOGRAPHY,
            Address.Type.MAC)
        .forEach(unsupportedType -> {
          Address address = new Address("name", new BatfishUUID(1));
          address.setType(unsupportedType);
          Warnings w = new Warnings(true, true, true);
          assertThat(toIpSpace(address, w), equalTo(EmptyIpSpace.INSTANCE));
          assertThat(
              w.getRedFlagWarnings(),
              contains(
                  hasText(
                      String.format(
                          "Addresses of type %s are unsupported and will be considered"
                              + " unmatchable.",
                          unsupportedType))));
        });
  }

  @Test
  void testToIpSpace_addrgrp() {
    Addrgrp addrgrp = new Addrgrp("addrgrp", new BatfishUUID(3));
    addrgrp.setExclude(true);
    addrgrp.setExcludeMember(ImmutableSet.of("exclude1"));
    addrgrp.setMember(ImmutableSet.of("include1", "include2"));

    assertConvertsWithoutWarnings(
        addrgrp,
        AclIpSpace.builder()
            .thenRejecting(new IpSpaceReference("exclude1"))
            .thenPermitting(new IpSpaceReference("include1"), new IpSpaceReference("include2"))
            .build());
  }

  /** Check that vendor structure id is set when access list is converted to route filter list */
  @Test
  void testConvertAccessList_vendorStructureId() {
    AccessList acl = new AccessList("name");
    RouteFilterList rfl = convertAccessList(acl, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", FortiosStructureType.ACCESS_LIST.getDescription(), "name")));
  }

  @Test
  void testConvertVdomStaticRoutesWarnings() {
    FortiosConfiguration vc = new FortiosConfiguration();
    vc.setHostname("host");
    vc.setFilename("filename");
    Warnings w = new Warnings(true, true, true);
    vc.setWarnings(w);

    Interface port1 = new Interface("port1");
    port1.setType(Interface.Type.PHYSICAL);
    vc.getInterfaces().put(port1.getName(), port1);

    Interface mesh1 = new Interface("mesh1");
    mesh1.setType(Interface.Type.WL_MESH);
    vc.getInterfaces().put(mesh1.getName(), mesh1);

    StaticRoute missingIface = new StaticRoute("1");
    missingIface.setDevice("missing");
    missingIface.setDst(Prefix.parse("10.0.1.0/24"));

    StaticRoute unconvIface = new StaticRoute("2");
    unconvIface.setDevice("mesh1");
    unconvIface.setDst(Prefix.parse("10.0.2.0/24"));

    StaticRoute disabled = new StaticRoute("3");
    disabled.setDevice("missing");
    disabled.setStatus(StaticRoute.Status.DISABLE);

    StaticRoute valid = new StaticRoute("4");
    valid.setDevice("port1");
    valid.setDst(Prefix.parse("10.0.4.0/24"));
    valid.setGateway(Ip.parse("192.0.2.1"));

    StaticRoute wrongVdom = new StaticRoute("5");
    wrongVdom.setDevice("port1");
    wrongVdom.setDst(Prefix.parse("10.0.5.0/24"));

    vc.getStaticRoutes().computeIfAbsent("root", k -> new HashMap<>()).put("1", missingIface);
    vc.getStaticRoutes().get("root").put("2", unconvIface);
    vc.getStaticRoutes().get("root").put("3", disabled);
    vc.getStaticRoutes().get("root").put("4", valid);
    vc.getStaticRoutes().computeIfAbsent("other", k -> new HashMap<>()).put("5", wrongVdom);

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertThat(
        c.getVrfs().get(computeVrfName("root", Interface.DEFAULT_VRF)).getStaticRoutes(),
        contains(
            org.batfish.datamodel.StaticRoute.builder()
                .setAdmin(StaticRoute.DEFAULT_DISTANCE)
                .setNetwork(Prefix.parse("10.0.4.0/24"))
                .setNextHop(NextHopInterface.of("port1", Ip.parse("192.0.2.1")))
                .build()));
    assertThat(
        w.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText("Interface mesh1 has unsupported type WL_MESH and will not be converted"),
            hasText(
                "Cannot convert static route 1 in VDOM root: outgoing interface missing is"
                    + " undefined"),
            hasText(
                "Cannot convert static route 2 in VDOM root: outgoing interface mesh1 of type"
                    + " WL_MESH was not converted"),
            hasText(
                "Cannot convert static route 5 in VDOM other: outgoing interface port1 belongs to"
                    + " VDOM root")));
  }

  private static void assertConvertsWithoutWarnings(Address address, IpSpace expected) {
    Warnings w = new Warnings(true, true, true);
    assertThat(toIpSpace(address, w), equalTo(expected));
    assertThat(w.getRedFlagWarnings(), empty());
  }

  private static void assertConvertsWithoutWarnings(Addrgrp addrgrp, IpSpace expected) {
    Warnings w = new Warnings(true, true, true);
    assertThat(toIpSpace(addrgrp, w), equalTo(expected));
    assertThat(w.getRedFlagWarnings(), empty());
  }
}
