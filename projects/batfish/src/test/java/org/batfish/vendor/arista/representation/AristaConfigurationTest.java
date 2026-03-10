package org.batfish.vendor.arista.representation;

import static org.batfish.vendor.arista.representation.AristaConfiguration.computeRouteMapEntryName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class AristaConfigurationTest {

  @Test
  public void testToStatement_traceable() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.ARISTA)
            .setHostname("c")
            .build();
    AristaConfiguration aristaConfiguration = new AristaConfiguration();
    aristaConfiguration.setFilename("file");

    String routeMapName = "routeMap";

    RouteMapClause entry = new RouteMapClause(LineAction.PERMIT, routeMapName, 100);
    RouteMapSetCommunity rmSetComm =
        new RouteMapSetCommunity(ImmutableList.of(StandardCommunity.BLACKHOLE), false);
    entry.setSetCommunity(rmSetComm);

    // what we expect from the entry above
    List<Statement> expectedTrueStatements = new LinkedList<>();
    rmSetComm.applyTo(expectedTrueStatements, aristaConfiguration, c, new Warnings());
    expectedTrueStatements.add(Statements.ReturnTrue.toStaticStatement());

    assertThat(
        aristaConfiguration.toStatement(
            c, routeMapName, entry, ImmutableMap.of(), ImmutableMap.of(), ImmutableSet.of()),
        equalTo(
            new If(
                new Conjunction(),
                ImmutableList.of(
                    new TraceableStatement(
                        TraceElement.builder()
                            .add("Matched ")
                            .add(
                                String.format("route-map %s sequence-number %d", routeMapName, 100),
                                new VendorStructureId(
                                    "file",
                                    AristaStructureType.ROUTE_MAP_ENTRY.getDescription(),
                                    computeRouteMapEntryName(routeMapName, 100)))
                            .build(),
                        expectedTrueStatements)))));
  }
}
