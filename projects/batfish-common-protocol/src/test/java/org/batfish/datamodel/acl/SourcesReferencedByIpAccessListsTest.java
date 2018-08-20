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
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.referencedSources;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.junit.Test;

public class SourcesReferencedByIpAccessListsTest {
  @Test
  public void testExprs() {
    Map<String, IpAccessList> namedAcls = ImmutableMap.of();

    assertThat(referencedSources(namedAcls, TRUE), equalTo(ImmutableSet.of()));
    assertThat(referencedSources(namedAcls, FALSE), equalTo(ImmutableSet.of()));
    assertThat(
        referencedSources(namedAcls, ORIGINATING_FROM_DEVICE),
        equalTo(ImmutableSet.of(SOURCE_ORIGINATING_FROM_DEVICE)));
    assertThat(referencedSources(namedAcls, matchDst(Ip.AUTO)), equalTo(ImmutableSet.of()));

    assertThat(
        referencedSources(namedAcls, matchSrcInterface("a", "b", "c")),
        equalTo(ImmutableSet.of("a", "b", "c")));
    assertThat(
        referencedSources(namedAcls, and(matchSrcInterface("a"), matchSrcInterface("b", "c"))),
        equalTo(ImmutableSet.of("a", "b", "c")));
    assertThat(
        referencedSources(namedAcls, not(matchSrcInterface("a", "b", "c"))),
        equalTo(ImmutableSet.of("a", "b", "c")));
    assertThat(
        referencedSources(namedAcls, or(matchSrcInterface("a"), matchSrcInterface("b", "c"))),
        equalTo(ImmutableSet.of("a", "b", "c")));
  }

  @Test
  public void testAcl() {
    IpAccessList.Builder aclBuilder = IpAccessList.builder().setName("foo");
    IpAccessList acl = aclBuilder.setLines(ImmutableList.of(IpAccessListLine.ACCEPT_ALL)).build();
    Map<String, IpAccessList> namedAcls = ImmutableMap.of();
    assertThat(referencedSources(namedAcls, acl), equalTo(ImmutableSet.of()));

    acl =
        aclBuilder
            .setLines(
                ImmutableList.of(
                    accepting().setMatchCondition(matchSrcInterface("a")).build(),
                    rejecting().setMatchCondition(matchSrcInterface("b")).build(),
                    accepting().setMatchCondition(matchSrcInterface("c")).build()))
            .build();
    assertThat(referencedSources(namedAcls, acl), equalTo(ImmutableSet.of("a", "b", "c")));
  }

  @Test
  public void testPermittedByAcl() {
    IpAccessList.Builder aclBuilder = IpAccessList.builder().setName("foo");
    IpAccessList acl =
        aclBuilder
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting().setMatchCondition(matchSrcInterface("a")).build()))
            .build();
    Map<String, IpAccessList> namedAcls = ImmutableMap.of(acl.getName(), acl);
    assertThat(
        referencedSources(namedAcls, new PermittedByAcl(acl.getName())),
        equalTo(ImmutableSet.of("a")));
  }
}
