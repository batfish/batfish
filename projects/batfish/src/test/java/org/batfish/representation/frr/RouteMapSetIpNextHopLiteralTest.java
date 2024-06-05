package org.batfish.representation.frr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

public class RouteMapSetIpNextHopLiteralTest {

  private static final Ip IP = Ip.parse("10.0.0.1");

  @Test
  public void testGetNextHops() {
    RouteMapSetIpNextHopLiteral set = new RouteMapSetIpNextHopLiteral(IP);
    assertThat(set.getNextHop(), equalTo(IP));
  }

  @Test
  public void testToStatements() {
    RouteMapSetIpNextHopLiteral set = new RouteMapSetIpNextHopLiteral(IP);

    ImmutableList<Statement> result =
        set.toStatements(null, null, null).collect(ImmutableList.toImmutableList());
    assertThat(
        result, equalTo(ImmutableList.of(new SetNextHop(new IpNextHop(ImmutableList.of(IP))))));
  }
}
