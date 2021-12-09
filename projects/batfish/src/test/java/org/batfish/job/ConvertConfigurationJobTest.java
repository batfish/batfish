package org.batfish.job;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Names.generatedTenantVniInterfaceName;
import static org.batfish.job.ConvertConfigurationJob.addTenantVniInterfaces;
import static org.batfish.job.ConvertConfigurationJob.finalizeConfiguration;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.vxlan.Layer3Vni;
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
  public void testAddTenantVniInterfaces() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_NX);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);
    Vrf.Builder vb = Vrf.builder().setOwner(c);
    Vrf v1 = vb.setName("v1").build();
    Vrf v2 = vb.setName("v2").build();
    Ip sourceAddress = Ip.parse("10.0.0.1");
    Layer3Vni.Builder l3b =
        Layer3Vni.builder()
            .setBumTransportIps(ImmutableSet.of())
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setUdpPort(5);
    v1.addLayer3Vni(l3b.setVni(1).setSourceAddress(sourceAddress).build());
    v2.addLayer3Vni(l3b.setVni(2).setSourceAddress(null).build());
    String vni1IfaceName = generatedTenantVniInterfaceName(1);
    String vni2IfaceName = generatedTenantVniInterfaceName(2);
    addTenantVniInterfaces(c);

    // v1 vni 1
    assertThat(c.getAllInterfaces(v1.getName()), hasKey(vni1IfaceName));

    Interface vni1Iface = c.getAllInterfaces().get(vni1IfaceName);

    assertThat(vni1Iface.getAdditionalArpIps(), equalTo(sourceAddress.toIpSpace()));

    // v2 vni 2

    assertThat(c.getAllInterfaces(v2.getName()), hasKey(vni2IfaceName));

    Interface vni2Iface = c.getAllInterfaces().get(vni2IfaceName);

    assertThat(vni2Iface.getAdditionalArpIps(), equalTo(EmptyIpSpace.INSTANCE));
  }
}
