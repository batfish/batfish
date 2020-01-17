package org.batfish.representation.cumulus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

public class RouteMapSetMetricTest {

  @Test
  public void testGetMetric() {
    RouteMapSetMetric set = new RouteMapSetMetric(new LiteralLong(10));
    assertThat(set.getMetric(), equalTo(new LiteralLong(10)));
  }

  @Test
  public void testToStatements() {
    RouteMapSetMetric set = new RouteMapSetMetric(new LiteralLong(10));
    List<Statement> result =
        set.toStatements(null, null, null).collect(ImmutableList.toImmutableList());
    assertThat(result, equalTo(ImmutableList.of(new SetMetric(new LiteralLong(10L)))));
  }
}
