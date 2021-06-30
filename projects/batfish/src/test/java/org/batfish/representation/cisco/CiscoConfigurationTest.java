package org.batfish.representation.cisco;

import static org.batfish.representation.cisco.CiscoConfiguration.getRouteMapClausePolicyName;
import static org.batfish.representation.cisco.CiscoConfiguration.makeClauseTraceable;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.junit.Test;

public class CiscoConfigurationTest {

  /** Test that trace hints are added during conversion */
  @Test
  public void testToRoutingPolicy_trace() {
    CiscoConfiguration cc = new CiscoConfiguration();
    cc.setFilename("file");
    Configuration c =
        Configuration.builder()
            .setHostname("host")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();

    RouteMap map = new RouteMap("rm");
    RouteMapClause clause = new RouteMapClause(LineAction.DENY, map.getName(), 10);
    map.getClauses().put(10, clause);

    RoutingPolicy routingPolicy = cc.toRoutingPolicy(c, map);

    If ifStatement = (If) Iterables.getOnlyElement(routingPolicy.getStatements());
    TraceableStatement traceableStatement =
        (TraceableStatement) Iterables.getOnlyElement(ifStatement.getTrueStatements());
    assertThat(
        traceableStatement.getTraceElement(),
        equalTo(makeClauseTraceable(ImmutableList.of(), 10, "rm", "file").getTraceElement()));

    // false statements are not wrapped in traceable
    assertThat(
        ifStatement.getFalseStatements(), contains(not(instanceOf(TraceableStatement.class))));
  }

  /** Test that trace hints are added during conversion */
  @Test
  public void testToRoutingPolicies_trace() {
    CiscoConfiguration cc = new CiscoConfiguration();
    cc.setFilename("file");
    Configuration c =
        Configuration.builder()
            .setHostname("host")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();

    RouteMap map = new RouteMap("rm");
    RouteMapClause clause = new RouteMapClause(LineAction.DENY, map.getName(), 10);
    map.getClauses().put(10, clause);

    cc.toRoutingPolicies(c, map);
    RoutingPolicy routingPolicy = c.getRoutingPolicies().get(getRouteMapClausePolicyName(map, 10));

    assertNotNull(routingPolicy);

    If ifStatement = (If) Iterables.getOnlyElement(routingPolicy.getStatements());
    TraceableStatement traceableStatement =
        (TraceableStatement) Iterables.getOnlyElement(ifStatement.getTrueStatements());
    assertThat(
        traceableStatement.getTraceElement(),
        equalTo(makeClauseTraceable(ImmutableList.of(), 10, "rm", "file").getTraceElement()));

    assertThat(
        ifStatement.getFalseStatements(), contains(not(instanceOf(TraceableStatement.class))));
  }
}
