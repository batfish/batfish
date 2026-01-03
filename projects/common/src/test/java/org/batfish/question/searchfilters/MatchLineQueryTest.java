package org.batfish.question.searchfilters;

import static org.batfish.datamodel.ExprAclLine.acceptingHeaderSpace;
import static org.batfish.datamodel.ExprAclLine.rejectingHeaderSpace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests of {@link MatchLineQuery} */
public class MatchLineQueryTest {
  @Test
  public void testCanQuery() {
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(ImmutableList.of(ExprAclLine.ACCEPT_ALL))
            .build();

    // Match line queries should work only if the line number is within range for the ACL
    MatchLineQuery matchLine0 = new MatchLineQuery(0);
    MatchLineQuery matchLine1 = new MatchLineQuery(1);
    assertTrue(matchLine0.canQuery(acl));
    assertFalse(matchLine1.canQuery(acl));
  }

  @Test
  public void testGetMatchingBdd() {
    BDDPacket pkt = new BDDPacket();
    IpAccessListToBdd ipAccessListToBdd =
        new IpAccessListToBddImpl(
            pkt, BDDSourceManager.empty(pkt), ImmutableMap.of(), ImmutableMap.of());

    // ACL accepts ip1, rejects ip2, accepts prefix that contains both
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.2");
    Prefix prefix = Prefix.parse("1.1.1.0/24");
    HeaderSpace ip1HeaderSpace = HeaderSpace.builder().setDstIps(ip1.toIpSpace()).build();
    HeaderSpace ip2HeaderSpace = HeaderSpace.builder().setDstIps(ip2.toIpSpace()).build();
    HeaderSpace prefixHeaderSpace = HeaderSpace.builder().setDstIps(prefix.toIpSpace()).build();
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(ip1HeaderSpace),
                    rejectingHeaderSpace(ip2HeaderSpace),
                    acceptingHeaderSpace(prefixHeaderSpace)))
            .build();

    HeaderSpaceToBDD headerSpaceToBDD = new HeaderSpaceToBDD(pkt, ImmutableMap.of());
    BDD ip1Bdd = headerSpaceToBDD.toBDD(ip1HeaderSpace);
    BDD ip2Bdd = headerSpaceToBDD.toBDD(ip2HeaderSpace);
    BDD prefixBdd = headerSpaceToBDD.toBDD(prefixHeaderSpace);

    // Match line 0: Matching BDD should only include dst IP ip1
    MatchLineQuery matchLine0 = new MatchLineQuery(0);
    assertThat(matchLine0.getMatchingBdd(acl, ipAccessListToBdd, pkt), equalTo(ip1Bdd));

    // Match line 1: Matching BDD should only include dst IP ip2
    MatchLineQuery matchLine1 = new MatchLineQuery(1);
    assertThat(matchLine1.getMatchingBdd(acl, ipAccessListToBdd, pkt), equalTo(ip2Bdd));

    // Match line 2: Matching BDD should be the prefix except ip1 and ip2
    MatchLineQuery matchLine2 = new MatchLineQuery(2);
    BDD expectedBdd = prefixBdd.diff(ip1Bdd.or(ip2Bdd));
    assertThat(matchLine2.getMatchingBdd(acl, ipAccessListToBdd, pkt), equalTo(expectedBdd));
  }
}
