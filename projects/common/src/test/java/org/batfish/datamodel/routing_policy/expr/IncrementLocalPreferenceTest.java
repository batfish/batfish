package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.datamodel.BgpRoute.MAX_LOCAL_PREFERENCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

public class IncrementLocalPreferenceTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(new IncrementLocalPreference(5), new IncrementLocalPreference(5))
        .addEqualityGroup(new IncrementLocalPreference(6))
        .testEquals();
  }

  @Test
  public void testEvaluate() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Bgpv4Route testRoute = Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO).build();
    IncrementLocalPreference add5 = new IncrementLocalPreference(5);
    assertThat(
        add5.evaluate(Environment.builder(c).setOriginalRoute(testRoute).build()), equalTo(5L));
    assertThat(
        add5.evaluate(
            Environment.builder(c)
                .setOriginalRoute(testRoute.toBuilder().setLocalPreference(105).build())
                .build()),
        equalTo(110L));
    // Clips MAX to MAX
    assertThat(
        add5.evaluate(
            Environment.builder(c)
                .setOriginalRoute(
                    testRoute.toBuilder().setLocalPreference(MAX_LOCAL_PREFERENCE - 4).build())
                .build()),
        equalTo(MAX_LOCAL_PREFERENCE));
    assertThat(
        add5.evaluate(
            Environment.builder(c)
                .setOriginalRoute(
                    testRoute.toBuilder().setLocalPreference(MAX_LOCAL_PREFERENCE).build())
                .build()),
        equalTo(MAX_LOCAL_PREFERENCE));
  }
}
