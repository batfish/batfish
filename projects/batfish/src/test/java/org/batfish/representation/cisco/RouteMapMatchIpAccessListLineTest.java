package org.batfish.representation.cisco;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.junit.Test;

/** Tests of {@link RouteMapMatchIpAccessListLine}. */
public class RouteMapMatchIpAccessListLineTest {

  /**
   * Helper method to test {@link RouteMapMatchIpAccessListLine#toBooleanExpr} with specified
   * defined route filter lists, avoiding the need to construct full Configuration objects.
   */
  private static BooleanExpr toBooleanExprHelper(
      Set<String> aclNames, Set<String> definedRouteFilterLists) {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("test")
            .build();

    // Add only the specified route filter lists to the configuration
    for (String name : definedRouteFilterLists) {
      RouteFilterList rfl = new RouteFilterList(name);
      c.getRouteFilterLists().put(name, rfl);
    }

    RouteMapMatchIpAccessListLine line = new RouteMapMatchIpAccessListLine(aclNames);
    return line.toBooleanExpr(c, new CiscoConfiguration(), new Warnings());
  }

  @Test
  public void testToBooleanExpr_allUndefined() {
    // All ACLs undefined - should return TRUE
    BooleanExpr result =
        toBooleanExprHelper(ImmutableSet.of("UNDEFINED1", "UNDEFINED2"), ImmutableSet.of());
    assertThat(result, equalTo(BooleanExprs.TRUE));
  }

  @Test
  public void testToBooleanExpr_singleUndefined() {
    // Single undefined ACL - should return TRUE
    BooleanExpr result = toBooleanExprHelper(ImmutableSet.of("UNDEFINED"), ImmutableSet.of());
    assertThat(result, equalTo(BooleanExprs.TRUE));
  }

  @Test
  public void testToBooleanExpr_singleDefined() {
    // Single defined ACL - should return MatchPrefixSet for that ACL
    BooleanExpr result =
        toBooleanExprHelper(ImmutableSet.of("DEFINED"), ImmutableSet.of("DEFINED"));
    BooleanExpr expected =
        new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet("DEFINED"));
    assertThat(result, equalTo(expected));
  }

  @Test
  public void testToBooleanExpr_multipleDefined() {
    // Multiple defined ACLs - should return Disjunction
    BooleanExpr result =
        toBooleanExprHelper(ImmutableSet.of("ACL1", "ACL2"), ImmutableSet.of("ACL1", "ACL2"));
    BooleanExpr expected =
        new Disjunction(
            ImmutableList.of(
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet("ACL1")),
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet("ACL2"))));
    assertThat(result, equalTo(expected));
  }

  @Test
  public void testToBooleanExpr_mixedDefinedUndefined() {
    // Mixed defined and undefined ACLs - should return Disjunction of only defined ACLs
    BooleanExpr result =
        toBooleanExprHelper(
            ImmutableSet.of("DEFINED1", "UNDEFINED", "DEFINED2"),
            ImmutableSet.of("DEFINED1", "DEFINED2"));
    BooleanExpr expected =
        new Disjunction(
            ImmutableList.of(
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet("DEFINED1")),
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet("DEFINED2"))));
    assertThat(result, equalTo(expected));
  }

  @Test
  public void testToBooleanExpr_mixedSingleDefined() {
    // Mixed with only one defined ACL - should return single MatchPrefixSet
    BooleanExpr result =
        toBooleanExprHelper(
            ImmutableSet.of("DEFINED", "UNDEFINED1", "UNDEFINED2"), ImmutableSet.of("DEFINED"));
    BooleanExpr expected =
        new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet("DEFINED"));
    assertThat(result, equalTo(expected));
  }

  @Test
  public void testToBooleanExpr_emptyList() {
    // Empty ACL list - should return TRUE
    BooleanExpr result = toBooleanExprHelper(ImmutableSet.of(), ImmutableSet.of());
    assertThat(result, equalTo(BooleanExprs.TRUE));
  }

  @Test
  public void testGetListNames() {
    Set<String> aclNames = ImmutableSet.of("ACL1", "ACL2");
    RouteMapMatchIpAccessListLine line = new RouteMapMatchIpAccessListLine(aclNames);
    assertThat(line.getListNames(), equalTo(aclNames));
  }
}
