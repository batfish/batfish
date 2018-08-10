package org.batfish.datamodel.acl;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.IpAccessListLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.acl.InterfacesReferencedByIpAccessLists.referencedInterfaces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.junit.Test;

public class InterfacesReferencedByIpAccessListsTest {
  @Test
  public void testExprs() {
    assertThat(referencedInterfaces(TRUE), equalTo(ImmutableSet.of()));
    assertThat(referencedInterfaces(FALSE), equalTo(ImmutableSet.of()));
    assertThat(referencedInterfaces(ORIGINATING_FROM_DEVICE), equalTo(ImmutableSet.of()));
    assertThat(referencedInterfaces(matchDst(Ip.AUTO)), equalTo(ImmutableSet.of()));
    assertThat(referencedInterfaces(permittedByAcl("foo")), equalTo(ImmutableSet.of()));

    assertThat(
        referencedInterfaces(matchSrcInterface("a", "b", "c")),
        equalTo(ImmutableSet.of("a", "b", "c")));
    assertThat(
        referencedInterfaces(and(matchSrcInterface("a"), matchSrcInterface("b", "c"))),
        equalTo(ImmutableSet.of("a", "b", "c")));
    assertThat(
        referencedInterfaces(not(matchSrcInterface("a", "b", "c"))),
        equalTo(ImmutableSet.of("a", "b", "c")));
    assertThat(
        referencedInterfaces(or(matchSrcInterface("a"), matchSrcInterface("b", "c"))),
        equalTo(ImmutableSet.of("a", "b", "c")));
  }

  @Test
  public void testAcl() {
    IpAccessList.Builder aclBuilder = IpAccessList.builder().setName("foo");
    IpAccessList acl = aclBuilder.setLines(ImmutableList.of(IpAccessListLine.ACCEPT_ALL)).build();
    assertThat(referencedInterfaces(acl), equalTo(ImmutableSet.of()));

    acl =
        aclBuilder
            .setLines(
                ImmutableList.of(
                    accepting().setMatchCondition(matchSrcInterface("a")).build(),
                    rejecting().setMatchCondition(matchSrcInterface("b")).build(),
                    accepting().setMatchCondition(matchSrcInterface("c")).build()))
            .build();
    assertThat(referencedInterfaces(acl), equalTo(ImmutableSet.of("a", "b", "c")));
  }
}
