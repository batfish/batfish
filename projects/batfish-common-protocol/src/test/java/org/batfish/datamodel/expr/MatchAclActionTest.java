package org.batfish.datamodel.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Before;
import org.junit.Test;

public class MatchAclActionTest {
  private NetworkFactory _nf;

  private static Flow createFlow(String ipAddrStr) {
    Flow.Builder b = new Flow.Builder();
    b.setIngressNode("ingressNode");
    b.setSrcIp(new Ip(ipAddrStr));
    b.setTag("test");
    return b.build();
  }

  private IpAccessList createAcl(String aclName, String srcIpWildcard) {
    IpAccessList.Builder aclb = _nf.aclBuilder();
    IpAccessListLine.Builder acllb = IpAccessListLine.builder();
    acllb.setSrcIps(ImmutableSet.of(new IpWildcard(srcIpWildcard)));
    IpAccessListLine acll = acllb.build();
    aclb.setName(aclName);
    aclb.setLines(ImmutableList.of(acll));
    return aclb.build();
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
  }

  @Test
  public void testAclMatch() {
    // Test ACL line expressions matching other ACLs

    IpAccessList acl = createAcl("acl1", "1.2.3.4/32");
    MatchAcl exprMatch = new MatchAcl("acl1");

    // Confirm MatchAcl.match correctly finds the matching ACL line for the given source address
    assertThat(exprMatch.match(createFlow("1.2.3.4"), "", ImmutableSet.of(acl)), equalTo(true));

    // Confirm MatchAcl.match does not find a matching ACL line for the given source address
    assertThat(
        exprMatch.match(createFlow("10.10.10.10"), "", ImmutableSet.of(acl)), equalTo(false));
  }
}
