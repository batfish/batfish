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

public class LiteralAdministrativeCostTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new LiteralAdministrativeCost(5), new LiteralAdministrativeCost(5))
        .addEqualityGroup(new LiteralAdministrativeCost(6))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    LiteralAdministrativeCost obj = new LiteralAdministrativeCost(123);
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
        StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setAdministrativeCost(1).build();
    LiteralAdministrativeCost lit = new LiteralAdministrativeCost(123);
    assertThat(
        lit.evaluate(
            Environment.builder(c).setDirection(Direction.IN).setOriginalRoute(testRoute).build()),
        equalTo(123L));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_negative() {
    new LiteralAdministrativeCost(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_tooLarge() {
    new LiteralAdministrativeCost(MAX_ADMIN_DISTANCE + 1);
  }
}
