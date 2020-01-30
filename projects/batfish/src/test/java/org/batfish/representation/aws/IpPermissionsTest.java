package org.batfish.representation.aws;

import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.matchers.ExprAclLineMatchers.hasMatchCondition;
import static org.batfish.representation.aws.Utils.getTraceElementForRule;
import static org.batfish.representation.aws.Utils.traceElementForAddress;
import static org.batfish.representation.aws.Utils.traceElementForDstPorts;
import static org.batfish.representation.aws.Utils.traceElementForProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.aws.IpPermissions.AddressType;
import org.batfish.representation.aws.IpPermissions.UserIdGroupPair;
import org.junit.Test;

/** Test for {@link IpPermissions} */
public class IpPermissionsTest {
  private static final String SG_NAME = "sg";
  private static final String SG_ID = "sg-id";
  private static final String SG_DESC = "sg desc";
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
    SecurityGroup sg = new SecurityGroup(SG_ID, SG_NAME, ImmutableList.of(), ImmutableList.of());
    sg.getUsersIpSpace().add(IpWildcard.parse("1.1.1.0/24"));
    sg.getUsersIpSpace().add(IpWildcard.parse("2.2.2.0/24"));
    region.getSecurityGroups().put(sg.getGroupId(), sg);
    PrefixList pl =
        new PrefixList(
            PL_ID,
            ImmutableList.of(Prefix.parse("1.1.1.0/24"), Prefix.parse("2.2.2.0/24")),
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
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(
                            IpWildcardSetIpSpace.builder()
                                .including(IpWildcard.parse("1.1.1.0/24"))
                                .including(IpWildcard.parse("2.2.2.0/24"))
                                .build())
                        .build(),
                    traceElementForAddress("source", SG_NAME, AddressType.SECURITY_GROUP)))));
    // check if rule description is populated from UserIdGroup description
    assertThat(
        Iterables.getOnlyElement(lines).getTraceElement(),
        equalTo(getTraceElementForRule(SG_DESC)));
  }

  @Test
  public void testCollectPrefixLists() {
    Region region = testRegion();
    Map<PrefixList, IpSpace> prefixListIpSpaceMap =
        IpPermissions.collectPrefixLists(region, ImmutableList.of(PL_ID));
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
