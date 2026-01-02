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

public class DecrementLocalPreferenceTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(new DecrementLocalPreference(5), new DecrementLocalPreference(5))
        .addEqualityGroup(new DecrementLocalPreference(6))
        .testEquals();
  }

  @Test
  public void testEvaluate() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Bgpv4Route testRoute =
        Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO).setLocalPreference(0L).build();
    DecrementLocalPreference sub5 = new DecrementLocalPreference(5);
    // Clips to 0
    assertThat(
        sub5.evaluate(Environment.builder(c).setOriginalRoute(testRoute).build()), equalTo(0L));
    assertThat(
        sub5.evaluate(
            Environment.builder(c)
                .setOriginalRoute(testRoute.toBuilder().setLocalPreference(2).build())
                .build()),
        equalTo(0L));
    assertThat(
        sub5.evaluate(
            Environment.builder(c)
                .setOriginalRoute(testRoute.toBuilder().setLocalPreference(105).build())
                .build()),
        equalTo(100L));
    // Check operation near bounds
    assertThat(
        sub5.evaluate(
            Environment.builder(c)
                .setOriginalRoute(
                    testRoute.toBuilder().setLocalPreference(MAX_LOCAL_PREFERENCE).build())
                .build()),
        equalTo(MAX_LOCAL_PREFERENCE - 5));
  }
}
