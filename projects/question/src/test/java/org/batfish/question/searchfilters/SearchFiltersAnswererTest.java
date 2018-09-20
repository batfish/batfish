package org.batfish.question.searchfilters;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstIp;
import static org.batfish.question.searchfilters.SearchFiltersAnswerer.toMatchLineAcl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.junit.Test;

public class SearchFiltersAnswererTest {
  IpAccessList _acl =
      IpAccessList.builder()
          .setName("foo")
          .setLines(
              ImmutableList.of(
                  IpAccessListLine.accepting().setMatchCondition(matchDstIp("1.1.1.1")).build(),
                  IpAccessListLine.rejecting().setMatchCondition(matchDstIp("1.1.1.2")).build(),
                  IpAccessListLine.rejecting().setMatchCondition(matchDstIp("1.1.1.3")).build(),
                  IpAccessListLine.accepting().setMatchCondition(matchDstIp("1.1.1.4")).build()))
          .build();

  @Test
  public void testToMatchLineAcl_0() {
    IpAccessList matchLine0Acl =
        IpAccessList.builder()
            .setName("foo")
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting().setMatchCondition(matchDstIp("1.1.1.1")).build()))
            .build();
    assertThat(toMatchLineAcl(0, _acl), equalTo(matchLine0Acl));
  }

  @Test
  public void testToMatchLineAcl_2() {
    IpAccessList matchLine2Acl =
        IpAccessList.builder()
            .setName("foo")
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.rejecting().setMatchCondition(matchDstIp("1.1.1.1")).build(),
                    IpAccessListLine.rejecting().setMatchCondition(matchDstIp("1.1.1.2")).build(),
                    IpAccessListLine.accepting().setMatchCondition(matchDstIp("1.1.1.3")).build()))
            .build();
    assertThat(toMatchLineAcl(2, _acl), equalTo(matchLine2Acl));
  }

  @Test
  public void testToDenyAcl() {
    IpAccessList denyAcl =
        IpAccessList.builder()
            .setName("foo")
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.rejecting().setMatchCondition(matchDstIp("1.1.1.1")).build(),
                    IpAccessListLine.accepting().setMatchCondition(matchDstIp("1.1.1.2")).build(),
                    IpAccessListLine.accepting().setMatchCondition(matchDstIp("1.1.1.3")).build(),
                    IpAccessListLine.rejecting().setMatchCondition(matchDstIp("1.1.1.4")).build(),
                    IpAccessListLine.ACCEPT_ALL))
            .build();
    assertThat(SearchFiltersAnswerer.toDenyAcl(_acl), equalTo(denyAcl));
  }
}
