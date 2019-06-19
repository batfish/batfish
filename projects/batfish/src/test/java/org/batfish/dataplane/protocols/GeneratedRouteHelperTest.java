package org.batfish.dataplane.protocols;

import static org.batfish.dataplane.ibdp.TestUtils.annotateRoute;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.GeneratedRoute.Builder;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link GeneratedRouteHelper}. */
public class GeneratedRouteHelperTest {

  private GeneratedRoute.Builder _builder;

  @Before
  public void setup() {
    _builder = GeneratedRoute.builder();
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
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();

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
            ImmutableSet.of(
                annotateRoute(
                    StaticRoute.builder()
                        .setNetwork(Prefix.parse("2.2.2.2/32"))
                        .setNextHopIp(null)
                        .setNextHopInterface("eth0")
                        .setAdministrativeCost(1)
                        .setMetric(0L)
                        .setTag(1)
                        .build())),
            vrf.getName());

    assertThat(newRoute, notNullValue());
  }
}
