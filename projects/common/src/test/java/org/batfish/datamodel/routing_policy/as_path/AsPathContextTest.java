package org.batfish.datamodel.routing_policy.as_path;

import static org.batfish.datamodel.routing_policy.as_path.AsPathContext.fromEnvironment;
import static org.batfish.datamodel.routing_policy.expr.IntComparator.EQ;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link AsPathContext}. */
public final class AsPathContextTest {

  @Test
  public void testFromEnvironment() {
    Map<String, AsPathExpr> asPathExprs = ImmutableMap.of("ae", InputAsPath.instance());
    Map<String, AsPathMatchExpr> asPathMatchExprs =
        ImmutableMap.of("ame", HasAsPathLength.of(new IntComparison(EQ, new LiteralInt(1))));

    Configuration c = new Configuration("a", ConfigurationFormat.CISCO_IOS_XR);
    c.setAsPathExprs(asPathExprs);
    c.setAsPathMatchExprs(asPathMatchExprs);

    AsPath outputAsPath = AsPath.ofSingletonAsSets(5L);
    AsPath intermediateAsPath = AsPath.ofSingletonAsSets(6L);
    AsPath inputRouteAsPath = AsPath.ofSingletonAsSets(7L);

    AsPathContext useOutput =
        fromEnvironment(
                Environment.builder(c)
                    .setUseOutputAttributes(true)
                    .setOutputRoute(Bgpv4Route.testBuilder().setAsPath(outputAsPath))
                    .build())
            .get();

    // first check attributes from configuration
    assertThat(useOutput.getAsPathExprs(), equalTo(asPathExprs));
    assertThat(useOutput.getAsPathMatchExprs(), equalTo(asPathMatchExprs));

    // test of proper input as-path
    assertThat(useOutput.getInputAsPath(), equalTo(outputAsPath));

    AsPathContext useIntermediate =
        fromEnvironment(
                Environment.builder(c)
                    .setReadFromIntermediateBgpAttributes(true)
                    .setIntermediateBgpAttributes(
                        Bgpv4Route.testBuilder().setAsPath(intermediateAsPath))
                    .build())
            .get();
    assertThat(useIntermediate.getInputAsPath(), equalTo(intermediateAsPath));

    AsPathContext useInputRoute =
        fromEnvironment(
                Environment.builder(c)
                    .setOriginalRoute(
                        Bgpv4Route.testBuilder()
                            .setAsPath(inputRouteAsPath)
                            .setNetwork(Prefix.ZERO)
                            .build())
                    .build())
            .get();
    assertThat(useInputRoute.getInputAsPath(), equalTo(inputRouteAsPath));

    // if no as-path is available, no context should be returned
    assertThat(
        fromEnvironment(
            Environment.builder(c).setOriginalRoute(new ConnectedRoute(Prefix.ZERO, "i1")).build()),
        equalTo(Optional.empty()));
  }
}
