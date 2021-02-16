package org.batfish.representation.cumulus_nclu;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

public class RouteMapSetWeightTest {

  @Test
  public void testGetWeight() {
    RouteMapSetWeight set = new RouteMapSetWeight(10);
    assertThat(set.getWeight(), equalTo(10));
  }

  @Test
  public void testToStatements() {
    RouteMapSetWeight set = new RouteMapSetWeight(10);
    List<Statement> result =
        set.toStatements(null, null, null).collect(ImmutableList.toImmutableList());
    assertThat(result, equalTo(ImmutableList.of(new SetWeight(new LiteralInt(10)))));
  }
}
