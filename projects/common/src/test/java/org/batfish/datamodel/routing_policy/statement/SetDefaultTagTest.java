package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasTag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.junit.Test;

/** Tests of {@link SetDefaultTag} */
public class SetDefaultTagTest {
  private static final Configuration C =
      new NetworkFactory()
          .configurationBuilder()
          .setConfigurationFormat(ConfigurationFormat.CISCO_NX)
          .build();

  @Test
  public void testEquals() {
    SetDefaultTag expr = new SetDefaultTag(new LiteralLong(1L));
    new EqualsTester()
        .addEqualityGroup(expr, expr, new SetDefaultTag(new LiteralLong(1L)))
        .addEqualityGroup(new SetDefaultTag(new LiteralLong(2L)))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testSerialization() {
    SetDefaultTag expr = new SetDefaultTag(new LiteralLong(1L));
    assertEquals(SerializationUtils.clone(expr), expr);
    assertEquals(BatfishObjectMapper.clone(expr, SetDefaultTag.class), expr);
  }

  @Test
  public void testExecute() {
    SetDefaultTag expr = new SetDefaultTag(new LiteralLong(1L));
    {
      // No tag explicitly set; default tag should take effect
      Bgpv4Route.Builder r = Bgpv4Route.testBuilder().setNetwork(Prefix.parse("1.1.1.1/32"));
      Environment env = Environment.builder(C).setOutputRoute(r).build();
      expr.execute(env);
      assertThat(r.build(), hasTag(1L));
    }
    {
      // First set tag, then set default tag; default tag should not take effect
      Bgpv4Route.Builder r = Bgpv4Route.testBuilder().setNetwork(Prefix.parse("1.1.1.1/32"));
      Environment env = Environment.builder(C).setOutputRoute(r).build();
      new SetTag(new LiteralLong(5L)).execute(env);
      expr.execute(env);
      assertThat(r.build(), hasTag(5L));
    }
  }
}
