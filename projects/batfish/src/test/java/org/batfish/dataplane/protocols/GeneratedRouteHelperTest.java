package org.batfish.dataplane.protocols;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.GeneratedRoute.Builder;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link GeneratedRouteHelper}. */
public class GeneratedRouteHelperTest {

  private GeneratedRoute.Builder _builder;

  @Before
  public void setup() {
    _builder = new Builder();
  }

  @Test
  public void activateWhenPolicyIsNull() {
    GeneratedRoute gr = _builder.setNetwork(Prefix.ZERO).build();

    Builder newRoute =
        GeneratedRouteHelper.activateGeneratedRoute(gr, null, ImmutableSet.of(), "vrf");

    assertThat(newRoute, notNullValue());
  }

  @Test
  public void testDiscardIsHonored() {
    GeneratedRoute gr = _builder.setDiscard(true).setNetwork(Prefix.ZERO).build();

    GeneratedRouteHelper.activateGeneratedRoute(gr, null, ImmutableSet.of(), "vrf");

    assertThat(gr.getDiscard(), equalTo(true));
  }

  @Test
  public void doNotActivateWithoutPolicyMatch() {
    GeneratedRoute gr = _builder.setDiscard(true).setNetwork(Prefix.parse("1.1.1.0/24")).build();
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n1")
            .build();

    RoutingPolicy policy =
        nf.routingPolicyBuilder()
            .setName("no match")
            .setOwner(c)
            .setStatements(ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))
            .build();

    Builder newRoute =
        GeneratedRouteHelper.activateGeneratedRoute(gr, policy, ImmutableSet.of(), "vrf");
    assertThat(newRoute, nullValue());
  }

  @Test
  public void activateWithPolicyMatch() {
    GeneratedRoute gr = _builder.setDiscard(true).setNetwork(Prefix.parse("1.1.1.0/24")).build();
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n1")
            .build();

    RoutingPolicy policy =
        nf.routingPolicyBuilder()
            .setName("always match")
            .setOwner(c)
            .setStatements(ImmutableList.of(Statements.ReturnTrue.toStaticStatement()))
            .build();

    Builder newRoute =
        GeneratedRouteHelper.activateGeneratedRoute(
            gr,
            policy,
            ImmutableSet.of(new StaticRoute(Prefix.parse("2.2.2.2/32"), null, "eth0", 1, 0L, 1)),
            "vrf");

    assertThat(newRoute, notNullValue());
  }
}
