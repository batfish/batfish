package org.batfish.representation.frr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

public class RouteMapSetOriginTest {

  @Test
  public void testGetOriginType() {
    RouteMapSetOrigin set = new RouteMapSetOrigin(OriginType.EGP);
    assertThat(set.getOriginType(), equalTo(OriginType.EGP));
  }

  @Test
  public void testToStatements() {
    RouteMapSetOrigin set = new RouteMapSetOrigin(OriginType.EGP);
    List<Statement> result =
        set.toStatements(null, null, null).collect(ImmutableList.toImmutableList());
    assertThat(
        result, equalTo(ImmutableList.of(new SetOrigin(new LiteralOrigin(OriginType.EGP, null)))));
  }
}
