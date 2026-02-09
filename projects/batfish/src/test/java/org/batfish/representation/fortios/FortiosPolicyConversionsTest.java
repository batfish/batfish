package org.batfish.representation.fortios;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class FortiosPolicyConversionsTest {
  private static final BatfishUUID UUID = new BatfishUUID(4);

  @Test
  public void testAddressIpSpaceMetadata() {
    Address address = new Address("foo", UUID);
    assertThat(
        FortiosPolicyConversions.toIpSpaceMetadata(address, "configs/file.txt"),
        equalTo(
            new IpSpaceMetadata(
                "foo", "address", new VendorStructureId("configs/file.txt", "address", "foo"))));
    Address addressComment = new Address("bar", UUID);
    addressComment.setComment("My special servers");
    assertThat(
        FortiosPolicyConversions.toIpSpaceMetadata(addressComment, "configs/file.txt"),
        equalTo(
            new IpSpaceMetadata(
                "bar (My special servers)",
                "address",
                new VendorStructureId("configs/file.txt", "address", "bar"))));
  }

  @Test
  public void testAddressGroupIpSpaceMetadata() {
    Addrgrp group = new Addrgrp("foo", UUID);
    assertThat(
        FortiosPolicyConversions.toIpSpaceMetadata(group, "configs/file.txt"),
        equalTo(
            new IpSpaceMetadata(
                "foo", "addrgrp", new VendorStructureId("configs/file.txt", "addrgrp", "foo"))));
    Addrgrp groupComment = new Addrgrp("bar", UUID);
    groupComment.setComment("My special servers");
    assertThat(
        FortiosPolicyConversions.toIpSpaceMetadata(groupComment, "configs/file.txt"),
        equalTo(
            new IpSpaceMetadata(
                "bar (My special servers)",
                "addrgrp",
                new VendorStructureId("configs/file.txt", "addrgrp", "bar"))));
  }

  @Test
  public void testConvertPolicyWithPartialSupport() {
    // Create a policy with addresses that won't be in namedIpSpaces
    Policy policy = new Policy("1144");
    policy.setName("Test Policy");
    policy.setAction(Policy.Action.ACCEPT);
    policy.setStatus(Policy.Status.ENABLE);
    policy.setSrcAddr(ImmutableSet.of("unimplemented-address"));
    policy.setDstAddr(ImmutableSet.of("valid-address"));
    policy.setService(ImmutableSet.of("HTTP"));
    policy.setSrcIntfZones(ImmutableSet.of());
    policy.setDstIntfZones(ImmutableSet.of());

    Warnings warnings = new Warnings(true, true, true);

    // Convert with only "valid-address" in namedIpSpaces
    Optional<AclLine> result =
        FortiosPolicyConversions.convertPolicy(
            policy,
            ImmutableMap.of("HTTP", AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP)),
            ImmutableMap.of(),
            ImmutableSet.of("valid-address"), // Only dst address is valid
            "test-config.txt",
            warnings);

    // Policy should be excluded due to partial support (unimplemented src address)
    assertFalse("Policy with partial support should not convert", result.isPresent());

    // Should have unimplemented warning, not redFlag
    assertFalse(
        "Should have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
    assertThat(
        warnings.getUnimplementedWarnings(),
        hasItem(hasText(containsString("excluded from reachability analysis"))));
    assertThat(
        warnings.getRedFlagWarnings(),
        not(hasItem(hasText(containsString("will not match any packets")))));
  }

  @Test
  public void testConvertPolicyWithFullSupport() {
    // Create a policy with all addresses in namedIpSpaces
    Policy policy = new Policy("1");
    policy.setName("Test Policy");
    policy.setAction(Policy.Action.ACCEPT);
    policy.setStatus(Policy.Status.ENABLE);
    policy.setSrcAddr(ImmutableSet.of("valid-src"));
    policy.setDstAddr(ImmutableSet.of("valid-dst"));
    policy.setService(ImmutableSet.of("HTTP"));
    policy.setSrcIntfZones(ImmutableSet.of());
    policy.setDstIntfZones(ImmutableSet.of());

    Warnings warnings = new Warnings(true, true, true);

    // Convert with all addresses in namedIpSpaces
    Optional<AclLine> result =
        FortiosPolicyConversions.convertPolicy(
            policy,
            ImmutableMap.of("HTTP", AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP)),
            ImmutableMap.of(),
            ImmutableSet.of("valid-src", "valid-dst"),
            "test-config.txt",
            warnings);

    // Policy should convert successfully
    assertTrue("Policy with full support should convert", result.isPresent());

    // Should not have warnings
    assertTrue(
        "Should not have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
    assertTrue("Should not have redFlag warnings", warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertPolicyDisabled() {
    // Create a disabled policy - should not convert
    Policy policy = new Policy("2");
    policy.setAction(Policy.Action.ACCEPT);
    policy.setStatus(Policy.Status.DISABLE);
    policy.setSrcAddr(ImmutableSet.of("src"));
    policy.setDstAddr(ImmutableSet.of("dst"));
    policy.setService(ImmutableSet.of("HTTP"));
    policy.setSrcIntfZones(ImmutableSet.of());
    policy.setDstIntfZones(ImmutableSet.of());

    Warnings warnings = new Warnings(true, true, true);

    Optional<AclLine> result =
        FortiosPolicyConversions.convertPolicy(
            policy,
            ImmutableMap.of("HTTP", AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP)),
            ImmutableMap.of(),
            ImmutableSet.of("src", "dst"),
            "test-config.txt",
            warnings);

    // Disabled policies should not convert
    assertFalse("Disabled policy should not convert", result.isPresent());
  }

  @Test
  public void testConvertPolicyDenyAction() {
    // Test DENY action conversion
    Policy policy = new Policy("3");
    policy.setAction(Policy.Action.DENY);
    policy.setStatus(Policy.Status.ENABLE);
    policy.setSrcAddr(ImmutableSet.of("src"));
    policy.setDstAddr(ImmutableSet.of("dst"));
    policy.setService(ImmutableSet.of("HTTP"));
    policy.setSrcIntfZones(ImmutableSet.of());
    policy.setDstIntfZones(ImmutableSet.of());

    Warnings warnings = new Warnings(true, true, true);

    Optional<AclLine> result =
        FortiosPolicyConversions.convertPolicy(
            policy,
            ImmutableMap.of("HTTP", AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP)),
            ImmutableMap.of(),
            ImmutableSet.of("src", "dst"),
            "test-config.txt",
            warnings);

    // DENY policy should convert
    assertTrue("DENY policy should convert", result.isPresent());
  }

  @Test
  public void testGetPolicyName() {
    // Test policy name generation
    assertThat(FortiosPolicyConversions.getPolicyName("123", null), equalTo("123"));
    assertThat(
        FortiosPolicyConversions.getPolicyName("456", "My Policy"), equalTo("456 named My Policy"));
  }

  @Test
  public void testComputeOutgoingFilterName() {
    // Test outgoing filter name generation
    assertThat(
        FortiosPolicyConversions.computeOutgoingFilterName("OUTGOING", "external"),
        equalTo("OUTGOING~external~OUTGOING_FILTER"));
  }

  // Tests for toIpSpace(Address, Warnings)

  @Test
  public void testToIpSpaceAddressIpMask() {
    // Test IPMASK type address conversion
    Address address = new Address("test_subnet", UUID);
    address.setType(Address.Type.IPMASK);
    address.getTypeSpecificFields().setIp1(Ip.parse("192.168.1.0"));
    address.getTypeSpecificFields().setIp2(Ip.parse("255.255.255.0"));

    Warnings warnings = new Warnings(true, true, true);
    IpSpace ipSpace = FortiosPolicyConversions.toIpSpace(address, warnings);

    // Should convert to the expected Prefix IpSpace
    assertThat(ipSpace, equalTo(Prefix.parse("192.168.1.0/24").toIpSpace()));
    assertTrue("Should not have warnings", warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testToIpSpaceAddressIpRange() {
    // Test IPRANGE type address conversion
    Address address = new Address("test_range", UUID);
    address.setType(Address.Type.IPRANGE);
    address.getTypeSpecificFields().setIp1(Ip.parse("10.0.0.1"));
    address.getTypeSpecificFields().setIp2(Ip.parse("10.0.0.10"));

    Warnings warnings = new Warnings(true, true, true);
    IpSpace ipSpace = FortiosPolicyConversions.toIpSpace(address, warnings);

    // Should convert to the expected IpRange IpSpace
    assertThat(
        ipSpace,
        equalTo(org.batfish.datamodel.IpRange.range(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.10"))));
    assertTrue("Should not have warnings", warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testToIpSpaceAddressWildcard() {
    // Test WILDCARD type address conversion
    Address address = new Address("test_wildcard", UUID);
    address.setType(Address.Type.WILDCARD);
    address.getTypeSpecificFields().setIp1(Ip.parse("192.168.1.100"));
    address.getTypeSpecificFields().setIp2(Ip.parse("0.0.0.255")); // wildcard mask

    Warnings warnings = new Warnings(true, true, true);
    IpSpace ipSpace = FortiosPolicyConversions.toIpSpace(address, warnings);

    // Should convert to IpWildcard's IpSpace
    IpWildcard expected =
        IpWildcard.ipWithWildcardMask(Ip.parse("192.168.1.100"), Ip.parse("0.0.0.255").inverted());
    assertThat(ipSpace, equalTo(expected.toIpSpace()));
    assertTrue("Should not have warnings", warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testToIpSpaceAddressDynamic() {
    // Test unsupported DYNAMIC type
    Address address = new Address("dynamic_addr", UUID);
    address.setType(Address.Type.DYNAMIC);

    Warnings warnings = new Warnings(true, true, true);
    IpSpace ipSpace = FortiosPolicyConversions.toIpSpace(address, warnings);

    assertThat(ipSpace, equalTo(EmptyIpSpace.INSTANCE));
    assertFalse(
        "Should have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
    assertThat(warnings.getUnimplementedWarnings(), hasItem(hasText(containsString("DYNAMIC"))));
  }

  @Test
  public void testToIpSpaceAddressFqdn() {
    // Test unsupported FQDN type
    Address address = new Address("fqdn_addr", UUID);
    address.setType(Address.Type.FQDN);

    Warnings warnings = new Warnings(true, true, true);
    IpSpace ipSpace = FortiosPolicyConversions.toIpSpace(address, warnings);

    assertThat(ipSpace, equalTo(EmptyIpSpace.INSTANCE));
    assertFalse(
        "Should have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
    assertThat(warnings.getUnimplementedWarnings(), hasItem(hasText(containsString("FQDN"))));
  }

  @Test
  public void testToIpSpaceAddressInterfaceSubnet() {
    // Test unsupported INTERFACE_SUBNET type
    Address address = new Address("interface_subnet", UUID);
    address.setType(Address.Type.INTERFACE_SUBNET);

    Warnings warnings = new Warnings(true, true, true);
    IpSpace ipSpace = FortiosPolicyConversions.toIpSpace(address, warnings);

    assertThat(ipSpace, equalTo(EmptyIpSpace.INSTANCE));
    assertFalse(
        "Should have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
    assertThat(
        warnings.getUnimplementedWarnings(), hasItem(hasText(containsString("INTERFACE_SUBNET"))));
  }

  @Test
  public void testToIpSpaceAddressGeography() {
    // Test unsupported GEOGRAPHY type
    Address address = new Address("geo_addr", UUID);
    address.setType(Address.Type.GEOGRAPHY);

    Warnings warnings = new Warnings(true, true, true);
    IpSpace ipSpace = FortiosPolicyConversions.toIpSpace(address, warnings);

    assertThat(ipSpace, equalTo(EmptyIpSpace.INSTANCE));
    assertFalse(
        "Should have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
    assertThat(warnings.getUnimplementedWarnings(), hasItem(hasText(containsString("GEOGRAPHY"))));
  }

  @Test
  public void testToIpSpaceAddressMac() {
    // Test unsupported MAC type
    Address address = new Address("mac_addr", UUID);
    address.setType(Address.Type.MAC);

    Warnings warnings = new Warnings(true, true, true);
    IpSpace ipSpace = FortiosPolicyConversions.toIpSpace(address, warnings);

    assertThat(ipSpace, equalTo(EmptyIpSpace.INSTANCE));
    assertFalse(
        "Should have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
    assertThat(warnings.getUnimplementedWarnings(), hasItem(hasText(containsString("MAC"))));
  }

  @Test
  public void testToIpSpaceAddressAssociatedInterfaceWarning() {
    // Test that associated-interface generates a warning
    Address address = new Address("test_with_assoc", UUID);
    address.setType(Address.Type.IPMASK);
    address.getTypeSpecificFields().setIp1(Ip.parse("10.0.0.0"));
    address.getTypeSpecificFields().setIp2(Ip.parse("255.255.255.0"));
    address.setAssociatedInterface("port1");

    Warnings warnings = new Warnings(true, true, true);
    FortiosPolicyConversions.toIpSpace(address, warnings);

    // Should still convert successfully but with a warning
    assertFalse("Should have redFlag warnings", warnings.getRedFlagWarnings().isEmpty());
    assertThat(
        warnings.getRedFlagWarnings(), hasItem(hasText(containsString("associated-interface"))));
  }

  // Tests for toIpSpace(Addrgrp, Warnings)

  @Test
  public void testToIpSpaceAddrgrpSimple() {
    // Test simple address group with members
    Addrgrp addrgrp = new Addrgrp("group1", UUID);
    addrgrp.setMember(ImmutableSet.of("addr1", "addr2"));
    addrgrp.setExcludeMember(ImmutableSet.of()); // Must be set (code asserts non-null)

    Warnings warnings = new Warnings(true, true, true);
    FortiosPolicyConversions.toIpSpace(addrgrp, warnings);

    // Should create an AclIpSpace with permit entries
    // The members are references, so we should see IpSpaceReference entries
    assertTrue("Should not have warnings", warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testToIpSpaceAddrgrpWithExclude() {
    // Test address group with exclude members
    Addrgrp addrgrp = new Addrgrp("group_with_exclude", UUID);
    addrgrp.setMember(ImmutableSet.of("addr1", "addr2"));
    addrgrp.setExcludeMember(ImmutableSet.of("excluded_addr"));

    Warnings warnings = new Warnings(true, true, true);
    FortiosPolicyConversions.toIpSpace(addrgrp, warnings);

    // Should have both permit and reject entries
    assertTrue("Should not have warnings", warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testToIpSpaceAddrgrpExcludeOnly() {
    // Test address group with only exclude members
    Addrgrp addrgrp = new Addrgrp("exclude_only_group", UUID);
    addrgrp.setMember(ImmutableSet.of());
    addrgrp.setExcludeMember(ImmutableSet.of("excluded_addr"));

    Warnings warnings = new Warnings(true, true, true);
    FortiosPolicyConversions.toIpSpace(addrgrp, warnings);

    assertTrue("Should not have warnings", warnings.getRedFlagWarnings().isEmpty());
  }

  // Tests for toMatchExpr with Service

  @Test
  public void testToMatchExprServiceTcp() {
    // Test TCP service conversion
    Service service = new Service("tcp_service", UUID);
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    service.setTcpPortRangeDst(org.batfish.datamodel.IntegerSpace.of(Range.closed(80, 80)));

    AclLineMatchExpr expr =
        FortiosPolicyConversions.toMatchExpr(service, ImmutableMap.of(), "test-config.txt");

    // Should be an OR expression with TCP protocol match
    assertFalse("Should not be FalseExpr", expr instanceof FalseExpr);
    assertTrue("Should match TCP protocol", expr.toString().contains("TCP"));
  }

  @Test
  public void testToMatchExprServiceUdp() {
    // Test UDP service conversion
    Service service = new Service("udp_service", UUID);
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    service.setUdpPortRangeDst(org.batfish.datamodel.IntegerSpace.of(Range.closed(53, 53)));

    AclLineMatchExpr expr =
        FortiosPolicyConversions.toMatchExpr(service, ImmutableMap.of(), "test-config.txt");

    // Should be an OR expression with UDP protocol match
    assertFalse("Should not be FalseExpr", expr instanceof FalseExpr);
    assertTrue("Should match UDP protocol", expr.toString().contains("UDP"));
  }

  @Test
  public void testToMatchExprServiceSctp() {
    // Test SCTP service conversion
    Service service = new Service("sctp_service", UUID);
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    service.setSctpPortRangeDst(org.batfish.datamodel.IntegerSpace.of(Range.closed(1000, 1000)));

    AclLineMatchExpr expr =
        FortiosPolicyConversions.toMatchExpr(service, ImmutableMap.of(), "test-config.txt");

    // Should be an OR expression with SCTP protocol match
    assertFalse("Should not be FalseExpr", expr instanceof FalseExpr);
  }

  @Test
  public void testToMatchExprServiceIcmp() {
    // Test ICMP service conversion
    Service service = new Service("icmp_service", UUID);
    service.setProtocol(Service.Protocol.ICMP);
    service.setIcmpType(8);
    service.setIcmpCode(0);

    AclLineMatchExpr expr =
        FortiosPolicyConversions.toMatchExpr(service, ImmutableMap.of(), "test-config.txt");

    // Should match ICMP with type and code
    assertFalse("Should not be FalseExpr", expr instanceof FalseExpr);
  }

  @Test
  public void testToMatchExprServiceIpAllProtocols() {
    // Test IP service with protocol number 0 (all protocols)
    Service service = new Service("ip_service", UUID);
    service.setProtocol(Service.Protocol.IP);
    service.setProtocolNumber(0);

    AclLineMatchExpr expr =
        FortiosPolicyConversions.toMatchExpr(service, ImmutableMap.of(), "test-config.txt");

    // Should wrap TRUE in an OrMatchExpr with trace element
    assertFalse("Should not be FalseExpr", expr instanceof FalseExpr);
    assertTrue("Should contain TRUE", expr.toString().contains("TrueExpr"));
  }

  @Test
  public void testToMatchExprServiceIpSpecificProtocol() {
    // Test IP service with specific protocol number
    Service service = new Service("ip_protocol_47", UUID);
    service.setProtocol(Service.Protocol.IP);
    service.setProtocolNumber(47); // GRE

    AclLineMatchExpr expr =
        FortiosPolicyConversions.toMatchExpr(service, ImmutableMap.of(), "test-config.txt");

    // Should match specific protocol number
    assertFalse("Should not be FalseExpr", expr instanceof FalseExpr);
    assertTrue("Should match protocol 47", expr.toString().contains("47"));
  }

  @Test
  public void testToMatchExprServiceWithSrcPorts() {
    // Test service with source ports
    Service service = new Service("tcp_with_src", UUID);
    service.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    service.setTcpPortRangeDst(org.batfish.datamodel.IntegerSpace.of(Range.closed(80, 80)));
    service.setTcpPortRangeSrc(org.batfish.datamodel.IntegerSpace.of(Range.closed(1024, 65535)));

    AclLineMatchExpr expr =
        FortiosPolicyConversions.toMatchExpr(service, ImmutableMap.of(), "test-config.txt");

    // Should include source port matching
    assertFalse("Should not be FalseExpr", expr instanceof FalseExpr);
  }

  // Tests for toMatchExpr with ServiceGroup

  @Test
  public void testToMatchExprServiceGroup() {
    // Test service group conversion
    ServiceGroup group = new ServiceGroup("service_group", UUID);
    group.setMember(ImmutableSet.of("HTTP", "HTTPS"));

    // Create dummy services
    Service httpService = new Service("HTTP", new BatfishUUID(1));
    httpService.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    httpService.setTcpPortRangeDst(org.batfish.datamodel.IntegerSpace.of(Range.closed(80, 80)));

    Service httpsService = new Service("HTTPS", new BatfishUUID(2));
    httpsService.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    httpsService.setTcpPortRangeDst(org.batfish.datamodel.IntegerSpace.of(Range.closed(443, 443)));

    ImmutableMap<String, ServiceGroupMember> serviceGroupMembers =
        ImmutableMap.of(
            "HTTP", httpService,
            "HTTPS", httpsService,
            "service_group", group);

    AclLineMatchExpr expr =
        FortiosPolicyConversions.toMatchExpr(group, serviceGroupMembers, "test-config.txt");

    // Should be an OR of the member services
    assertFalse("Should not be FalseExpr", expr instanceof FalseExpr);
  }

  // Additional edge case tests for convertPolicy

  @Test
  public void testConvertPolicyIpsecAction() {
    // Test that IPSEC action policies are not supported
    Policy policy = new Policy("5");
    policy.setName("IPSEC Policy");
    policy.setAction(Policy.Action.IPSEC);
    policy.setStatus(Policy.Status.ENABLE);
    policy.setSrcAddr(ImmutableSet.of("src"));
    policy.setDstAddr(ImmutableSet.of("dst"));
    policy.setService(ImmutableSet.of("HTTP"));
    policy.setSrcIntfZones(ImmutableSet.of());
    policy.setDstIntfZones(ImmutableSet.of());

    Warnings warnings = new Warnings(true, true, true);

    Optional<AclLine> result =
        FortiosPolicyConversions.convertPolicy(
            policy,
            ImmutableMap.of("HTTP", AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP)),
            ImmutableMap.of(),
            ImmutableSet.of("src", "dst"),
            "test-config.txt",
            warnings);

    // IPSEC action should not convert
    assertFalse("IPSEC policy should not convert", result.isPresent());
    assertFalse("Should have redFlag warnings", warnings.getRedFlagWarnings().isEmpty());
    assertThat(warnings.getRedFlagWarnings(), hasItem(hasText(containsString("IPSEC"))));
  }

  @Test
  public void testConvertPolicyEmptyServices() {
    // Test policy with empty services set
    Policy policy = new Policy("6");
    policy.setAction(Policy.Action.ACCEPT);
    policy.setStatus(Policy.Status.ENABLE);
    policy.setSrcAddr(ImmutableSet.of("src"));
    policy.setDstAddr(ImmutableSet.of("dst"));
    policy.setService(ImmutableSet.of()); // Empty services
    policy.setSrcIntfZones(ImmutableSet.of());
    policy.setDstIntfZones(ImmutableSet.of());

    Warnings warnings = new Warnings(true, true, true);

    Optional<AclLine> result =
        FortiosPolicyConversions.convertPolicy(
            policy,
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableSet.of("src", "dst"),
            "test-config.txt",
            warnings);

    // Empty services should result in a line that won't match
    assertTrue("Policy with empty services should still convert", result.isPresent());
    assertFalse("Should have redFlag warnings", warnings.getRedFlagWarnings().isEmpty());
    assertThat(warnings.getRedFlagWarnings(), hasItem(hasText(containsString("services"))));
  }

  @Test
  public void testConvertPolicyMissingSrcAddrInNamedIpSpaces() {
    // Test policy where src address is not in namedIpSpaces
    Policy policy = new Policy("7");
    policy.setAction(Policy.Action.ACCEPT);
    policy.setStatus(Policy.Status.ENABLE);
    policy.setSrcAddr(ImmutableSet.of("src1", "src2"));
    policy.setDstAddr(ImmutableSet.of("dst"));
    policy.setService(ImmutableSet.of("HTTP"));
    policy.setSrcIntfZones(ImmutableSet.of());
    policy.setDstIntfZones(ImmutableSet.of());

    Warnings warnings = new Warnings(true, true, true);

    Optional<AclLine> result =
        FortiosPolicyConversions.convertPolicy(
            policy,
            ImmutableMap.of("HTTP", AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP)),
            ImmutableMap.of(),
            ImmutableSet.of("dst"), // src addresses missing
            "test-config.txt",
            warnings);

    // Should be excluded due to partial support
    assertFalse("Policy with missing src addresses should not convert", result.isPresent());
    assertFalse(
        "Should have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
  }

  @Test
  public void testConvertPolicyMissingDstAddrInNamedIpSpaces() {
    // Test policy where dst address is not in namedIpSpaces
    Policy policy = new Policy("8");
    policy.setAction(Policy.Action.ACCEPT);
    policy.setStatus(Policy.Status.ENABLE);
    policy.setSrcAddr(ImmutableSet.of("src"));
    policy.setDstAddr(ImmutableSet.of("dst1", "dst2"));
    policy.setService(ImmutableSet.of("HTTP"));
    policy.setSrcIntfZones(ImmutableSet.of());
    policy.setDstIntfZones(ImmutableSet.of());

    Warnings warnings = new Warnings(true, true, true);

    Optional<AclLine> result =
        FortiosPolicyConversions.convertPolicy(
            policy,
            ImmutableMap.of("HTTP", AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP)),
            ImmutableMap.of(),
            ImmutableSet.of("src"), // dst addresses missing
            "test-config.txt",
            warnings);

    // Should be excluded due to partial support
    assertFalse("Policy with missing dst addresses should not convert", result.isPresent());
    assertFalse(
        "Should have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
  }

  @Test
  public void testConvertPolicyWithMultipleServices() {
    // Test policy with multiple services
    Policy policy = new Policy("9");
    policy.setName("Multi-service policy");
    policy.setAction(Policy.Action.ACCEPT);
    policy.setStatus(Policy.Status.ENABLE);
    policy.setSrcAddr(ImmutableSet.of("src"));
    policy.setDstAddr(ImmutableSet.of("dst"));
    policy.setService(ImmutableSet.of("HTTP", "HTTPS", "DNS"));
    policy.setSrcIntfZones(ImmutableSet.of());
    policy.setDstIntfZones(ImmutableSet.of());

    Warnings warnings = new Warnings(true, true, true);

    Optional<AclLine> result =
        FortiosPolicyConversions.convertPolicy(
            policy,
            ImmutableMap.of(
                "HTTP", AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP),
                "HTTPS", AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP),
                "DNS", AclLineMatchExprs.matchIpProtocol(IpProtocol.UDP)),
            ImmutableMap.of(),
            ImmutableSet.of("src", "dst"),
            "test-config.txt",
            warnings);

    // Should convert successfully with all services
    assertTrue("Policy with multiple services should convert", result.isPresent());
    assertTrue("Should not have warnings", warnings.getRedFlagWarnings().isEmpty());
    assertTrue(
        "Should not have unimplemented warnings", warnings.getUnimplementedWarnings().isEmpty());
  }
}
