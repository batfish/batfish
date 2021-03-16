package org.batfish.dataplane.protocols;

import static org.batfish.datamodel.OriginType.IGP;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasAsPath;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasCommunities;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasLocalPreference;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasOriginType;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasWeight;
import static org.batfish.dataplane.ibdp.TestUtils.annotateRoute;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.GeneratedRoute.Builder;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
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

    Builder newRoute = GeneratedRouteHelper.activateGeneratedRoute(gr, null, ImmutableSet.of());

    assertThat(newRoute, notNullValue());
  }

  @Test
  public void testDiscardIsHonored() {
    GeneratedRoute gr = _builder.setDiscard(true).setNetwork(Prefix.ZERO).build();

    GeneratedRouteHelper.activateGeneratedRoute(gr, null, ImmutableSet.of());

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

    Builder newRoute = GeneratedRouteHelper.activateGeneratedRoute(gr, policy, ImmutableSet.of());
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
    nf.vrfBuilder().setOwner(c).build();

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
                    StaticRoute.testBuilder()
                        .setNetwork(Prefix.parse("2.2.2.2/32"))
                        .setNextHopIp(null)
                        .setNextHopInterface("eth0")
                        .setAdministrativeCost(1)
                        .setMetric(0L)
                        .setTag(1L)
                        .build())));

    assertThat(newRoute, notNullValue());
  }

  @Test
  public void testActivateAndSetBgpProperties() {
    GeneratedRoute gr = _builder.setDiscard(true).setNetwork(Prefix.parse("1.1.1.0/24")).build();
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n1")
            .build();
    nf.vrfBuilder().setOwner(c).build();

    GeneratedRoute otherRoute =
        _builder
            .setNextHopIp(Ip.parse("5.5.5.5"))
            .setDiscard(false)
            .setNetwork(Prefix.parse("7.7.7.0/24"))
            .build();

    RoutingPolicy policy =
        nf.routingPolicyBuilder()
            .setName("always match")
            .setOwner(c)
            .setStatements(
                ImmutableList.of(
                    new SetCommunities(
                        new LiteralCommunitySet(CommunitySet.of(StandardCommunity.of(2L)))),
                    new PrependAsPath(
                        new LiteralAsList(
                            ImmutableList.of(new ExplicitAs(1L), new ExplicitAs(65100L)))),
                    new SetOrigin(new LiteralOrigin(IGP, null)),
                    Statements.RemovePrivateAs.toStaticStatement(),
                    new SetLocalPreference(new LiteralLong(123L)),
                    new SetWeight(new LiteralInt(456)),
                    Statements.ReturnTrue.toStaticStatement()))
            .build();

    Builder newRoute =
        GeneratedRouteHelper.activateGeneratedRoute(
            gr, policy, ImmutableSet.of(annotateRoute(otherRoute)));

    assertThat(
        newRoute.build(),
        allOf(
            hasAsPath(equalTo(AsPath.of(AsSet.of(1L)))),
            hasCommunities(StandardCommunity.of(2L)),
            hasOriginType(IGP),
            hasLocalPreference(123L),
            hasWeight(456)));
  }
}
