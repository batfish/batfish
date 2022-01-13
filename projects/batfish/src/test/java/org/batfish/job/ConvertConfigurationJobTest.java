package org.batfish.job;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.job.ConvertConfigurationJob.finalizeConfiguration;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedMap;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
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
  public void testFInalizeConfigurationVerifyVrrpGroups() {
    {
      // Test that undefined interface is removed from VrrpGroup, and warning is added.
      Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS);
      c.setDefaultCrossZoneAction(LineAction.PERMIT);
      c.setDefaultInboundAction(LineAction.PERMIT);

      Interface.builder()
          .setOwner(c)
          .setName("exists")
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
      Interface.builder().setOwner(c).setName("exists").setVrrpGroups(vrrpGroups).build();
      Interface.builder().setOwner(c).setName("alsoExists").build();
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
    Interface.builder().setName("i1").setVrf(v).setOwner(c).build();

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
}
