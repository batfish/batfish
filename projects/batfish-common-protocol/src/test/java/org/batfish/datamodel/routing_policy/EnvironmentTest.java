package org.batfish.datamodel.routing_policy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Test of {@link Environment}. */
@ParametersAreNonnullByDefault
public final class EnvironmentTest {

  @Test
  public void testWithAlternateRoute() {
    ConnectedRoute orig = new ConnectedRoute(Prefix.ZERO, "orig");
    ConnectedRoute alt = new ConnectedRoute(Prefix.ZERO, "alt");
    Configuration c = new Configuration("a", ConfigurationFormat.CISCO_IOS);
    Environment env = Environment.builder(c).setOriginalRoute(orig).build();

    // sanity check that original route is returned
    assertThat(env.getOriginalRoute(), equalTo(orig));

    // alternate route should be returned
    assertThat(env.withAlternateRoute(alt, env::getOriginalRoute), equalTo(alt));

    // make sure the original route is returned afterward
    assertThat(env.getOriginalRoute(), equalTo(orig));
  }
}
