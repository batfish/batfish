package org.batfish.datamodel.visitors;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.MainRibRoutes;
import org.junit.Test;

@ParametersAreNonnullByDefault
public final class RoutesExprEvaluatorTest {

  @Test
  public void testVisitMainRibRoutesExpr() {
    ConnectedRoute route = new ConnectedRoute(Prefix.ZERO, "blah");
    Configuration c = new Configuration("a", ConfigurationFormat.CISCO_IOS);
    Environment env =
        Environment.builder(c).setMainRibRoutes(() -> ImmutableList.of(route)).build();
    assertThat(
        RoutesExprEvaluator.instance().visitMainRibRoutes(MainRibRoutes.instance(), env),
        contains(route));
  }
}
