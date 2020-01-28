package org.batfish.representation.aws;

import static org.batfish.datamodel.matchers.ExprAclLineMatchers.hasMatchCondition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.List;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.aws.IpPermissions.UserIdGroupPair;
import org.junit.Test;

/** Test for {@link IpPermissions} */
public class IpPermissionsTest {
  private static final String SG_NAME = "sg";
  private static final String SG_ID = "sg-id";

  private static Region testRegion() {
    Region region = new Region("test");
    SecurityGroup sg = new SecurityGroup(SG_ID, SG_NAME, ImmutableList.of(), ImmutableList.of());
    sg.getUsersIpSpace().add(IpWildcard.parse("1.1.1.0/24"));
    sg.getUsersIpSpace().add(IpWildcard.parse("2.2.2.0/24"));
    region.getSecurityGroups().put(sg.getGroupId(), sg);
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
            ImmutableList.of(new UserIdGroupPair(SG_ID, "sg desc")));
    List<ExprAclLine> lines =
        ipPermissions.userIdGroupsToAclLines(testRegion(), ImmutableList.of(), true, "acl line");

    // check if source IPs are populated from all UserIpSpaces in the referred security group
    assertThat(
        Iterables.getOnlyElement(lines),
        hasMatchCondition(
            new MatchHeaderSpace(
                HeaderSpace.builder()
                    .setSrcIps(
                        IpWildcardSetIpSpace.builder()
                            .including(IpWildcard.parse("1.1.1.0/24"))
                            .including(IpWildcard.parse("2.2.2.0/24"))
                            .build())
                    .build(),
                "Matched source address Security Group sg")));
    // check if rule description is populated from UserIdGroup description
    assertThat(
        Iterables.getOnlyElement(lines).getTraceElement(),
        equalTo(TraceElement.of("Matched rule with description sg desc")));
  }
}
