package org.batfish.vendor.check_point_gateway.representation;

import static com.google.common.collect.Maps.immutableEntry;
import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.HIDE_BEHIND_GATEWAY_IP;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.NAT_PORT_FIRST;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.NAT_PORT_LAST;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.TRANSLATED_SOURCE_TO_TRANSFORMATION_STEPS;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.automaticHideRuleTransformationStep;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.automaticStaticRuleTransformationStep;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.checkValidManualHide;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.checkValidManualStatic;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getApplicableNatRules;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getOutgoingTransformations;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.isValidAutomaticHideRule;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.isValidAutomaticStaticRule;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.manualHideTransformationSteps;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.manualRuleTransformation;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.manualStaticTransformationSteps;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.matchAutomaticStaticRule;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.matchInternalTraffic;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.matchManualRule;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.mergeTransformations;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.shouldConvertAutomaticRule;
import static org.batfish.vendor.check_point_management.TestSharedInstances.NAT_SETTINGS_TEST_INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.AddressSpaceToMatchExpr;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.Domain;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.GatewayOrServerPolicy;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.ManagementDomain;
import org.batfish.vendor.check_point_management.NamedManagementObject;
import org.batfish.vendor.check_point_management.NatHideBehindGateway;
import org.batfish.vendor.check_point_management.NatHideBehindIp;
import org.batfish.vendor.check_point_management.NatMethod;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.NatSettings;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.PolicyTargets;
import org.batfish.vendor.check_point_management.ServiceTcp;
import org.batfish.vendor.check_point_management.ServiceToMatchExpr;
import org.batfish.vendor.check_point_management.SimpleGateway;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;
import org.batfish.vendor.check_point_management.UnhandledNatHideBehind;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link CheckpointNatConversions}. */
public final class CheckpointNatConversionsTest {
  @Test
  public void testGetApplicableNatRules() {
    NatRule ruleForAllGateways =
        new NatRule(
            true,
            "",
            true,
            ImmutableList.of(PT_UID), // apply rule to all policy targets (all gateways)
            NatMethod.STATIC,
            UID,
            UID,
            UID,
            1,
            UID,
            UID,
            UID,
            UID);
    NatRule ruleForThisGateway =
        new NatRule(
            true,
            "",
            true,
            ImmutableList.of(TEST_GATEWAY.getUid()), // apply rule to this gateway
            NatMethod.STATIC,
            UID,
            UID,
            UID,
            1,
            UID,
            UID,
            UID,
            UID);
    NatRule inapplicableRule =
        new NatRule(
            true,
            "",
            true,
            ImmutableList.of(), // do not apply rule to any gateways
            NatMethod.STATIC,
            UID,
            UID,
            UID,
            1,
            UID,
            UID,
            UID,
            UID);
    NatRule disabledRule =
        new NatRule(
            true,
            "",
            false,
            ImmutableList.of(UID),
            NatMethod.STATIC,
            UID,
            UID,
            UID,
            1,
            UID,
            UID,
            UID,
            UID);
    NatRulebase natRulebase =
        new NatRulebase(
            ImmutableMap.of(PT_UID, POLICY_TARGETS),
            ImmutableList.of(
                ruleForAllGateways, ruleForThisGateway, inapplicableRule, disabledRule),
            UID);
    assertThat(
        getApplicableNatRules(natRulebase, TEST_DOMAIN_AND_GATEWAY)
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(ruleForAllGateways, ruleForThisGateway));
  }

  @Test
  public void testCheckValidManualHide() {
    Uid uid = Uid.of("1");
    TypedManagementObject addressSpace = new Host(Ip.ZERO, NAT_SETTINGS_TEST_INSTANCE, "foo", uid);
    TypedManagementObject service = new ServiceTcp("foo", "1", uid);
    {
      // Invalid source type
      Warnings warnings = new Warnings(true, true, true);
      assertFalse(checkValidManualHide(service, ORIG, ORIG, warnings));
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(hasText(containsString("translated-source foo has invalid type ServiceTcp"))));
    }
    {
      // Invalid destination type
      Warnings warnings = new Warnings(true, true, true);
      assertFalse(checkValidManualHide(addressSpace, service, ORIG, warnings));
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(
              hasText(containsString("translated-destination foo has invalid type ServiceTcp"))));
    }
    {
      // Invalid service type
      Warnings warnings = new Warnings(true, true, true);
      assertFalse(checkValidManualHide(addressSpace, ORIG, service, warnings));
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(hasText(containsString("translated-service foo has invalid type ServiceTcp"))));
    }
    {
      // Valid
      Warnings warnings = new Warnings(true, true, true);
      assertTrue(checkValidManualHide(addressSpace, ORIG, ORIG, warnings));
      assertThat(warnings.getRedFlagWarnings(), emptyIterable());
    }
  }

  @Test
  public void testManualHideTransformationSteps() {
    Warnings warnings = new Warnings();
    {
      // Invalid translated-types
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.HIDE,
              ANY_UID,
              ANY_UID,
              ANY_UID,
              1,
              PT_UID,
              PT_UID,
              PT_UID,
              Uid.of("9999"));
      assertThat(
          manualHideTransformationSteps(natRule, OBJECTS, warnings), equalTo(Optional.empty()));
    }
    {
      // Ipv4 host translated-source and translated-destination
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.HIDE,
              HOST2_IPV4_UID,
              ANY_UID,
              ANY_UID,
              1,
              HOST2_IPV4_UID,
              ORIG_UID,
              HOST_IPV4_UID,
              Uid.of("9999"));
      assertThat(
          manualHideTransformationSteps(natRule, OBJECTS, warnings),
          equalTo(
              Optional.of(
                  ImmutableList.of(
                      assignSourceIp(HOST_IPV4.getIpv4Address()),
                      assignDestinationIp(HOST2_IPV4.getIpv4Address()),
                      assignSourcePort(NAT_PORT_FIRST, NAT_PORT_LAST)))));
    }
    {
      // Ipv6 host translated-source
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.HIDE,
              ANY_UID,
              ANY_UID,
              ANY_UID,
              1,
              ORIG_UID,
              ORIG_UID,
              HOST_IPV6_UID,
              Uid.of("9999"));
      assertThat(
          manualHideTransformationSteps(natRule, OBJECTS, warnings), equalTo(Optional.empty()));
    }
    {
      // Address range (ipv4) translated-source
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.HIDE,
              ANY_UID,
              ANY_UID,
              ANY_UID,
              1,
              ORIG_UID,
              ORIG_UID,
              ADDR_RANGE_IPV4_UID,
              Uid.of("9999"));
      assertThat(
          manualHideTransformationSteps(natRule, OBJECTS, warnings),
          equalTo(
              Optional.of(
                  ImmutableList.of(
                      assignSourceIp(
                          ADDR_RANGE_IPV4.getIpv4AddressFirst(),
                          ADDR_RANGE_IPV4.getIpv4AddressLast()),
                      assignSourcePort(NAT_PORT_FIRST, NAT_PORT_LAST)))));
    }
    {
      // Address range (ipv6) translated-source
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.HIDE,
              ANY_UID,
              ANY_UID,
              ANY_UID,
              1,
              ORIG_UID,
              ORIG_UID,
              ADDR_RANGE_IPV6_UID,
              Uid.of("9999"));
      assertThat(
          manualHideTransformationSteps(natRule, OBJECTS, warnings), equalTo(Optional.empty()));
    }
    {
      // Original translated-source
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.HIDE,
              ANY_UID,
              ANY_UID,
              ANY_UID,
              1,
              ORIG_UID,
              ORIG_UID,
              ORIG_UID,
              Uid.of("9999"));
      assertThat(
          manualHideTransformationSteps(natRule, OBJECTS, warnings), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testManualRuleTransformation_hide() {
    Warnings warnings = new Warnings();
    Uid hostUid = Uid.of("1");
    Ip hostIp = Ip.parse("1.1.1.1");
    String hostname = "host";
    Host host = new Host(hostIp, NAT_SETTINGS_TEST_INSTANCE, hostname, hostUid);
    {
      ImmutableMap<Uid, TypedManagementObject> objs =
          ImmutableMap.of(ANY_UID, ANY, PT_UID, POLICY_TARGETS);
      NatRule rule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.HIDE,
              ANY_UID,
              ANY_UID,
              ANY_UID,
              1,
              PT_UID,
              PT_UID,
              PT_UID,
              UID);

      // invalid translated fields
      assertThat(manualRuleTransformation(rule, objs, warnings), equalTo(Optional.empty()));
    }
    {
      ImmutableMap<Uid, TypedManagementObject> objs =
          ImmutableMap.of(ANY_UID, ANY, ORIG_UID, ORIG, hostUid, host);
      NatRule rule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.HIDE,
              hostUid,
              ANY_UID,
              ANY_UID,
              1,
              ORIG_UID,
              ORIG_UID,
              hostUid,
              UID);

      Transformation xform = manualRuleTransformation(rule, objs, warnings).get();
      // no match condition enforced in transformation (despite dest constraint)
      assertThat(xform.getGuard(), equalTo(AclLineMatchExprs.TRUE));
      assertThat(
          xform.getTransformationSteps(),
          containsInAnyOrder(
              assignSourceIp(hostIp), assignSourcePort(NAT_PORT_FIRST, NAT_PORT_LAST)));
    }
  }

  @Test
  public void testMatchInternalTraffic() {
    NatSettings natSettings =
        new NatSettings(true, NatHideBehindGateway.INSTANCE, "All", null, NatMethod.HIDE);
    {
      // Test match internal traffic for an address-range
      AddressRange addressRange =
          new AddressRange(
              Ip.parse("1.1.1.0"), Ip.parse("1.1.1.255"), null, null, natSettings, "a1", UID);
      assertThat(
          matchInternalTraffic(addressRange, ADDRESS_SPACE_TO_MATCH_EXPR).get(),
          equalTo(
              and(
                  ADDRESS_SPACE_TO_MATCH_EXPR.convertSource(addressRange),
                  ADDRESS_SPACE_TO_MATCH_EXPR.convertDest(addressRange))));
    }
    {
      // Test match internal traffic for a network
      Network network =
          new Network("nw", natSettings, Ip.parse("1.1.1.0"), Ip.parse("255.255.255.0"), UID);
      assertThat(
          matchInternalTraffic(network, ADDRESS_SPACE_TO_MATCH_EXPR).get(),
          equalTo(
              and(
                  ADDRESS_SPACE_TO_MATCH_EXPR.convertSource(network),
                  ADDRESS_SPACE_TO_MATCH_EXPR.convertDest(network))));
    }
    {
      // No such thing as internal traffic for a host
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      assertThat(
          matchInternalTraffic(host, ADDRESS_SPACE_TO_MATCH_EXPR), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testAutomaticHideRuleTransformationStep() {
    {
      // Hide behind gateway
      NatSettings natSettings =
          new NatSettings(true, NatHideBehindGateway.INSTANCE, "All", null, NatMethod.HIDE);
      AddressRange addressRange =
          new AddressRange(
              Ip.parse("1.1.1.0"), Ip.parse("1.1.1.255"), null, null, natSettings, "a1", UID);
      TransformationStep step = automaticHideRuleTransformationStep(addressRange);
      assertThat(step, equalTo(assignSourceIp(HIDE_BEHIND_GATEWAY_IP)));
    }
    {
      // Hide behind IP
      Ip hideBehindIp = Ip.parse("5.5.5.5");
      NatSettings natSettings =
          new NatSettings(true, new NatHideBehindIp(hideBehindIp), "All", null, NatMethod.HIDE);
      // use a different HasNatSettings type just for fun
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      TransformationStep step = automaticHideRuleTransformationStep(host);
      assertThat(step, equalTo(assignSourceIp(hideBehindIp)));
    }
    {
      // Unhandled hide-behind type (NAT settings like this should be filtered out before reaching
      // this function)
      NatSettings natSettings =
          new NatSettings(true, new UnhandledNatHideBehind("foo"), "All", null, NatMethod.HIDE);
      // use a different HasNatSettings type just for fun
      Network network =
          new Network("nw", natSettings, Ip.parse("1.0.0.0"), Ip.parse("255.255.255.0"), UID);
      _thrown.expect(IllegalArgumentException.class);
      automaticHideRuleTransformationStep(network);
    }
  }

  @Test
  public void testMatchAutomaticStaticRule() {
    Ip natIp = Ip.parse("5.5.5.5");
    Ip hostIp = Ip.parse("1.1.1.1");
    NatSettings natSettings = new NatSettings(true, null, "All", natIp, NatMethod.STATIC);
    Host host = new Host(hostIp, natSettings, "host", UID);
    {
      // Source NAT
      AclLineMatchExpr match = matchAutomaticStaticRule(host, true);
      assertThat(match, equalTo(matchSrc(hostIp.toIpSpace())));
    }
    {
      // Destination NAT
      AclLineMatchExpr match = matchAutomaticStaticRule(host, false);
      assertThat(match, equalTo(matchDst(natIp.toIpSpace())));
    }
  }

  @Test
  public void testAutomaticStaticRuleTransformationStep() {
    Ip natIp = Ip.parse("5.5.5.5");
    Ip hostIp = Ip.parse("1.1.1.1");
    NatSettings natSettings = new NatSettings(true, null, "All", natIp, NatMethod.STATIC);
    Host host = new Host(hostIp, natSettings, "host", UID);
    {
      // Source transformation
      TransformationStep step = automaticStaticRuleTransformationStep(host, true);
      assertThat(step, equalTo(assignSourceIp(natIp)));
    }
    {
      // Destination NAT
      TransformationStep step = automaticStaticRuleTransformationStep(host, false);
      assertThat(step, equalTo(assignDestinationIp(hostIp)));
    }
  }

  @Test
  public void testShouldConvertAutomaticRule() {
    Uid hostUid = Uid.of("12345");
    Host host = new Host(Ip.parse("1.1.1.1"), NAT_SETTINGS_TEST_INSTANCE, "host", hostUid);
    Map<Uid, TypedManagementObject> objs =
        ImmutableMap.of(hostUid, host, ANY_UID, ANY, PT_UID, POLICY_TARGETS);
    {
      // Rule types that are expected but ignored
      ImmutableList.of(
              new NatRule(
                  true,
                  "",
                  true,
                  ImmutableList.of(PT_UID),
                  NatMethod.HIDE,
                  // Original src and dst both constrained (match internal traffic for auto hide)
                  hostUid,
                  ANY_UID,
                  hostUid,
                  1,
                  ORIG_UID,
                  ORIG_UID,
                  ORIG_UID,
                  UID),
              new NatRule(
                  true,
                  "",
                  true,
                  ImmutableList.of(PT_UID),
                  NatMethod.HIDE,
                  // Src not constrained, dst constrained (dst translation for an auto static rule)
                  hostUid,
                  ANY_UID,
                  ANY_UID,
                  1,
                  ORIG_UID,
                  ORIG_UID,
                  ORIG_UID,
                  UID))
          .forEach(
              rule -> {
                Warnings warnings = new Warnings(true, true, true);
                assertFalse(shouldConvertAutomaticRule(rule, objs, warnings));
                assertThat(warnings.getRedFlagWarnings(), empty());
              });
    }
    {
      // Rule that should be converted
      NatRule rule =
          new NatRule(
              true,
              "",
              true,
              ImmutableList.of(PT_UID),
              NatMethod.HIDE,
              ANY_UID,
              ANY_UID,
              hostUid, // Source constrained, other original fields not constrained
              1,
              ORIG_UID,
              ORIG_UID,
              ORIG_UID,
              UID);
      Warnings warnings = new Warnings(true, true, true);
      assertTrue(shouldConvertAutomaticRule(rule, objs, warnings));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
  }

  @Test
  public void testIsValidAutomaticHideRule() {
    {
      // Hide rule missing hide-behind
      NatSettings natSettings = new NatSettings(true, null, "All", null, NatMethod.HIDE);
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      assertFalse(isValidAutomaticHideRule(host, warnings));
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(hasText(containsString("type is HIDE, but hide-behind is missing"))));
    }
    {
      // Hide rule with unrecognized hide-behind setting
      NatSettings natSettings =
          new NatSettings(true, new UnhandledNatHideBehind("garbage"), "All", null, NatMethod.HIDE);
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      assertFalse(isValidAutomaticHideRule(host, warnings));
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(hasText(containsString("NAT hide-behind \"garbage\" is not recognized"))));
    }
    {
      // Valid hide rule
      NatSettings natSettings =
          new NatSettings(true, NatHideBehindGateway.INSTANCE, "All", null, NatMethod.HIDE);
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      assertTrue(isValidAutomaticHideRule(host, new Warnings(true, true, true)));
    }
  }

  @Test
  public void testIsValidAutomaticStaticRule() {
    {
      // Static rule on non-host object
      NatSettings natSettings =
          new NatSettings(true, null, "All", Ip.parse("5.5.5.5"), NatMethod.STATIC);
      Network network =
          new Network("nw", natSettings, Ip.parse("1.0.0.0"), Ip.parse("255.255.255.0"), UID);
      Warnings warnings = new Warnings(true, true, true);
      assertFalse(isValidAutomaticStaticRule(network, warnings));
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(
              hasText(
                  containsString(
                      "Automatic static NAT rules on non-host objects are not yet supported"))));
    }
    {
      // Static rule on IPv6 host (assuming to be, since the host's IP is null)
      NatSettings natSettings =
          new NatSettings(true, null, "All", Ip.parse("5.5.5.5"), NatMethod.STATIC);
      Host host = new Host(null, natSettings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      assertFalse(isValidAutomaticStaticRule(host, warnings));
      assertThat(warnings.getRedFlagWarnings(), empty()); // no warning for missing IPv6 support
    }
    {
      // Static IPv6 rule (assuming to be, since IPv4 address is null)
      NatSettings ipv6Settings = new NatSettings(true, null, "All", null, NatMethod.STATIC);
      Host host = new Host(Ip.parse("1.1.1.1"), ipv6Settings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      assertFalse(isValidAutomaticStaticRule(host, warnings));
      assertThat(warnings.getRedFlagWarnings(), empty()); // no warning for missing IPv6 support
    }
    {
      // Valid static rule
      NatSettings natSettings =
          new NatSettings(true, null, "All", Ip.parse("5.5.5.5"), NatMethod.STATIC);
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      assertTrue(isValidAutomaticStaticRule(host, new Warnings(true, true, true)));
    }
  }

  @Test
  public void testCheckValidManualStatic() {
    Uid hostUid1 = Uid.of("1");
    Uid hostUid2 = Uid.of("2");
    Uid addrRangeUid = Uid.of("3");
    Uid serviceUid = Uid.of("4");
    Uid networkUid1 = Uid.of("5");
    Uid networkUid2 = Uid.of("6");
    String hostName1 = "host1";
    String hostName2 = "host2";
    String addrRangeName = "addrRange";
    String serviceName = "service";
    String networkName1 = "network1";
    String networkName2 = "network2";

    TypedManagementObject addrRange =
        new AddressRange(
            Ip.ZERO,
            Ip.parse("1.1.1.1"),
            null,
            null,
            NAT_SETTINGS_TEST_INSTANCE,
            addrRangeName,
            addrRangeUid);
    TypedManagementObject service = new ServiceTcp(serviceName, "1", serviceUid);
    TypedManagementObject host1 =
        new Host(Ip.parse("1.1.1.1"), NAT_SETTINGS_TEST_INSTANCE, hostName1, hostUid1);
    TypedManagementObject host2 =
        new Host(Ip.parse("2.2.2.2"), NAT_SETTINGS_TEST_INSTANCE, hostName2, hostUid2);
    TypedManagementObject network1 =
        new Network(
            networkName1,
            NAT_SETTINGS_TEST_INSTANCE,
            Ip.parse("1.1.1.1"),
            Ip.parse("255.255.255.0"),
            networkUid1);
    TypedManagementObject network2 =
        new Network(
            networkName2,
            NAT_SETTINGS_TEST_INSTANCE,
            Ip.parse("2.2.2.2"),
            Ip.parse("255.255.255.0"),
            networkUid2);
    ImmutableMap<Uid, TypedManagementObject> objects =
        ImmutableMap.<Uid, TypedManagementObject>builder()
            .put(hostUid1, host1)
            .put(hostUid2, host2)
            .put(addrRangeUid, addrRange)
            .put(serviceUid, service)
            .put(ANY_UID, ANY)
            .put(ORIG_UID, ORIG)
            .put(networkUid1, network1)
            .put(networkUid2, network2)
            .build();
    Warnings warnings = new Warnings(true, true, true);

    NatRule natRuleService =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(),
            NatMethod.STATIC,
            ANY_UID,
            ANY_UID,
            ANY_UID,
            1,
            ORIG_UID,
            serviceUid,
            ORIG_UID,
            UID);
    NatRule natRuleDstSpaceToHost =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(),
            NatMethod.STATIC,
            addrRangeUid,
            ANY_UID,
            ANY_UID,
            1,
            hostUid1,
            ORIG_UID,
            ORIG_UID,
            UID);
    NatRule natRuleDstToSpace =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(),
            NatMethod.STATIC,
            ANY_UID,
            ANY_UID,
            ANY_UID,
            1,
            addrRangeUid,
            ORIG_UID,
            ORIG_UID,
            UID);

    NatRule natRuleSrcSpaceToHost =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(),
            NatMethod.STATIC,
            ANY_UID,
            ANY_UID,
            addrRangeUid,
            1,
            ORIG_UID,
            ORIG_UID,
            hostUid1,
            UID);
    NatRule natRuleSrcToSpace =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(),
            NatMethod.STATIC,
            ANY_UID,
            ANY_UID,
            ANY_UID,
            1,
            ORIG_UID,
            ORIG_UID,
            addrRangeUid,
            UID);

    NatRule natRule =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(),
            NatMethod.STATIC,
            ANY_UID,
            ANY_UID,
            hostUid1,
            1,
            ORIG_UID,
            ORIG_UID,
            hostUid2,
            UID);

    NatRule natRuleNetworks =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(),
            NatMethod.STATIC,
            networkUid2,
            ANY_UID,
            networkUid1,
            1,
            networkUid1,
            ORIG_UID,
            networkUid2,
            UID);
    assertFalse(checkValidManualStatic(natRuleService, objects, warnings));
    assertFalse(checkValidManualStatic(natRuleSrcSpaceToHost, objects, warnings));
    assertFalse(checkValidManualStatic(natRuleSrcToSpace, objects, warnings));
    assertFalse(checkValidManualStatic(natRuleDstSpaceToHost, objects, warnings));
    assertFalse(checkValidManualStatic(natRuleDstToSpace, objects, warnings));
    assertTrue(checkValidManualStatic(natRule, objects, warnings));
    assertTrue(checkValidManualStatic(natRuleNetworks, objects, warnings));

    assertThat(
        warnings,
        hasRedFlags(
            containsInAnyOrder(
                hasText(
                    "Manual Static NAT rule cannot translate services (like service of type"
                        + " ServiceTcp) and will be ignored"),
                hasText(
                    "Manual Static NAT rule translated-source host1 of type Host is incompatible"
                        + " with original-source addrRange of type AddressRange and will be"
                        + " ignored"),
                hasText(
                    "Manual Static NAT rule translated-source addrRange has unsupported type"
                        + " AddressRange and will be ignored"),
                hasText(
                    "Manual Static NAT rule translated-destination host1 of type Host is"
                        + " incompatible with original-destination addrRange of type AddressRange"
                        + " and will be ignored"),
                hasText(
                    "Manual Static NAT rule translated-destination addrRange has unsupported type"
                        + " AddressRange and will be ignored"))));
  }

  @Test
  public void testManualStaticTransformationSteps() {
    Warnings warnings = new Warnings();
    Uid hostUid1 = Uid.of("1");
    Ip hostIp1 = Ip.parse("1.1.1.1");
    String hostname1 = "host";
    Host host1 = new Host(hostIp1, NAT_SETTINGS_TEST_INSTANCE, hostname1, hostUid1);

    Uid hostUid2 = Uid.of("2");
    Ip hostIp2 = Ip.parse("2.2.2.2");
    String hostname2 = "host2";
    Host host2 = new Host(hostIp2, NAT_SETTINGS_TEST_INSTANCE, hostname2, hostUid2);

    Uid networkUid1 = Uid.of("1");
    String networkName1 = "net1";
    Network network1 =
        new Network(
            networkName1,
            NAT_SETTINGS_TEST_INSTANCE,
            Ip.parse("10.0.1.0"),
            Ip.parse("255.255.255.0"),
            networkUid1);

    Uid networkUid2 = Uid.of("2");
    String networkName2 = "net2";
    Network network2 =
        new Network(
            networkName2,
            NAT_SETTINGS_TEST_INSTANCE,
            Ip.parse("10.0.2.0"),
            Ip.parse("255.255.255.0"),
            networkUid2);
    Prefix networkPrefix2 = Prefix.create(network2.getSubnet4(), network2.getSubnetMask());
    {
      // src: host -> host
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.STATIC,
              ANY_UID,
              ANY_UID,
              hostUid1,
              1,
              ORIG_UID,
              ORIG_UID,
              hostUid2,
              UID);
      ImmutableMap<Uid, NamedManagementObject> objects =
          ImmutableMap.<Uid, NamedManagementObject>builder()
              .put(hostUid1, host1)
              .put(hostUid2, host2)
              .put(ANY_UID, ANY)
              .put(ORIG_UID, ORIG)
              .build();

      assertThat(
          manualStaticTransformationSteps(natRule, objects, warnings),
          equalTo(Optional.of(ImmutableList.of(assignSourceIp(hostIp2)))));
    }
    {
      // src: network -> network
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.STATIC,
              ANY_UID,
              ANY_UID,
              networkUid1,
              1,
              ORIG_UID,
              ORIG_UID,
              networkUid2,
              UID);
      ImmutableMap<Uid, NamedManagementObject> objects =
          ImmutableMap.<Uid, NamedManagementObject>builder()
              .put(networkUid1, network1)
              .put(networkUid2, network2)
              .put(ANY_UID, ANY)
              .put(ORIG_UID, ORIG)
              .build();

      assertThat(
          manualStaticTransformationSteps(natRule, objects, warnings),
          equalTo(
              Optional.of(
                  ImmutableList.of(
                      assignSourceIp(
                          networkPrefix2.getFirstHostIp(), networkPrefix2.getLastHostIp())))));
    }
    {
      // dst: host -> host
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.STATIC,
              hostUid1,
              ANY_UID,
              ANY_UID,
              1,
              hostUid2,
              ORIG_UID,
              ORIG_UID,
              UID);
      ImmutableMap<Uid, NamedManagementObject> objects =
          ImmutableMap.<Uid, NamedManagementObject>builder()
              .put(hostUid1, host1)
              .put(hostUid2, host2)
              .put(ANY_UID, ANY)
              .put(ORIG_UID, ORIG)
              .build();

      assertThat(
          manualStaticTransformationSteps(natRule, objects, warnings),
          equalTo(Optional.of(ImmutableList.of(assignDestinationIp(hostIp2)))));
    }
    {
      // dst: network -> network
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.STATIC,
              networkUid1,
              ANY_UID,
              ANY_UID,
              1,
              networkUid2,
              ORIG_UID,
              ORIG_UID,
              UID);
      ImmutableMap<Uid, NamedManagementObject> objects =
          ImmutableMap.<Uid, NamedManagementObject>builder()
              .put(networkUid1, network1)
              .put(networkUid2, network2)
              .put(ANY_UID, ANY)
              .put(ORIG_UID, ORIG)
              .build();

      assertThat(
          manualStaticTransformationSteps(natRule, objects, warnings),
          equalTo(
              Optional.of(
                  ImmutableList.of(
                      assignDestinationIp(
                          networkPrefix2.getFirstHostIp(), networkPrefix2.getLastHostIp())))));
    }
    {
      // src and dst translation
      NatRule natRule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.STATIC,
              hostUid1,
              ANY_UID,
              hostUid2,
              1,
              hostUid2,
              ORIG_UID,
              hostUid1,
              UID);
      ImmutableMap<Uid, NamedManagementObject> objects =
          ImmutableMap.<Uid, NamedManagementObject>builder()
              .put(hostUid1, host1)
              .put(hostUid2, host2)
              .put(ANY_UID, ANY)
              .put(ORIG_UID, ORIG)
              .build();

      assertThat(
          manualStaticTransformationSteps(natRule, objects, warnings),
          equalTo(
              Optional.of(
                  ImmutableList.of(assignSourceIp(hostIp1), assignDestinationIp(hostIp2)))));
    }
  }

  @Test
  public void testMatchManualRule() {
    Warnings warnings = new Warnings();
    ServiceToMatchExpr serviceToMatchExpr = new ServiceToMatchExpr(ImmutableMap.of());
    {
      // Returns empty optional when toMatchExpr does (more thorough testing testNatOrigToMatchExpr)
      ImmutableMap<Uid, TypedManagementObject> objs =
          ImmutableMap.of(ANY_UID, ANY, ORIG_UID, ORIG, PT_UID, POLICY_TARGETS);
      NatRule rule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.STATIC,
              PT_UID, // invalid
              ANY_UID,
              ANY_UID,
              1,
              ORIG_UID,
              ORIG_UID,
              ORIG_UID,
              UID);
      assertThat(
          matchManualRule(rule, serviceToMatchExpr, ADDRESS_SPACE_TO_MATCH_EXPR, objs, warnings),
          equalTo(Optional.empty()));
    }
    {
      // Returns correct expr when toMatchExpr is successful
      Uid hostUid = Uid.of("1");
      Ip hostIp = Ip.parse("1.1.1.1");
      Host host = new Host(hostIp, NAT_SETTINGS_TEST_INSTANCE, "name", hostUid);
      BddTestbed tb =
          new BddTestbed(ImmutableMap.of(), ImmutableMap.of(host.getName(), hostIp.toIpSpace()));
      ImmutableMap<Uid, TypedManagementObject> objs =
          ImmutableMap.of(ANY_UID, ANY, hostUid, host, ORIG_UID, ORIG);
      NatRule rule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.STATIC,
              hostUid,
              ANY_UID,
              ANY_UID,
              1,
              ORIG_UID,
              ORIG_UID,
              ORIG_UID,
              UID);
      AclLineMatchExpr match =
          matchManualRule(rule, serviceToMatchExpr, ADDRESS_SPACE_TO_MATCH_EXPR, objs, warnings)
              .get();
      assertThat(tb.toBDD(match), equalTo(tb.toBDD(matchDst(hostIp))));
    }
  }

  @Test
  public void testManualRuleTransformation_static() {
    Warnings warnings = new Warnings();
    Uid hostUid1 = Uid.of("1");
    Ip hostIp1 = Ip.parse("1.1.1.1");
    Uid hostUid2 = Uid.of("2");
    Ip hostIp2 = Ip.parse("2.2.2.2");
    String hostname1 = "host1";
    String hostname2 = "host2";
    Host host1 = new Host(hostIp1, NAT_SETTINGS_TEST_INSTANCE, hostname1, hostUid1);
    Host host2 = new Host(hostIp2, NAT_SETTINGS_TEST_INSTANCE, hostname2, hostUid2);
    {
      ImmutableMap<Uid, TypedManagementObject> objs =
          ImmutableMap.of(ANY_UID, ANY, PT_UID, POLICY_TARGETS);
      NatRule rule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.STATIC,
              ANY_UID,
              ANY_UID,
              ANY_UID,
              1,
              PT_UID,
              PT_UID,
              PT_UID,
              UID);

      // invalid fields
      assertThat(manualRuleTransformation(rule, objs, warnings), equalTo(Optional.empty()));
    }
    {
      ImmutableMap<Uid, TypedManagementObject> objs =
          ImmutableMap.of(ANY_UID, ANY, ORIG_UID, ORIG, hostUid1, host1, hostUid2, host2);
      NatRule rule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.STATIC,
              hostUid1,
              ANY_UID,
              ANY_UID,
              1,
              hostUid2,
              ORIG_UID,
              ORIG_UID,
              UID);

      Transformation xform = manualRuleTransformation(rule, objs, warnings).get();
      // no match condition enforced in transformation (despite dest constraint)
      assertThat(xform.getGuard(), equalTo(AclLineMatchExprs.TRUE));
      assertThat(xform.getTransformationSteps(), contains(assignDestinationIp(hostIp2)));
    }
  }

  @Test
  public void visitHost_noIp() {
    Host host = new Host(null, NAT_SETTINGS_TEST_INSTANCE, "host", Uid.of("1"));
    assertThat(
        TRANSLATED_SOURCE_TO_TRANSFORMATION_STEPS.visitHost(host), equalTo(ImmutableList.of()));
  }

  @Test
  public void testGetOutgoingTransformations() {
    Ip ifaceIp = Ip.parse("10.10.10.1");
    Interface viIface =
        TestInterface.builder()
            .setName("iface")
            .setAddress(ConcreteInterfaceAddress.create(ifaceIp, 24))
            .build();
    List<Function<Ip, Transformation>> transformationFuncs =
        ImmutableList.of(ip -> always().apply(assignSourceIp(ip)).build());
    List<Transformation> outgoingTransformations =
        getOutgoingTransformations(viIface, transformationFuncs);
    assertThat(outgoingTransformations, contains(always().apply(assignSourceIp(ifaceIp)).build()));
  }

  @Test
  public void testMergeTransformations() {
    {
      List<Transformation> transformations = ImmutableList.of();
      assertThat(mergeTransformations(transformations), equalTo(Optional.empty()));
    }
    {
      Transformation t = always().apply(TransformationStep.assignSourceIp(Ip.ZERO)).build();
      List<Transformation> transformations = ImmutableList.of(t);
      assertThat(mergeTransformations(transformations), equalTo(Optional.of(t)));
    }
    {
      Transformation.Builder tb1 = Transformation.when(FALSE).apply(assignDestinationIp(Ip.ZERO));
      Transformation t2 = always().apply(TransformationStep.assignSourceIp(Ip.ZERO)).build();
      List<Transformation> transformations = ImmutableList.of(tb1.build(), t2);
      assertThat(
          mergeTransformations(transformations), equalTo(Optional.of(tb1.setOrElse(t2).build())));
    }
  }

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final Uid UID = Uid.of("1001");
  private static final Uid PT_UID = Uid.of("1002");
  private static final PolicyTargets POLICY_TARGETS = new PolicyTargets(PT_UID);
  private static final GatewayOrServer TEST_GATEWAY =
      new SimpleGateway(
          Ip.ZERO, "foo", ImmutableList.of(), new GatewayOrServerPolicy(null, null), UID);
  private static final ManagementDomain TEST_MANAGEMENT_DOMAIN =
      new ManagementDomain(
          new Domain("domain", Uid.of("1000")),
          ImmutableMap.of(TEST_GATEWAY.getUid(), TEST_GATEWAY),
          ImmutableMap.of(),
          ImmutableList.of());
  private static final Map.Entry<ManagementDomain, GatewayOrServer> TEST_DOMAIN_AND_GATEWAY =
      immutableEntry(TEST_MANAGEMENT_DOMAIN, TEST_GATEWAY);
  private static final AddressSpaceToMatchExpr ADDRESS_SPACE_TO_MATCH_EXPR =
      new AddressSpaceToMatchExpr(ImmutableMap.of());
  private static final Uid ANY_UID = Uid.of("1003");
  private static final CpmiAnyObject ANY = new CpmiAnyObject(ANY_UID);
  private static final Uid ORIG_UID = Uid.of("1004");
  private static final Original ORIG = new Original(ORIG_UID);
  private static final Uid HOST_IPV4_UID = Uid.of("1005");
  private static final Host HOST_IPV4 =
      new Host(Ip.parse("1.1.1.1"), NAT_SETTINGS_TEST_INSTANCE, "hostv4", HOST_IPV4_UID);
  private static final Uid HOST2_IPV4_UID = Uid.of("1006");
  private static final Host HOST2_IPV4 =
      new Host(Ip.parse("2.2.2.2"), NAT_SETTINGS_TEST_INSTANCE, "host2v4", HOST2_IPV4_UID);
  private static final Uid HOST_IPV6_UID = Uid.of("1007");
  private static final Host HOST_IPV6 =
      new Host(null, NAT_SETTINGS_TEST_INSTANCE, "hostv6", HOST_IPV6_UID);
  private static final Uid ADDR_RANGE_IPV4_UID = Uid.of("1008");
  private static final AddressRange ADDR_RANGE_IPV4 =
      new AddressRange(
          Ip.parse("1.1.1.1"),
          Ip.parse("1.1.1.10"),
          null,
          null,
          NAT_SETTINGS_TEST_INSTANCE,
          "rangev4",
          ADDR_RANGE_IPV4_UID);
  private static final Uid ADDR_RANGE_IPV6_UID = Uid.of("1009");
  private static final AddressRange ADDR_RANGE_IPV6 =
      new AddressRange(
          null,
          null,
          Ip6.ZERO,
          Ip6.ZERO,
          NAT_SETTINGS_TEST_INSTANCE,
          "rangev6",
          ADDR_RANGE_IPV6_UID);

  private static final Map<Uid, NamedManagementObject> OBJECTS =
      ImmutableMap.<Uid, NamedManagementObject>builder()
          .put(PT_UID, POLICY_TARGETS)
          .put(ANY_UID, ANY)
          .put(ORIG_UID, ORIG)
          .put(HOST_IPV4_UID, HOST_IPV4)
          .put(HOST2_IPV4_UID, HOST2_IPV4)
          .put(HOST_IPV6_UID, HOST_IPV6)
          .put(ADDR_RANGE_IPV4_UID, ADDR_RANGE_IPV4)
          .put(ADDR_RANGE_IPV6_UID, ADDR_RANGE_IPV6)
          .build();
}
