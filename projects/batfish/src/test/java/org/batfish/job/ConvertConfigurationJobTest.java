package org.batfish.job;

import static org.batfish.job.ConvertConfigurationJob.finalizeConfiguration;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
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
}
