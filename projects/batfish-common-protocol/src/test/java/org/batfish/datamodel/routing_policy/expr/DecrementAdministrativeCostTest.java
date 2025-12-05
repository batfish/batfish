package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.datamodel.AbstractRoute.MAX_ADMIN_DISTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.junit.Test;

public class DecrementAdministrativeCostTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new DecrementAdministrativeCost(5, 0), new DecrementAdministrativeCost(5, 0))
        .addEqualityGroup(new DecrementAdministrativeCost(6, 0))
        .addEqualityGroup(new DecrementAdministrativeCost(5, 10))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    DecrementAdministrativeCost obj = new DecrementAdministrativeCost(50, 0);
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
    assertThat(BatfishObjectMapper.clone(obj, AdministrativeCostExpr.class), equalTo(obj));
  }

  @Test
  public void testEvaluate() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    StaticRoute testRoute =
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(100).build();
    DecrementAdministrativeCost sub30 = new DecrementAdministrativeCost(30, 0);

    // Normal subtraction
    assertThat(
        sub30.evaluate(
            Environment.builder(c).setDirection(Direction.IN).setOriginalRoute(testRoute).build()),
        equalTo(70L));

    // Clips at 0
    StaticRoute lowCostRoute =
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(20).build();
    assertThat(
        sub30.evaluate(
            Environment.builder(c)
                .setDirection(Direction.IN)
                .setOriginalRoute(lowCostRoute)
                .build()),
        equalTo(0L));
  }

  @Test
  public void testEvaluate_customMin() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    StaticRoute testRoute =
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(100).build();
    DecrementAdministrativeCost sub30 = new DecrementAdministrativeCost(30, 50);

    // Normal subtraction above min
    assertThat(
        sub30.evaluate(
            Environment.builder(c).setDirection(Direction.IN).setOriginalRoute(testRoute).build()),
        equalTo(70L));

    // Clips at custom min (50)
    StaticRoute lowCostRoute =
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(60).build();
    assertThat(
        sub30.evaluate(
            Environment.builder(c)
                .setDirection(Direction.IN)
                .setOriginalRoute(lowCostRoute)
                .build()),
        equalTo(50L));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_negative() {
    new DecrementAdministrativeCost(-1, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_tooLarge() {
    new DecrementAdministrativeCost(MAX_ADMIN_DISTANCE + 1, 0);
  }
}
