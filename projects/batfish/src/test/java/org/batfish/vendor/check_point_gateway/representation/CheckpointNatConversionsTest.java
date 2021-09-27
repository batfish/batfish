package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.NAT_PORT_FIRST;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.NAT_PORT_LAST;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.TRANSLATED_SOURCE_TO_TRANSFORMATION_STEPS;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.automaticHideRuleTransformationFunction;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.automaticStaticRuleTransformation;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.checkValidManualHide;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.checkValidManualStatic;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getApplicableNatRules;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getManualNatRules;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getOutgoingTransformations;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.manualHideTransformationSteps;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.manualRuleTransformation;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.manualStaticTransformationSteps;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.mergeTransformations;
import static org.batfish.vendor.check_point_management.TestSharedInstances.NAT_SETTINGS_TEST_INSTANCE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.AddressSpaceToMatchExpr;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.GatewayOrServerPolicy;
import org.batfish.vendor.check_point_management.Host;
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
import org.junit.Test;

/** Test of {@link CheckpointNatConversions}. */
public final class CheckpointNatConversionsTest {
  @Test
  public void testGetApplicableNatRules() {
    NatRule enabledRule =
        new NatRule(
            true,
            "",
            true,
            ImmutableList.of(),
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
            ImmutableList.of(),
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
        new NatRulebase(ImmutableMap.of(), ImmutableList.of(enabledRule, disabledRule), UID);
    assertThat(
        getApplicableNatRules(natRulebase, TEST_GATEWAY).collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(enabledRule)));
  }

  @Test
  public void testGetManualNatRules() {
    GatewayOrServer gateway =
        new SimpleGateway(
            Ip.ZERO, "foo", ImmutableList.of(), new GatewayOrServerPolicy(null, null), UID);
    NatRule autoRule =
        new NatRule(
            true,
            "",
            true,
            ImmutableList.of(),
            NatMethod.STATIC,
            UID,
            UID,
            UID,
            1,
            UID,
            UID,
            UID,
            UID);
    NatRule manualRule =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(),
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
        new NatRulebase(ImmutableMap.of(), ImmutableList.of(autoRule, manualRule), UID);
    assertThat(
        getManualNatRules(natRulebase, gateway).collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(manualRule)));
  }

  @Test
  public void testCheckValidManualHide() {
    Uid uid = Uid.of("1");
    TypedManagementObject addressSpace = new Host(Ip.ZERO, NAT_SETTINGS_TEST_INSTANCE, "foo", uid);
    TypedManagementObject service = new ServiceTcp("foo", "1", uid);
    Warnings warnings = new Warnings();

    assertFalse(checkValidManualHide(service, ORIG, ORIG, warnings));
    assertFalse(checkValidManualHide(addressSpace, addressSpace, ORIG, warnings));
    assertFalse(checkValidManualHide(addressSpace, ORIG, service, warnings));
    assertTrue(checkValidManualHide(addressSpace, ORIG, ORIG, warnings));
  }

  @Test
  public void testManualHideTransformationSteps() {
    Warnings warnings = new Warnings();
    {
      assertThat(
          manualHideTransformationSteps(POLICY_TARGETS, POLICY_TARGETS, POLICY_TARGETS, warnings),
          equalTo(Optional.empty()));
    }
    {
      // src: ipv4 host
      Uid hostUid = Uid.of("1");
      Ip hostIp = Ip.parse("1.1.1.1");
      String hostname = "host";
      Host host = new Host(hostIp, NAT_SETTINGS_TEST_INSTANCE, hostname, hostUid);
      assertThat(
          manualHideTransformationSteps(host, ORIG, ORIG, warnings),
          equalTo(
              Optional.of(
                  ImmutableList.of(
                      assignSourceIp(hostIp), assignSourcePort(NAT_PORT_FIRST, NAT_PORT_LAST)))));
    }
    {
      // src: ipv6 host
      Uid hostUid = Uid.of("1");
      String hostname = "host";
      Host host = new Host(null, NAT_SETTINGS_TEST_INSTANCE, hostname, hostUid);
      assertThat(
          manualHideTransformationSteps(host, ORIG, ORIG, warnings), equalTo(Optional.empty()));
    }
    {
      // src: ipv4 address range
      Uid addressRangeUid = Uid.of("1");
      Ip firstIp = Ip.parse("1.1.1.1");
      Ip lastIp = Ip.parse("1.1.1.10");
      String name = "range1";
      AddressRange addressRange =
          new AddressRange(
              firstIp, lastIp, null, null, NAT_SETTINGS_TEST_INSTANCE, name, addressRangeUid);
      assertThat(
          manualHideTransformationSteps(addressRange, ORIG, ORIG, warnings),
          equalTo(
              Optional.of(
                  ImmutableList.of(
                      assignSourceIp(firstIp, lastIp),
                      assignSourcePort(NAT_PORT_FIRST, NAT_PORT_LAST)))));
    }
    {
      // src: ipv6 address range
      Uid addressRangeUid = Uid.of("1");
      String name = "range1";
      AddressRange addressRange =
          new AddressRange(
              null, null, Ip6.ZERO, Ip6.ZERO, NAT_SETTINGS_TEST_INSTANCE, name, addressRangeUid);
      assertThat(
          manualHideTransformationSteps(addressRange, ORIG, ORIG, warnings),
          equalTo(Optional.empty()));
    }
    {
      // src: original
      assertThat(
          manualHideTransformationSteps(ORIG, ORIG, ORIG, warnings), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testManualHideRuleTransformation() {
    Warnings warnings = new Warnings();
    Uid hostUid = Uid.of("1");
    Ip hostIp = Ip.parse("1.1.1.1");
    String hostname = "host";
    Host host = new Host(hostIp, NAT_SETTINGS_TEST_INSTANCE, hostname, hostUid);
    ServiceToMatchExpr serviceToMatchExpr = new ServiceToMatchExpr(ImmutableMap.of());
    {
      ImmutableMap<Uid, TypedManagementObject> objs =
          ImmutableMap.of(hostUid, host, PT_UID, POLICY_TARGETS, ORIG_UID, ORIG);
      NatRule rule =
          new NatRule(
              false,
              "",
              true,
              ImmutableList.of(),
              NatMethod.HIDE,
              PT_UID,
              PT_UID,
              PT_UID,
              1,
              ORIG_UID,
              ORIG_UID,
              hostUid,
              UID);

      // invalid original fields
      assertThat(
          manualRuleTransformation(
              rule, serviceToMatchExpr, ADDRESS_SPACE_TO_MATCH_EXPR, objs, warnings),
          equalTo(Optional.empty()));
    }
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
      assertThat(
          manualRuleTransformation(
              rule, serviceToMatchExpr, ADDRESS_SPACE_TO_MATCH_EXPR, objs, warnings),
          equalTo(Optional.empty()));
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
              ANY_UID,
              ANY_UID,
              ANY_UID,
              1,
              ORIG_UID,
              ORIG_UID,
              hostUid,
              UID);

      Transformation xform =
          manualRuleTransformation(
                  rule, serviceToMatchExpr, ADDRESS_SPACE_TO_MATCH_EXPR, objs, warnings)
              .get();
      assertThat(
          xform.getTransformationSteps(),
          containsInAnyOrder(
              assignSourceIp(hostIp), assignSourcePort(NAT_PORT_FIRST, NAT_PORT_LAST)));
    }
  }

  @Test
  public void testAutomaticHideRuleTransformation_hideBehindGateway() {
    NatSettings natSettings =
        new NatSettings(true, NatHideBehindGateway.INSTANCE, "All", null, NatMethod.HIDE);
    Ip egressIfaceIp = Ip.parse("5.5.5.5");
    {
      // Hide address range
      AddressRange addressRange =
          new AddressRange(
              Ip.parse("1.1.1.0"), Ip.parse("1.1.1.255"), null, null, natSettings, "a1", UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Function<Ip, Transformation>> func =
          automaticHideRuleTransformationFunction(
              addressRange, ADDRESS_SPACE_TO_MATCH_EXPR, warnings);
      assertTrue(func.isPresent());
      assertThat(
          func.get().apply(egressIfaceIp),
          equalTo(
              Transformation.when(matchSrc(new IpSpaceReference(addressRange.getName())))
                  .apply(assignSourceIp(egressIfaceIp))
                  .build()));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
    {
      // Hide host
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Function<Ip, Transformation>> func =
          automaticHideRuleTransformationFunction(host, ADDRESS_SPACE_TO_MATCH_EXPR, warnings);
      assertTrue(func.isPresent());
      assertThat(
          func.get().apply(egressIfaceIp),
          equalTo(
              Transformation.when(matchSrc(new IpSpaceReference(host.getName())))
                  .apply(assignSourceIp(egressIfaceIp))
                  .build()));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
    {
      // Hide network
      Network network =
          new Network("nw", natSettings, Ip.parse("1.0.0.0"), Ip.parse("255.255.255.0"), UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Function<Ip, Transformation>> func =
          automaticHideRuleTransformationFunction(network, ADDRESS_SPACE_TO_MATCH_EXPR, warnings);
      assertTrue(func.isPresent());
      assertThat(
          func.get().apply(egressIfaceIp),
          equalTo(
              Transformation.when(matchSrc(new IpSpaceReference(network.getName())))
                  .apply(assignSourceIp(egressIfaceIp))
                  .build()));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
  }

  @Test
  public void testAutomaticHideRuleTransformation_hideBehindIp() {
    Ip hideBehindIp = Ip.parse("5.5.5.5");
    NatSettings natSettings =
        new NatSettings(true, new NatHideBehindIp(hideBehindIp), "All", null, NatMethod.HIDE);

    // No need to test every type of HasNatSettings because this is done in hideBehindGateway test
    Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
    Warnings warnings = new Warnings(true, true, true);
    Optional<Function<Ip, Transformation>> func =
        automaticHideRuleTransformationFunction(host, ADDRESS_SPACE_TO_MATCH_EXPR, warnings);
    assertTrue(func.isPresent());
    assertThat(
        func.get().apply(Ip.parse("10.10.10.10")),
        equalTo(
            Transformation.when(matchSrc(new IpSpaceReference(host.getName())))
                .apply(assignSourceIp(hideBehindIp))
                .build()));
    assertThat(warnings.getRedFlagWarnings(), empty());
  }

  @Test
  public void testAutomaticHideRuleTransformationFunction_warnings() {
    {
      // Missing hide-behind
      NatSettings natSettings = new NatSettings(true, null, "All", null, NatMethod.HIDE);
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Function<Ip, Transformation>> func =
          automaticHideRuleTransformationFunction(host, ADDRESS_SPACE_TO_MATCH_EXPR, warnings);
      assertFalse(func.isPresent());
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(hasText(containsString("type is HIDE, but hide-behind is missing"))));
    }
    {
      // hide-behind setting is not recognized
      NatSettings natSettings =
          new NatSettings(true, new UnhandledNatHideBehind("garbage"), "All", null, NatMethod.HIDE);
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Function<Ip, Transformation>> func =
          automaticHideRuleTransformationFunction(host, ADDRESS_SPACE_TO_MATCH_EXPR, warnings);
      assertFalse(func.isPresent());
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(hasText(containsString("NAT hide-behind \"garbage\" is not recognized"))));
    }
    {
      // install-on is not "All" (individual gateways aren't yet supported)
      NatSettings natSettings =
          new NatSettings(true, NatHideBehindGateway.INSTANCE, "gateway1", null, NatMethod.HIDE);
      Host host = new Host(Ip.parse("1.1.1.1"), natSettings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Function<Ip, Transformation>> func =
          automaticHideRuleTransformationFunction(host, ADDRESS_SPACE_TO_MATCH_EXPR, warnings);
      assertFalse(func.isPresent());
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(
              hasText(
                  containsString(
                      "Automatic NAT rules on specific gateways are not yet supported"))));
    }
  }

  @Test
  public void testAutomaticStaticRuleTransformation() {
    Ip natIp = Ip.parse("5.5.5.5");
    Ip hostIp = Ip.parse("1.1.1.1");
    NatSettings natSettings = new NatSettings(true, null, "All", natIp, NatMethod.STATIC);
    Host host = new Host(hostIp, natSettings, "host", UID);
    Warnings warnings = new Warnings(true, true, true);
    Optional<Transformation> srcTransform = automaticStaticRuleTransformation(host, true, warnings);
    Optional<Transformation> dstTransform =
        automaticStaticRuleTransformation(host, false, warnings);
    assertTrue(srcTransform.isPresent() && dstTransform.isPresent());
    assertThat(
        srcTransform.get(),
        equalTo(Transformation.when(matchSrc(hostIp)).apply(assignSourceIp(natIp)).build()));
    assertThat(
        dstTransform.get(),
        equalTo(Transformation.when(matchDst(natIp)).apply(assignDestinationIp(hostIp)).build()));
    assertThat(warnings.getRedFlagWarnings(), empty()); // Neither call generated warnings
  }

  @Test
  public void testAutomaticStaticRuleTransformation_warnings() {
    { // Non-host object
      NatSettings natSettings =
          new NatSettings(true, null, "All", Ip.parse("5.5.5.5"), NatMethod.STATIC);
      Network network =
          new Network("nw", natSettings, Ip.parse("1.0.0.0"), Ip.parse("255.255.255.0"), UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Transformation> t = automaticStaticRuleTransformation(network, true, warnings);
      assertFalse(t.isPresent());
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(
              hasText(
                  containsString(
                      "Automatic static NAT rules on non-host objects are not yet supported"))));
    }
    { // IPv6 host (assuming to be, since the host's IP is null)
      NatSettings natSettings =
          new NatSettings(true, null, "All", Ip.parse("5.5.5.5"), NatMethod.STATIC);
      Host host = new Host(null, natSettings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Transformation> t = automaticStaticRuleTransformation(host, true, warnings);
      assertFalse(t.isPresent());
      assertThat(warnings.getRedFlagWarnings(), empty()); // no warning for missing IPv6 support
    }
    { // IPv6 rule (assuming to be, since IPv4 address is null)
      NatSettings ipv6Settings = new NatSettings(true, null, "All", null, NatMethod.STATIC);
      Host host = new Host(Ip.parse("1.1.1.1"), ipv6Settings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Transformation> t = automaticStaticRuleTransformation(host, true, warnings);
      assertFalse(t.isPresent());
      assertThat(warnings.getRedFlagWarnings(), empty()); // no warning for missing IPv6 support
    }
    { // install-on is not "All" (individual gateways aren't yet supported)
      NatSettings gatewaySettings =
          new NatSettings(true, null, "gateway1", Ip.parse("5.5.5.5"), NatMethod.STATIC);
      Host host = new Host(Ip.parse("1.1.1.1"), gatewaySettings, "host", UID);
      Warnings warnings = new Warnings(true, true, true);
      Optional<Transformation> t = automaticStaticRuleTransformation(host, true, warnings);
      assertFalse(t.isPresent());
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(
              hasText(
                  containsString(
                      "Automatic NAT rules on specific gateways are not yet supported"))));
    }
  }

  @Test
  public void testCheckValidManualStatic() {
    Uid hostUid1 = Uid.of("1");
    Uid hostUid2 = Uid.of("2");
    Uid addrRangeUid = Uid.of("3");
    Uid serviceUid = Uid.of("4");
    String hostName1 = "host1";
    String hostName2 = "host2";
    String addrRangeName = "addrRange";
    String serviceName = "service";

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
    ImmutableMap<Uid, TypedManagementObject> objects =
        ImmutableMap.<Uid, TypedManagementObject>builder()
            .put(hostUid1, host1)
            .put(hostUid2, host2)
            .put(addrRangeUid, addrRange)
            .put(serviceUid, service)
            .put(ANY_UID, ANY)
            .put(ORIG_UID, ORIG)
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
    assertFalse(checkValidManualStatic(natRuleService, objects, warnings));
    assertFalse(checkValidManualStatic(natRuleSrcSpaceToHost, objects, warnings));
    assertFalse(checkValidManualStatic(natRuleSrcToSpace, objects, warnings));
    assertFalse(checkValidManualStatic(natRuleDstSpaceToHost, objects, warnings));
    assertFalse(checkValidManualStatic(natRuleDstToSpace, objects, warnings));
    assertTrue(checkValidManualStatic(natRule, objects, warnings));

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
  public void testManualStaticRuleTransformation() {
    Warnings warnings = new Warnings();
    Uid hostUid1 = Uid.of("1");
    Ip hostIp1 = Ip.parse("1.1.1.1");
    Uid hostUid2 = Uid.of("2");
    Ip hostIp2 = Ip.parse("2.2.2.2");
    String hostname1 = "host1";
    String hostname2 = "host2";
    Host host1 = new Host(hostIp1, NAT_SETTINGS_TEST_INSTANCE, hostname1, hostUid1);
    Host host2 = new Host(hostIp2, NAT_SETTINGS_TEST_INSTANCE, hostname2, hostUid2);
    ServiceToMatchExpr serviceToMatchExpr = new ServiceToMatchExpr(ImmutableMap.of());
    BddTestbed tb =
        new BddTestbed(
            ImmutableMap.of(),
            ImmutableMap.of(
                hostname1,
                host1.getIpv4Address().toIpSpace(),
                hostname2,
                host2.getIpv4Address().toIpSpace()));

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
      assertThat(
          manualRuleTransformation(
              rule, serviceToMatchExpr, ADDRESS_SPACE_TO_MATCH_EXPR, objs, warnings),
          equalTo(Optional.empty()));
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

      Transformation xform =
          manualRuleTransformation(
                  rule, serviceToMatchExpr, ADDRESS_SPACE_TO_MATCH_EXPR, objs, warnings)
              .get();
      assertThat(
          tb.toBDD(xform.getGuard()), equalTo(tb.toBDD(AclLineMatchExprs.matchDst(hostIp1))));
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
        Interface.builder()
            .setName("iface")
            .setAddress(ConcreteInterfaceAddress.create(ifaceIp, 24))
            .build();
    List<Function<Ip, Transformation>> transformationFuncs =
        ImmutableList.of(ip -> Transformation.always().apply(assignSourceIp(ip)).build());
    List<Transformation> outgoingTransformations =
        getOutgoingTransformations(viIface, transformationFuncs);
    assertThat(
        outgoingTransformations,
        contains(Transformation.always().apply(assignSourceIp(ifaceIp)).build()));
  }

  @Test
  public void testMergeTransformations() {
    {
      List<Transformation> transformations = ImmutableList.of();
      assertThat(mergeTransformations(transformations), equalTo(Optional.empty()));
    }
    {
      Transformation t =
          Transformation.always().apply(TransformationStep.assignSourceIp(Ip.ZERO)).build();
      List<Transformation> transformations = ImmutableList.of(t);
      assertThat(mergeTransformations(transformations), equalTo(Optional.of(t)));
    }
    {
      Transformation.Builder tb1 = Transformation.when(FALSE).apply(assignDestinationIp(Ip.ZERO));
      Transformation t2 =
          Transformation.always().apply(TransformationStep.assignSourceIp(Ip.ZERO)).build();
      List<Transformation> transformations = ImmutableList.of(tb1.build(), t2);
      assertThat(
          mergeTransformations(transformations), equalTo(Optional.of(tb1.setOrElse(t2).build())));
    }
  }

  private static final Uid UID = Uid.of("1001");
  private static final Uid PT_UID = Uid.of("1002");
  private static final PolicyTargets POLICY_TARGETS = new PolicyTargets(PT_UID);
  private static final GatewayOrServer TEST_GATEWAY =
      new SimpleGateway(
          Ip.ZERO, "foo", ImmutableList.of(), new GatewayOrServerPolicy(null, null), UID);
  private static final AddressSpaceToMatchExpr ADDRESS_SPACE_TO_MATCH_EXPR =
      new AddressSpaceToMatchExpr(ImmutableMap.of());
  private static final Uid ANY_UID = Uid.of("1003");
  private static final CpmiAnyObject ANY = new CpmiAnyObject(ANY_UID);
  private static final Uid ORIG_UID = Uid.of("1004");
  private static final Original ORIG = new Original(ORIG_UID);
}
