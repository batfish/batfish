package org.batfish.datamodel.acl;

import static org.batfish.datamodel.matchers.AclLineMatchExprMatchers.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Before;
import org.junit.Test;

public class PermittedByAclTest {
  private NetworkFactory _nf;

  private static Flow createFlow(String ipAddrStr) {
    Flow.Builder b = Flow.builder();
    b.setIngressNode("ingressNode");
    b.setSrcIp(Ip.parse(ipAddrStr));
    return b.build();
  }

  private Map<String, IpAccessList> createAclMap(
      String aclName, String srcIpWildcard, LineAction lineAction) {
    // Build a single entry map, mapping aclName to an ACL matching the given srcIpWildcard

    // Create a single ACL line matching the given srcIpWildcard
    ExprAclLine acll =
        ExprAclLine.builder()
            .setMatchCondition(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(ImmutableSet.of(IpWildcard.parse(srcIpWildcard)))
                        .build()))
            .setAction(lineAction)
            .build();

    // Add that single ACL line to a new ACL
    IpAccessList.Builder aclb = _nf.aclBuilder();
    aclb.setName(aclName);
    aclb.setLines(ImmutableList.of(acll));

    // Return a map, mapping aclName to the ACL itself
    return ImmutableMap.of(aclName, aclb.build());
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
  }

  @Test
  public void testAclMatch() {
    // Test ACL line expressions which are permitted by other ACLs

    Map<String, IpAccessList> acceptAcl = createAclMap("acl1", "1.2.3.4/32", LineAction.PERMIT);
    Map<String, IpAccessList> rejectAcl = createAclMap("acl1", "1.2.3.4/32", LineAction.DENY);
    PermittedByAcl exprMatch = new PermittedByAcl("acl1");

    // Confirm a flow matching the ACL is correctly identified as accepted
    assertThat(exprMatch, matches(createFlow("1.2.3.4"), "", acceptAcl));

    // Confirm a flow NOT matching the ACL is correctly identified as NOT accepted
    assertThat(exprMatch, not(matches(createFlow("10.10.10.10"), "", acceptAcl)));

    // Confirm a flow matching the ACL is correctly identified as NOT accepted (line action is
    // reject)
    assertThat(exprMatch, not(matches(createFlow("1.2.3.4"), "", rejectAcl)));
  }
}
