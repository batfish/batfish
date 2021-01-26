package org.batfish.representation.cisco;

import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.representation.cisco.CiscoIosNatUtil.toMatchExpr;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.junit.Test;

public class CiscoIosNatUtilTest {

  @Test
  public void testToMatchExpr_inconvertibleRouteMaps() {
    // Empty route-map should not convert
    {
      Warnings w = new Warnings(true, true, true);
      assertFalse(toMatchExpr(new RouteMap("rm"), ImmutableSet.of(), w).isPresent());
      assertThat(
          Iterables.getOnlyElement(w.getRedFlagWarnings()).getText(),
          containsString("empty route-map"));
    }

    // We will assume there is one ACL defined, called acl1
    Set<String> definedAcls = ImmutableSet.of("acl1");
    RouteMapMatchLine acl1MatchLine = new RouteMapMatchIpAccessListLine(ImmutableSet.of("acl1"));
    RouteMapClause acl1Clause = new RouteMapClause(LineAction.PERMIT, "acl1", 5);
    acl1Clause.addMatchLine(acl1MatchLine);

    // Sanity check: make sure a route-map with only acl1Clause does convert successfully
    assertTrue(
        toMatchExpr(rmWithClauses(acl1Clause), definedAcls, new Warnings(true, true, true))
            .isPresent());

    // In these tests, acl1Clause is included in all route maps to confirm that conversion will fail
    // if *any* clauses are unsupported rather than *all*.
    // Similarly, clauses that aren't supported because they contain an unsupported match line will
    // also contain a supported match line.
    {
      // Clause is empty
      Warnings w = new Warnings(true, true, true);
      assertFalse(
          toMatchExpr(rmWithClauses(acl1Clause, rmcWithMatchLines()), definedAcls, w).isPresent());
      assertThat(
          Iterables.getOnlyElement(w.getRedFlagWarnings()).getText(),
          containsString("clauses without match lines"));
    }
    {
      // Clause denies
      RouteMapClause rmc = new RouteMapClause(LineAction.DENY, "rmc", 10);
      rmc.addMatchLine(acl1MatchLine); // inconvertible bc it denies, not bc no match lines
      Warnings w = new Warnings(true, true, true);
      assertFalse(toMatchExpr(rmWithClauses(acl1Clause, rmc), definedAcls, w).isPresent());
      assertThat(
          Iterables.getOnlyElement(w.getRedFlagWarnings()).getText(),
          containsString("deny clause"));
    }
    {
      // Clause includes a set line
      RouteMapClause rmc = rmcWithMatchLines(acl1MatchLine);
      rmc.addSetLine(new RouteMapSetCommunityLine(ImmutableList.of(1L)));
      Warnings w = new Warnings(true, true, true);
      assertFalse(toMatchExpr(rmWithClauses(acl1Clause, rmc), definedAcls, w).isPresent());
      assertThat(
          Iterables.getOnlyElement(w.getRedFlagWarnings()).getText(), containsString("set line"));
    }
    {
      // Clause references undefined ACL acl2
      RouteMapClause rmc = rmcWithMatchLines(acl1MatchLine);
      rmc.addMatchLine(new RouteMapMatchIpAccessListLine(ImmutableSet.of("acl2")));
      Warnings w = new Warnings(true, true, true);
      assertFalse(toMatchExpr(rmWithClauses(acl1Clause, rmc), definedAcls, w).isPresent());
      assertThat(
          Iterables.getOnlyElement(w.getRedFlagWarnings()).getText(),
          equalTo(
              "Ignoring NAT rule with route-map rm 10: route-map references undefined access-lists [acl2]"));
    }
    {
      // Clause references defined ACL acl1 but also undefined ACL acl2
      RouteMapClause rmc = rmcWithMatchLines(acl1MatchLine);
      rmc.addMatchLine(new RouteMapMatchIpAccessListLine(ImmutableSet.of("acl1", "acl2")));
      Warnings w = new Warnings(true, true, true);
      assertFalse(toMatchExpr(rmWithClauses(acl1Clause, rmc), definedAcls, w).isPresent());
      assertThat(
          Iterables.getOnlyElement(w.getRedFlagWarnings()).getText(),
          equalTo(
              "Ignoring NAT rule with route-map rm 10: route-map references undefined access-lists [acl2]"));
    }
    {
      // Clause uses an unsupported type of match line
      RouteMapClause rmc = rmcWithMatchLines(acl1MatchLine);
      rmc.addMatchLine(new RouteMapMatchIpPrefixListLine(ImmutableSet.of("pl")));
      Warnings w = new Warnings(true, true, true);
      assertFalse(toMatchExpr(rmWithClauses(acl1Clause, rmc), definedAcls, w).isPresent());
      assertThat(
          Iterables.getOnlyElement(w.getRedFlagWarnings()).getText(),
          equalTo(
              "Ignoring NAT rule with route-map rm 10: match ip address prefix-list not supported in this context"));
    }
  }

  @Test
  public void testToMatchExpr() {
    Set<String> definedAcls = ImmutableSet.of("acl1", "acl2");
    RouteMapMatchLine acl1MatchLine = new RouteMapMatchIpAccessListLine(ImmutableSet.of("acl1"));
    RouteMapMatchLine acl2MatchLine = new RouteMapMatchIpAccessListLine(ImmutableSet.of("acl2"));
    RouteMapMatchLine acl1And2MatchLine =
        new RouteMapMatchIpAccessListLine(ImmutableSet.of("acl1", "acl2"));

    {
      RouteMap rm = rmWithClauses(rmcWithMatchLines(acl1MatchLine));
      assertThat(
          toMatchExpr(rm, definedAcls, new Warnings(true, true, true)).get(),
          equalTo(permittedByAcl("acl1")));
    }
    {
      // need to create second clause manually so it has a different sequence number
      RouteMapClause matchAcl2 = new RouteMapClause(LineAction.PERMIT, "rmc2", 20);
      matchAcl2.addMatchLine(acl2MatchLine);
      RouteMap rm = rmWithClauses(rmcWithMatchLines(acl1MatchLine), matchAcl2);
      OrMatchExpr matchExpr =
          (OrMatchExpr) toMatchExpr(rm, definedAcls, new Warnings(true, true, true)).get();
      assertThat(
          matchExpr.getDisjuncts(),
          containsInAnyOrder(permittedByAcl("acl1"), permittedByAcl("acl2")));
    }
    {
      RouteMap rm = rmWithClauses(rmcWithMatchLines(acl1And2MatchLine));
      OrMatchExpr matchExpr =
          (OrMatchExpr) toMatchExpr(rm, definedAcls, new Warnings(true, true, true)).get();
      assertThat(
          matchExpr.getDisjuncts(),
          containsInAnyOrder(permittedByAcl("acl1"), permittedByAcl("acl2")));
    }
  }

  /** Creates a permitting {@link RouteMapClause} with the given match lines and seq number 10. */
  private static RouteMapClause rmcWithMatchLines(RouteMapMatchLine... lines) {
    RouteMapClause rmc = new RouteMapClause(LineAction.PERMIT, "rmc", 10);
    for (RouteMapMatchLine line : lines) {
      rmc.addMatchLine(line);
    }
    return rmc;
  }

  /** Creates a {@link RouteMap} with the given clauses. */
  private static RouteMap rmWithClauses(RouteMapClause... clauses) {
    RouteMap rm = new RouteMap("rm");
    for (RouteMapClause clause : clauses) {
      assert !rm.getClauses().containsKey(clause.getSeqNum()); // clauses must have unique seq nums
      rm.getClauses().put(clause.getSeqNum(), clause);
    }
    return rm;
  }
}
