package org.batfish.representation.fortios;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.representation.fortios.FortiosConfiguration.convertAccessList;
import static org.batfish.representation.fortios.FortiosPolicyConversions.toIpSpace;
import static org.batfish.representation.fortios.FortiosPolicyConversions.toMatchExpr;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchServiceGroupTraceElement;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchServiceTraceElement;
import static org.batfish.representation.fortios.Service.DEFAULT_SOURCE_PORT_RANGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.BddTestbed;
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
import org.batfish.vendor.VendorStructureId;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FortiosConfigurationTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

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
    assertConverts(service, _bddTestbed.getPkt().getFactory().zero());
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

  private static Address createAddressWithIps(Ip ip1, Ip ip2) {
    Address address = new Address("name", new BatfishUUID(1));
    address.getTypeSpecificFields().setIp1(ip1);
    address.getTypeSpecificFields().setIp2(ip2);
    return address;
  }

  @Test
  public void testToIpSpace_ipmaskDefault() {
    Address address = new Address("name", new BatfishUUID(1));
    assertConvertsWithoutWarnings(address, Prefix.ZERO.toIpSpace());
  }

  @Test
  public void testToIpSpace_ipmask() {
    Ip ip = Ip.parse("1.1.1.1");
    Ip mask = Ip.parse("255.255.255.0");
    Address address = createAddressWithIps(ip, mask);
    assertConvertsWithoutWarnings(address, Prefix.create(ip, mask).toIpSpace());
  }

  @Test
  public void testToIpSpace_ipmaskInvalidMask() {
    // 1.1.1.0 isn't a valid subnet mask
    Address address = createAddressWithIps(Ip.parse("1.1.1.0"), Ip.parse("1.1.1.0"));
    _thrown.expect(IllegalStateException.class);
    toIpSpace(address, new Warnings());
  }

  @Test
  public void testToIpSpace_iprangeDefault() {
    // Must set end IP
    Ip endIp = Ip.parse("1.1.1.255");
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.IPRANGE);
    address.getTypeSpecificFields().setIp2(endIp);
    assertConvertsWithoutWarnings(address, IpRange.range(Ip.ZERO, endIp));
  }

  @Test
  public void testToIpSpace_iprange() {
    Ip startIp = Ip.parse("1.1.1.0");
    Ip endIp = Ip.parse("1.1.1.255");
    Address address = createAddressWithIps(startIp, endIp);
    address.setType(Address.Type.IPRANGE);
    assertConvertsWithoutWarnings(address, IpRange.range(startIp, endIp));
  }

  @Test
  public void testToIpSpace_iprangeNoEndIp() {
    // 1.1.1.0 isn't a valid subnet mask
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.IPRANGE);
    _thrown.expect(IllegalStateException.class);
    toIpSpace(address, new Warnings());
  }

  @Test
  public void testToIpSpace_iprangeEndIpTooLow() {
    Address address = createAddressWithIps(Ip.parse("2.2.2.2"), Ip.parse("1.1.1.1"));
    address.setType(Address.Type.IPRANGE);
    _thrown.expect(IllegalArgumentException.class);
    toIpSpace(address, new Warnings());
  }

  @Test
  public void testToIpSpace_wildcardDefault() {
    Address address = new Address("name", new BatfishUUID(1));
    address.setType(Address.Type.WILDCARD);
    assertConvertsWithoutWarnings(address, IpWildcard.ANY.toIpSpace());
  }

  @Test
  public void testToIpSpace_wildcard() {
    Ip mask = Ip.parse("255.0.255.128"); // FortiOS format (bits that are set matter)
    IpWildcard wildcard = IpWildcard.ipWithWildcardMask(Ip.parse("1.1.1.1"), mask.inverted());
    Address address = createAddressWithIps(wildcard.getIp(), mask);
    address.setType(Address.Type.WILDCARD);
    assertConvertsWithoutWarnings(address, wildcard.toIpSpace());
  }

  @Test
  public void testToIpSpace_unsupportedTypes() {
    Stream.of(
            Address.Type.INTERFACE_SUBNET,
            Address.Type.DYNAMIC,
            Address.Type.FQDN,
            Address.Type.GEOGRAPHY,
            Address.Type.MAC)
        .forEach(
            unsupportedType -> {
              Address address = new Address("name", new BatfishUUID(1));
              address.setType(unsupportedType);
              Warnings w = new Warnings(true, true, true);
              assertThat(toIpSpace(address, w), equalTo(EmptyIpSpace.INSTANCE));
              // Changed from redFlag to unimplemented - these are features we haven't
              // implemented yet, not configuration errors
              assertThat(
                  w.getUnimplementedWarnings(),
                  contains(
                      hasText(
                          String.format(
                              "Address type %s is not yet supported; address name will be"
                                  + " treated as unmatchable. Policies using this address will"
                                  + " be excluded from analysis.",
                              unsupportedType))));
            });
  }

  @Test
  public void testToIpSpace_addrgrp() {
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
  public void testConvertAccessList_vendorStructureId() {
    AccessList acl = new AccessList("name");
    RouteFilterList rfl = convertAccessList(acl, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", FortiosStructureType.ACCESS_LIST.getDescription(), "name")));
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
