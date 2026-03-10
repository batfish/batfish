package org.batfish.representation.frr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.statement.ExcludeAsPath;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

/** Test of {@link RouteMapSetExcludeAsPath} */
public class RouteMapSetExcludeAsPathTest {
  @Test
  public void testToStatements() {
    RouteMapSetExcludeAsPath set = new RouteMapSetExcludeAsPath(ImmutableList.of(1L, 2L, 3L));
    List<Statement> result =
        set.toStatements(null, null, null).collect(ImmutableList.toImmutableList());
    assertThat(
        result,
        contains(
            new ExcludeAsPath(
                new LiteralAsList(
                    ImmutableList.of(
                        new ExplicitAs(1L), new ExplicitAs(2L), new ExplicitAs(3L))))));
  }
}
