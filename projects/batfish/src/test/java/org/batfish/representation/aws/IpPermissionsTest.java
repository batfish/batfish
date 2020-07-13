package org.batfish.representation.aws;

import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.matchers.ExprAclLineMatchers.hasMatchCondition;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasChildren;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasTraceElement;
import static org.batfish.representation.aws.Utils.getTraceElementForRule;
import static org.batfish.representation.aws.Utils.traceElementEniPrivateIp;
import static org.batfish.representation.aws.Utils.traceElementForAddress;
import static org.batfish.representation.aws.Utils.traceElementForDstPorts;
import static org.batfish.representation.aws.Utils.traceElementForProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.representation.aws.IpPermissions.AddressType;
import org.batfish.representation.aws.IpPermissions.UserIdGroupPair;
import org.junit.Test;

/** Test for {@link IpPermissions} */
public class IpPermissionsTest {
  private static final String SG_NAME = "sg";
  private static final String SG_ID = "sg-id";
  private static final String SG_DESC = "sg desc";
  private static final String INSTANCE_1 = "i1";
  private static final String INSTANCE_2 = "i2";
  private static final String PL_ID = "pl-id";
  private static final String PL_NAME = "pl name";

  private static final MatchHeaderSpace matchTcp =
      new MatchHeaderSpace(
          HeaderSpace.builder().setIpProtocols(TCP).build(), traceElementForProtocol(TCP));

  private static final MatchHeaderSpace matchSSH =
      new MatchHeaderSpace(
          HeaderSpace.builder().setDstPorts(SubRange.singleton(22)).build(),
          traceElementForDstPorts(22, 22));

  private static Region testRegion() {
    Region region = new Region("test");
    SecurityGroup sg =
        new SecurityGroup(SG_ID, SG_NAME, ImmutableList.of(), ImmutableList.of(), "vpc");
    sg.getReferrerIps().put(Ip.parse("1.1.1.1"), INSTANCE_1);
    sg.getReferrerIps().put(Ip.parse("2.2.2.2"), INSTANCE_2);
    region.getSecurityGroups().put(sg.getId(), sg);
    PrefixList pl =
        new PrefixList(
            PL_ID,
            ImmutableList.of(
                Prefix.parse("1.1.1.0/24"), Prefix.parse("2.2.2.0/24"), Prefix.parse("2.2.2.0/24")),
            PL_NAME);
    region.getPrefixLists().put(pl.getId(), pl);
    return region;
  }

  @Test
  public void userIdGroupsToAclLines() {
    IpPermissions ipPermissions =
        new IpPermissions(
            "tcp",
            22,
            22,
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableList.of(new UserIdGroupPair(SG_ID, SG_DESC)));
    List<ExprAclLine> lines =
        ipPermissions.toIpAccessListLines(true, testRegion(), "acl line", new Warnings());

    // check if source IPs are populated from all UserIpSpaces in the referred security group
    assertThat(
        Iterables.getOnlyElement(lines),
        hasMatchCondition(
            and(
                matchTcp,
                matchSSH,
                new OrMatchExpr(
                    ImmutableList.of(
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(Ip.parse("1.1.1.1").toIpSpace())
                                .build(),
                            traceElementEniPrivateIp(INSTANCE_1)),
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(Ip.parse("2.2.2.2").toIpSpace())
                                .build(),
                            traceElementEniPrivateIp(INSTANCE_2))),
                    traceElementForAddress("source", SG_NAME, AddressType.SECURITY_GROUP)))));
    // check if rule description is populated from UserIdGroup description
    assertThat(
        Iterables.getOnlyElement(lines).getTraceElement(),
        equalTo(getTraceElementForRule(SG_DESC)));
  }

  private static Region createTestRegion() {
    Region region = new Region("test");
    String vpcId = "vpc";
    String subnetId = "subnet";
    String ifaceName = "eni-123";
    SecurityGroup sg1 =
        new SecurityGroup(SG_ID, SG_NAME, ImmutableList.of(), ImmutableList.of(), vpcId);

    NetworkInterface ni =
        new NetworkInterface(
            ifaceName,
            subnetId,
            vpcId,
            ImmutableList.of(SG_ID),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("1.2.3.4"), null)),
            "no desc",
            null,
            ImmutableMap.of());
    region.getNetworkInterfaces().put(ni.getId(), ni);
    region.getSecurityGroups().put(sg1.getId(), sg1);

    region.addNetworkInterfaceIpsToSecurityGroups(new Warnings());
    return region;
  }

  @Test
  public void testUserIdGroupTracing() {
    IpPermissions ipPermissions =
        new IpPermissions(
            "tcp",
            22,
            22,
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableList.of(new UserIdGroupPair(SG_ID, SG_DESC)));
    List<ExprAclLine> lines =
        ipPermissions.toIpAccessListLines(true, createTestRegion(), "line", new Warnings());
    IpAccessList aclList =
        IpAccessList.builder()
            // need to add to list<AclLine> because toIpAccessListLines return ExprAclLine
            .setLines(ImmutableList.<AclLine>builder().addAll(lines).build())
            .setName("lines")
            .build();

    Flow flow =
        Flow.builder()
            .setIpProtocol(TCP)
            .setSrcPort(22)
            .setSrcIp(Ip.parse("1.2.3.4"))
            .setDstPort(22)
            .setIngressNode("c")
            .build();
    List<TraceTree> root =
        AclTracer.trace(
            aclList, flow, null, ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());

    assertThat(
        root,
        contains(
            allOf(
                hasTraceElement(getTraceElementForRule(SG_DESC)),
                hasChildren(
                    containsInAnyOrder(
                        hasTraceElement(traceElementForProtocol(TCP)),
                        hasTraceElement(traceElementForDstPorts(22, 22)),
                        allOf(
                            hasTraceElement(
                                traceElementForAddress(
                                    "source", SG_NAME, AddressType.SECURITY_GROUP)),
                            hasChildren(
                                containsInAnyOrder(
                                    hasTraceElement(traceElementEniPrivateIp("eni-123"))))))))));

    Flow deniedFlow =
        Flow.builder()
            .setIpProtocol(TCP)
            .setSrcPort(22)
            .setSrcIp(Ip.parse("1.2.3.5"))
            .setDstPort(22)
            .setIngressNode("c")
            .build();
    root =
        AclTracer.trace(
            aclList, deniedFlow, null, ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(root, empty());
  }

  @Test
  public void testCollectPrefixLists() {
    Region region = testRegion();
    Map<PrefixList, IpSpace> prefixListIpSpaceMap =
        IpPermissions.collectPrefixLists(region, ImmutableList.of(PL_ID, PL_ID));
    assertThat(
        prefixListIpSpaceMap,
        equalTo(
            ImmutableMap.of(
                region.getPrefixLists().get(PL_ID),
                IpWildcardSetIpSpace.builder()
                    .including(IpWildcard.parse("1.1.1.0/24"), IpWildcard.parse("2.2.2.0/24"))
                    .build())));
  }
}
