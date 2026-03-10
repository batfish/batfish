package org.batfish.datamodel.acl;

import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.ExprAclLine.rejecting;
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.junit.Test;

/** Tests of {@link SourcesReferencedByIpAccessLists} */
public class SourcesReferencedByIpAccessListsTest {
  @Test
  public void testExprs() {
    Map<String, IpAccessList> namedAcls = ImmutableMap.of();

    assertThat(referencedSources(namedAcls, TRUE), empty());
    assertThat(referencedSources(namedAcls, FALSE), empty());
    assertThat(
        referencedSources(namedAcls, ORIGINATING_FROM_DEVICE),
        contains(SOURCE_ORIGINATING_FROM_DEVICE));
    assertThat(referencedSources(namedAcls, matchDst(Ip.AUTO)), empty());

    assertThat(
        referencedSources(namedAcls, matchSrcInterface("a", "b", "c")),
        containsInAnyOrder("a", "b", "c"));
    assertThat(
        referencedSources(namedAcls, and(matchSrcInterface("a"), matchSrcInterface("b", "c"))),
        containsInAnyOrder("a", "b", "c"));
    assertThat(
        referencedSources(namedAcls, not(matchSrcInterface("a", "b", "c"))),
        containsInAnyOrder("a", "b", "c"));
    assertThat(
        referencedSources(namedAcls, or(matchSrcInterface("a"), matchSrcInterface("b", "c"))),
        containsInAnyOrder("a", "b", "c"));
  }

  @Test
  public void testAcl() {
    IpAccessList.Builder aclBuilder = IpAccessList.builder().setName("foo");
    IpAccessList acl = aclBuilder.setLines(ImmutableList.of(ExprAclLine.ACCEPT_ALL)).build();
    Map<String, IpAccessList> namedAcls = ImmutableMap.of();
    assertThat(referencedSources(namedAcls, acl), empty());

    acl =
        aclBuilder
            .setLines(
                ImmutableList.of(
                    accepting().setMatchCondition(matchSrcInterface("a")).build(),
                    rejecting().setMatchCondition(matchSrcInterface("b")).build(),
                    accepting().setMatchCondition(matchSrcInterface("c")).build()))
            .build();
    assertThat(referencedSources(namedAcls, acl), containsInAnyOrder("a", "b", "c"));
  }

  @Test
  public void testDeniedByAcl() {
    IpAccessList acl =
        IpAccessList.builder()
            .setName("foo")
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting().setMatchCondition(matchSrcInterface("a")).build()))
            .build();
    Map<String, IpAccessList> namedAcls = ImmutableMap.of(acl.getName(), acl);
    assertThat(referencedSources(namedAcls, new DeniedByAcl(acl.getName())), contains("a"));
  }

  @Test
  public void testPermittedByAcl() {
    IpAccessList.Builder aclBuilder = IpAccessList.builder().setName("foo");
    IpAccessList acl =
        aclBuilder
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting().setMatchCondition(matchSrcInterface("a")).build()))
            .build();
    Map<String, IpAccessList> namedAcls = ImmutableMap.of(acl.getName(), acl);
    assertThat(referencedSources(namedAcls, new PermittedByAcl(acl.getName())), contains("a"));
  }

  @Test
  public void testReferencedSourcesForMultipleAcls() {
    IpAccessList.Builder aclBuilder = IpAccessList.builder();
    IpAccessList acl1 =
        aclBuilder
            .setName("acl1")
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting().setMatchCondition(matchSrcInterface("a")).build()))
            .build();
    IpAccessList acl2 =
        aclBuilder
            .setName("acl2")
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting().setMatchCondition(matchSrcInterface("b")).build()))
            .build();
    IpAccessList acl3 =
        aclBuilder
            .setName("acl3")
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting().setMatchCondition(matchSrcInterface("c")).build()))
            .build();

    Map<String, IpAccessList> namedAcls =
        ImmutableMap.of(acl1.getName(), acl1, acl2.getName(), acl2, acl3.getName(), acl3);
    assertThat(
        referencedSources(namedAcls, ImmutableSet.of(acl1.getName(), acl2.getName())),
        containsInAnyOrder("a", "b"));
  }
}
