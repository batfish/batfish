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
import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.acl.AclLineMatchExprs;
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
}
