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

public class IncrementAdministrativeCostTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IncrementAdministrativeCost(5, MAX_ADMIN_DISTANCE),
            new IncrementAdministrativeCost(5, MAX_ADMIN_DISTANCE))
        .addEqualityGroup(new IncrementAdministrativeCost(6, MAX_ADMIN_DISTANCE))
        .addEqualityGroup(new IncrementAdministrativeCost(5, 255))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    IncrementAdministrativeCost obj = new IncrementAdministrativeCost(50, MAX_ADMIN_DISTANCE);
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
    IncrementAdministrativeCost add50 = new IncrementAdministrativeCost(50, MAX_ADMIN_DISTANCE);

    // Normal addition
    assertThat(
        add50.evaluate(
            Environment.builder(c).setDirection(Direction.IN).setOriginalRoute(testRoute).build()),
        equalTo(150L));

    // Clips at MAX_ADMIN_DISTANCE
    StaticRoute highCostRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(MAX_ADMIN_DISTANCE - 40)
            .build();
    assertThat(
        add50.evaluate(
            Environment.builder(c)
                .setDirection(Direction.IN)
                .setOriginalRoute(highCostRoute)
                .build()),
        equalTo(MAX_ADMIN_DISTANCE));
  }

  @Test
  public void testEvaluate_customMax() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    StaticRoute testRoute =
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(200).build();
    IncrementAdministrativeCost add50 = new IncrementAdministrativeCost(50, 255);

    // Normal addition under max
    assertThat(
        add50.evaluate(
            Environment.builder(c).setDirection(Direction.IN).setOriginalRoute(testRoute).build()),
        equalTo(250L));

    // Clips at custom max (255)
    StaticRoute highCostRoute =
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(220).build();
    assertThat(
        add50.evaluate(
            Environment.builder(c)
                .setDirection(Direction.IN)
                .setOriginalRoute(highCostRoute)
                .build()),
        equalTo(255L));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_negative() {
    new IncrementAdministrativeCost(-1, MAX_ADMIN_DISTANCE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_tooLarge() {
    new IncrementAdministrativeCost(MAX_ADMIN_DISTANCE + 1, MAX_ADMIN_DISTANCE);
  }
}
