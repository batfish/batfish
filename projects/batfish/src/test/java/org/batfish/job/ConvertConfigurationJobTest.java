package org.batfish.job;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.matchers.StaticRouteMatchers.hasTrack;
import static org.batfish.datamodel.topology.InterfaceTopology.l13NoEncapsulation;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.computeLegacyInterfaceTopology;
import static org.batfish.datamodel.tracking.TrackMethods.alwaysTrue;
import static org.batfish.job.ConvertConfigurationJob.finalizeConfiguration;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import java.util.SortedMap;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.job.ConvertConfigurationJob.CollectIpSpaceReferences;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link ConvertConfigurationJob}. */
public final class ConvertConfigurationJobTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testCollectIpSpaceReferences() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
    c.getIpSpaces().put("ref", new IpSpaceReference("InIpSpaces"));
    c.getIpSpaces().put("SelfRefInIpSpaces", new IpSpaceReference("SelfRefInIpSpaces"));
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(
                new ExprAclLine(
                    LineAction.PERMIT,
                    new NotMatchExpr(
                        new AndMatchExpr(
                            ImmutableList.of(
                                new OrMatchExpr(
                                    ImmutableList.of(
                                        new MatchHeaderSpace(
                                            HeaderSpace.builder()
                                                .setSrcIps(new IpSpaceReference("SrcIps"))
                                                .setNotSrcIps(new IpSpaceReference("NotSrcIps"))
                                                .setDstIps(new IpSpaceReference("DstIps"))
                                                .setNotDstIps(new IpSpaceReference("NotDstIps"))
                                                .build())))))),
                    "line"))
            .build();
    c.getIpAccessLists().put(acl.getName(), acl);
    IpAccessList selfRefAcl =
        IpAccessList.builder()
            .setName("selfRefAcl")
            .setLines(new AclAclLine("selfRefAcl", "selfRefAcl"))
            .build();
    c.getIpAccessLists().put(selfRefAcl.getName(), selfRefAcl);
    Transformation outT =
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(new IpSpaceReference("InOutgoingTransformation"))
                        .build()))
            .build();
    Transformation andThenT =
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(new IpSpaceReference("AndThenTransformation"))
                        .build()))
            .build();
    Transformation orElseT =
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(new IpSpaceReference("OrElseTransformation"))
                        .build()))
            .build();
    Transformation inT =
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(new IpSpaceReference("InIncomingTransformation"))
                        .build()))
            .setAndThen(andThenT)
            .setOrElse(orElseT)
            .build();
    Interface i =
        Interface.builder()
            .setType(InterfaceType.PHYSICAL)
            .setName("i")
            .setIncomingTransformation(inT)
            .setOutgoingTransformation(outT)
            .build();
    c.getAllInterfaces().put(i.getName(), i);
    assertThat(
        CollectIpSpaceReferences.collect(c),
        containsInAnyOrder(
            "InIpSpaces",
            "SelfRefInIpSpaces",
            "SrcIps",
            "NotSrcIps",
            "DstIps",
            "NotDstIps",
            "InIncomingTransformation",
            "AndThenTransformation",
            "OrElseTransformation",
            "InOutgoingTransformation"));
  }

  @Test
  public void testFinalizeConfigurationVerifyCommunityStructures() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);
    c.setCommunityMatchExprs(
        ImmutableMap.of("referenceToUndefined", new CommunityMatchExprReference("undefined")));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference"));
    finalizeConfiguration(c, new Warnings());
  }

  @Test
  public void testFinalizeConfigurationVerifyVrrpGroups() {
    {
      // Test that undefined interface is removed from VrrpGroup, and warning is added.
      Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
      c.setDefaultCrossZoneAction(LineAction.PERMIT);
      c.setDefaultInboundAction(LineAction.PERMIT);

      Interface.builder()
          .setOwner(c)
          .setName("exists")
          .setTopology(l13NoEncapsulation("exists"))
          .setVrrpGroups(
              ImmutableSortedMap.of(
                  1,
                  VrrpGroup.builder()
                      .addVirtualAddress("exists", Ip.parse("1.1.1.1"))
                      .addVirtualAddress("missing", Ip.parse("2.2.2.2"))
                      .build()))
          .build();
      Warnings w = new Warnings(false, true, false);
      finalizeConfiguration(c, w);

      assertThat(
          c.getAllInterfaces().get("exists").getVrrpGroups().get(1).getVirtualAddresses(),
          hasKeys("exists"));
      assertThat(
          w.getRedFlagWarnings(),
          contains(
              hasText(
                  "Removing virtual addresses to be assigned to non-existent interface 'missing'"
                      + " for VRID 1 on sync interface 'exists'")));
    }
    {
      // Test that there are no warnings when all VrrpGroup referenced interfaces exist.
      Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
      c.setDefaultCrossZoneAction(LineAction.PERMIT);
      c.setDefaultInboundAction(LineAction.PERMIT);

      SortedMap<Integer, VrrpGroup> vrrpGroups =
          ImmutableSortedMap.of(
              1,
              VrrpGroup.builder()
                  .addVirtualAddress("exists", Ip.parse("1.1.1.1"))
                  .addVirtualAddress("alsoExists", Ip.parse("2.2.2.2"))
                  .build());
      Interface.builder()
          .setOwner(c)
          .setName("exists")
          .setTopology(l13NoEncapsulation("exists"))
          .setVrrpGroups(vrrpGroups)
          .build();
      Interface.builder()
          .setOwner(c)
          .setName("alsoExists")
          .setTopology(l13NoEncapsulation("alsoExists"))
          .build();
      Warnings w = new Warnings(false, true, false);
      finalizeConfiguration(c, w);

      assertThat(c.getAllInterfaces().get("exists").getVrrpGroups(), equalTo(vrrpGroups));
      assertThat(w.getRedFlagWarnings(), empty());
    }
  }

  @Test
  public void testRemoveInvalidStaticRoutes() {
    Configuration c =
        Configuration.builder()
            .setHostname("foo")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();
    Vrf v = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c).build();
    Interface.builder()
        .setName("i1")
        .setVrf(v)
        .setOwner(c)
        .setTopology(l13NoEncapsulation("i1"))
        .build();

    StaticRoute intMissing =
        StaticRoute.builder()
            .setNextHop(NextHopInterface.of("missing"))
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .build();
    StaticRoute intPresent =
        StaticRoute.builder()
            .setNextHop(NextHopInterface.of("i1"))
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .build();
    StaticRoute vrfPresent =
        StaticRoute.builder()
            .setNextHop(NextHopVrf.of(DEFAULT_VRF_NAME))
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .build();
    StaticRoute vrfMissing =
        StaticRoute.builder()
            .setNextHop(NextHopVrf.of("missing"))
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .build();
    StaticRoute ip =
        StaticRoute.builder()
            .setNextHop(NextHopIp.of(Ip.parse("1.1.1.1")))
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .build();
    StaticRoute discard =
        StaticRoute.builder()
            .setNextHop(NextHopDiscard.instance())
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .build();

    v.setStaticRoutes(
        ImmutableSortedSet.of(intMissing, intPresent, vrfPresent, vrfMissing, ip, discard));

    Warnings w = new Warnings(false, true, false);
    finalizeConfiguration(c, w);

    assertThat(v.getStaticRoutes(), containsInAnyOrder(intPresent, vrfPresent, ip, discard));
    assertThat(
        w.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText(
                "Removing invalid static route on node 'foo' with undefined next hop interface"
                    + " 'missing'"),
            hasText(
                "Removing invalid static route on node 'foo' with undefined next hop vrf"
                    + " 'missing'")));
  }

  @Test
  public void testRemoveInvalidTrackReferences() {
    Configuration c =
        Configuration.builder()
            .setHostname("foo")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();
    Vrf v = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c).build();
    StaticRoute srMissing =
        StaticRoute.builder()
            .setNextHop(NextHopDiscard.instance())
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .setTrack("missing")
            .build();
    StaticRoute srPresent =
        StaticRoute.builder()
            .setNextHop(NextHopDiscard.instance())
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .setTrack("present")
            .build();
    HsrpGroup hsrpGroup =
        HsrpGroup.builder()
            .setTrackActions(
                ImmutableSortedMap.of(
                    "missing", new DecrementPriority(1), "present", new DecrementPriority(1)))
            .build();
    Interface.builder()
        .setName("i1")
        .setVrf(v)
        .setOwner(c)
        .setHsrpGroups(ImmutableMap.of(1, hsrpGroup))
        .setTopology(l13NoEncapsulation("i1"))
        .build();
    v.setStaticRoutes(ImmutableSortedSet.of(srMissing, srPresent));
    c.getTrackingGroups().put("present", alwaysTrue());

    Warnings w = new Warnings(false, true, false);
    finalizeConfiguration(c, w);

    assertThat(
        c.getAllInterfaces().get("i1").getHsrpGroups().get(1).getTrackActions(),
        hasKeys("present"));
    assertThat(v.getStaticRoutes(), containsInAnyOrder(hasTrack(nullValue()), hasTrack("present")));

    assertThat(
        w.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText("Removing reference to undefined track 'missing' in HSRP group 1 on 'foo[i1]'"),
            hasText(
                "Removing reference to undefined track 'missing' on static route for prefix"
                    + " 0.0.0.0/0 in vrf 'default'")));
  }

  @Test
  public void testVerifyInterfaces() {
    InterfaceAddress address = ConcreteInterfaceAddress.parse("1.1.1.1/24");

    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();
    Vrf v = Vrf.builder().setName("v").setOwner(c).build();
    // good
    Interface switchportOnModeAccess =
        Interface.builder()
            .setName("switchportOnModeAccess")
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setVrf(v)
            .setOwner(c)
            .build();
    switchportOnModeAccess.setTopology(computeLegacyInterfaceTopology(switchportOnModeAccess));
    // bad
    Interface.builder()
        .setName("switchportOnModeNone")
        .setSwitchport(true)
        .setSwitchportMode(SwitchportMode.NONE)
        .setVrf(v)
        .setOwner(c)
        .build();
    // bad
    Interface.builder()
        .setName("switchportOffModeAccess")
        .setSwitchport(false)
        .setSwitchportMode(SwitchportMode.ACCESS)
        .setVrf(v)
        .setOwner(c)
        .build();
    // bad
    Interface.builder()
        .setName("switchportAndL3")
        .setSwitchport(true)
        .setSwitchportMode(SwitchportMode.ACCESS)
        .setAddress(address)
        .setVrf(v)
        .setOwner(c)
        .build();
    // bad
    Interface.builder()
        .setName("vlanNoVlan")
        .setType(InterfaceType.VLAN)
        .setVrf(v)
        .setOwner(c)
        .build();
    // good
    Interface vlanWithVlan =
        Interface.builder()
            .setName("vlanWithVlan")
            .setType(InterfaceType.VLAN)
            .setVlan(5)
            .setVrf(v)
            .setOwner(c)
            .build();
    vlanWithVlan.setTopology(computeLegacyInterfaceTopology(vlanWithVlan));
    // bad
    Interface.builder()
        .setName("channelGroupAndL3")
        .setType(InterfaceType.PHYSICAL)
        .setChannelGroup("aggregated")
        .setAddress(address)
        .setVrf(v)
        .setOwner(c)
        .build();
    // good
    Interface aggregated =
        Interface.builder()
            .setName("aggregated")
            .setType(InterfaceType.AGGREGATED)
            .setVrf(v)
            .setOwner(c)
            .build();
    aggregated.setTopology(computeLegacyInterfaceTopology(aggregated));
    // good
    Interface channelGroup =
        Interface.builder()
            .setName("channelGroup")
            .setType(InterfaceType.PHYSICAL)
            .setChannelGroup("aggregated")
            .setVrf(v)
            .setOwner(c)
            .build();
    channelGroup.setTopology(computeLegacyInterfaceTopology(channelGroup));
    // bad
    Interface.builder()
        .setName("l3AndParentChannelGroup")
        .setType(InterfaceType.LOGICAL)
        .setDependencies(ImmutableSet.of(new Dependency("channelGroup", DependencyType.BIND)))
        .setAddress(address)
        .setVrf(v)
        .setOwner(c)
        .build();
    // bad
    Interface.builder()
        .setName("missingBindDep")
        .setType(InterfaceType.LOGICAL)
        .setDependencies(ImmutableSet.of(new Dependency("undefined", DependencyType.BIND)))
        .setVrf(v)
        .setOwner(c)
        .build();
    // bad
    Interface missingAggregateDep =
        Interface.builder()
            .setName("missingAggregateDep")
            .setType(InterfaceType.AGGREGATED)
            .setDependencies(ImmutableSet.of(new Dependency("undefined", DependencyType.AGGREGATE)))
            .setVrf(v)
            .setOwner(c)
            .build();
    missingAggregateDep.setTopology(computeLegacyInterfaceTopology(missingAggregateDep));

    Warnings w = new Warnings(false, true, false);
    finalizeConfiguration(c, w);

    assertThat(
        w.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText("Interface switchportOnModeNone has switchport true but switchport mode NONE"),
            hasText(
                "Interface switchportOffModeAccess has switchport false but switchport mode"
                    + " ACCESS"),
            hasText("Interface switchportAndL3 is a switchport, but it has L3 addresses"),
            hasText("Interface vlanNoVlan is a VLAN interface but has no vlan set"),
            hasText(
                "Interface channelGroupAndL3 is a member of AGGREGATED interface aggregated but it"
                    + " has L3 addresses"),
            hasText(
                "Interface l3AndParentChannelGroup is a child of a member of AGGREGATED interface"
                    + " aggregated but it has L3 addresses"),
            hasText(
                "Interface missingBindDep has a bind dependency on missing interface undefined"),
            hasText(
                "Interface missingAggregateDep has an aggregate dependency on missing interface"
                    + " undefined")));
    assertThat(
        c.getAllInterfaces(),
        hasKeys(
            "switchportOnModeAccess",
            "vlanWithVlan",
            "aggregated",
            "channelGroup",
            "missingAggregateDep"));
    assertThat(missingAggregateDep.getDependencies(), empty());
  }

  @Test
  public void testVerifyInterfacesOverlappingTagsL2L2() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();
    Vrf v = Vrf.builder().setName("v").setOwner(c).build();
    // bad: overlapping tags on subinterfaces
    Interface overlappingTags =
        Interface.builder()
            .setName("overlappingTags")
            .setType(InterfaceType.PHYSICAL)
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTags.setTopology(computeLegacyInterfaceTopology(overlappingTags));
    // good in isolation, but removed because of overlapping allowed tags with overlappingTagsSub2
    Interface overlappingTagsSub1 =
        Interface.builder()
            .setName("overlappingTagsSub1")
            .setType(InterfaceType.LOGICAL)
            .setDependencies(
                ImmutableSet.of(new Dependency("overlappingTags", DependencyType.BIND)))
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.TRUNK)
            .setNativeVlan(6) // untagged
            .setAllowedVlans(IntegerSpace.of(Range.closed(1, 1000)))
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTagsSub1.setTopology(computeLegacyInterfaceTopology(overlappingTagsSub1));
    // good in isolation, but removed because of overlapping allowed tags with overlappingTagsSub1
    Interface overlappingTagsSub2 =
        Interface.builder()
            .setName("overlappingTagsSub2")
            .setType(InterfaceType.LOGICAL)
            .setDependencies(
                ImmutableSet.of(new Dependency("overlappingTags", DependencyType.BIND)))
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(6) // allows untagged
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTagsSub2.setTopology(computeLegacyInterfaceTopology(overlappingTagsSub2));

    Warnings w = new Warnings(false, true, false);
    finalizeConfiguration(c, w);

    assertThat(
        w.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText(
                "L1 Interface overlappingTags has overlapping tags on edges to L2 and L3"
                    + " interfaces")));
    assertThat(c.getAllInterfaces(), anEmptyMap());
  }

  @Test
  public void testVerifyInterfacesOverlappingTagsL2L3() {
    InterfaceAddress address = ConcreteInterfaceAddress.parse("1.1.1.1/24");

    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();
    Vrf v = Vrf.builder().setName("v").setOwner(c).build();
    // bad: overlapping tags on subinterfaces
    Interface overlappingTags =
        Interface.builder()
            .setName("overlappingTags")
            .setType(InterfaceType.PHYSICAL)
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTags.setTopology(computeLegacyInterfaceTopology(overlappingTags));
    // good in isolation, but removed because of overlapping allowed tags with overlappingTagsSub2
    Interface overlappingTagsSub1 =
        Interface.builder()
            .setName("overlappingTagsSub1")
            .setType(InterfaceType.LOGICAL)
            .setDependencies(
                ImmutableSet.of(new Dependency("overlappingTags", DependencyType.BIND)))
            .setAddress(address)
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTagsSub1.setTopology(computeLegacyInterfaceTopology(overlappingTagsSub1));
    // good in isolation, but removed because of overlapping allowed tags with overlappingTagsSub1
    Interface overlappingTagsSub2 =
        Interface.builder()
            .setName("overlappingTagsSub2")
            .setType(InterfaceType.LOGICAL)
            .setDependencies(
                ImmutableSet.of(new Dependency("overlappingTags", DependencyType.BIND)))
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(6) // allows untagged
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTagsSub2.setTopology(computeLegacyInterfaceTopology(overlappingTagsSub2));

    Warnings w = new Warnings(false, true, false);
    finalizeConfiguration(c, w);

    assertThat(
        w.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText(
                "L1 Interface overlappingTags has overlapping tags on edges to L2 and L3"
                    + " interfaces")));
    assertThat(c.getAllInterfaces(), anEmptyMap());
  }

  @Test
  public void testVerifyInterfacesOverlappingTagsL3L3() {
    InterfaceAddress address1 = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    InterfaceAddress address2 = ConcreteInterfaceAddress.parse("2.2.2.2/24");

    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();
    Vrf v = Vrf.builder().setName("v").setOwner(c).build();
    // bad: overlapping tags on subinterfaces
    Interface overlappingTags =
        Interface.builder()
            .setName("overlappingTags")
            .setType(InterfaceType.PHYSICAL)
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTags.setTopology(computeLegacyInterfaceTopology(overlappingTags));
    // good in isolation, but removed because of overlapping allowed tags with overlappingTagsSub2
    Interface overlappingTagsSub1 =
        Interface.builder()
            .setName("overlappingTagsSub1")
            .setType(InterfaceType.LOGICAL)
            .setDependencies(
                ImmutableSet.of(new Dependency("overlappingTags", DependencyType.BIND)))
            .setAddress(address1)
            .setEncapsulationVlan(10)
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTagsSub1.setTopology(computeLegacyInterfaceTopology(overlappingTagsSub1));
    // good in isolation, but removed because of overlapping allowed tags with overlappingTagsSub1
    Interface overlappingTagsSub2 =
        Interface.builder()
            .setName("overlappingTagsSub2")
            .setType(InterfaceType.LOGICAL)
            .setDependencies(
                ImmutableSet.of(new Dependency("overlappingTags", DependencyType.BIND)))
            .setAddress(address2)
            .setEncapsulationVlan(10)
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTagsSub2.setTopology(computeLegacyInterfaceTopology(overlappingTagsSub2));

    Warnings w = new Warnings(false, true, false);
    finalizeConfiguration(c, w);

    assertThat(
        w.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText(
                "L1 Interface overlappingTags has overlapping tags on edges to L2 and L3"
                    + " interfaces")));
    assertThat(c.getAllInterfaces(), anEmptyMap());
  }

  @Test
  public void testVerifyInterfacesOverlappingTagsNonOverlappingL3L3() {
    InterfaceAddress address1 = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    InterfaceAddress address2 = ConcreteInterfaceAddress.parse("2.2.2.2/24");

    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();
    Vrf v = Vrf.builder().setName("v").setOwner(c).build();
    // bad: overlapping tags on subinterfaces
    Interface overlappingTags =
        Interface.builder()
            .setName("overlappingTags")
            .setType(InterfaceType.PHYSICAL)
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTags.setTopology(computeLegacyInterfaceTopology(overlappingTags));
    // good in isolation, but removed because of overlapping allowed tags with overlappingTagsSub2
    Interface overlappingTagsSub1 =
        Interface.builder()
            .setName("overlappingTagsSub1")
            .setType(InterfaceType.LOGICAL)
            .setDependencies(
                ImmutableSet.of(new Dependency("overlappingTags", DependencyType.BIND)))
            .setAddress(address1)
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTagsSub1.setTopology(computeLegacyInterfaceTopology(overlappingTagsSub1));
    // good in isolation, but removed because of overlapping allowed tags with overlappingTagsSub1
    Interface overlappingTagsSub2 =
        Interface.builder()
            .setName("overlappingTagsSub2")
            .setType(InterfaceType.LOGICAL)
            .setDependencies(
                ImmutableSet.of(new Dependency("overlappingTags", DependencyType.BIND)))
            .setAddress(address2)
            .setEncapsulationVlan(10)
            .setVrf(v)
            .setOwner(c)
            .build();
    overlappingTagsSub2.setTopology(computeLegacyInterfaceTopology(overlappingTagsSub2));

    Warnings w = new Warnings(false, true, false);
    finalizeConfiguration(c, w);

    assertThat(w.getRedFlagWarnings(), empty());
    assertThat(
        c.getAllInterfaces(),
        hasKeys("overlappingTags", "overlappingTagsSub1", "overlappingTagsSub2"));
  }

  @Test
  public void testVerifyOspfAreas() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();
    Vrf v = Vrf.builder().setName("v").setOwner(c).build();
    OspfArea area =
        OspfArea.builder()
            .setInterfaces(ImmutableList.of("undefined", "defined"))
            .setNumber(1L)
            .build();
    OspfProcess proc =
        OspfProcess.builder()
            .setAreas(ImmutableMap.of(area.getAreaNumber(), area))
            .setProcessId("p")
            .setReferenceBandwidth(1E9D)
            .setRouterId(Ip.ZERO)
            .build();
    v.setOspfProcesses(ImmutableSortedMap.of(proc.getProcessId(), proc));
    Interface defined = Interface.builder().setName("defined").setOwner(c).setVrf(v).build();
    defined.setTopology(computeLegacyInterfaceTopology(defined));

    Warnings w = new Warnings(false, true, false);
    finalizeConfiguration(c, w);

    assertThat(
        w.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText(
                "Removing undefined interfaces [undefined] from OSPF process p area 1 on node c vrf"
                    + " v")));
    assertThat(
        proc.getAreas().get(area.getAreaNumber()).getInterfaces(),
        equalTo(ImmutableSet.of("defined")));
  }
}
