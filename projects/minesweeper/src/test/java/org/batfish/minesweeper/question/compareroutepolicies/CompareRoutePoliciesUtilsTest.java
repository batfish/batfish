package org.batfish.minesweeper.question.compareroutepolicies;

import static org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesUtils.findConcreteDifference;
import static org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesUtils.relevantAttributesFor;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.ADMINISTRATIVE_DISTANCE;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.AS_PATH;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.CLUSTER_LIST;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.COMMUNITIES;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.LOCAL_PREFERENCE;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.METRIC;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.NETWORK;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.NEXT_HOP;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.TAG;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.WEIGHT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.minesweeper.utils.Tuple;
import org.batfish.question.testroutepolicies.Result;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link
 * org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesUtils}.
 */
public class CompareRoutePoliciesUtilsTest {

  ConfigAtomicPredicates _configAPs;
  BDDFactory _factory;
  BDDRoute _bddRoute;
  Configuration _config;

  RoutingPolicy.Builder _policyBuilderRef;
  RoutingPolicy.Builder _policyBuilderOther;

  @Before
  public void setup() {
    _configAPs =
        new ConfigAtomicPredicates(
            ImmutableList.of(),
            ImmutableSet.of(CommunityVar.from("30:40")),
            ImmutableSet.of("^20$"));
    _factory = JFactory.init(100, 100);
    _bddRoute = new BDDRoute(_factory, _configAPs);

    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname("host")
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_CONCATENATED);
    _config = cb.build();

    _policyBuilderRef = nf.routingPolicyBuilder().setOwner(_config).setName("name");
    _policyBuilderOther = nf.routingPolicyBuilder().setOwner(_config).setName("name");
  }

  @Test
  public void testRelevantInputAttributesFor() {
    BDD none = _factory.one();
    BDD localPref = _bddRoute.getLocalPref().value(300);
    BDD commOrMed = _bddRoute.getCommunityAtomicPredicates()[0].or(_bddRoute.getMed().leq(10));
    BDD notASAndAD =
        _bddRoute
            .getAsPathRegexAtomicPredicates()
            .value(0)
            .not()
            .and(_bddRoute.getAdminDist().range(11, 13));
    BDD prefixLenAndWeight =
        _bddRoute.getPrefixLength().value(32).and(_bddRoute.getWeight().geq(4));
    Prefix p = Prefix.create(Ip.parse("1.1.1.1"), 32);
    BDD several =
        _bddRoute
            .getPrefix()
            .toBDD(p)
            .and(_bddRoute.getNextHop().toBDD(p))
            .or(_bddRoute.getClusterListLength().value(1))
            .and(_bddRoute.getTag().value(101).not());

    assertThat(relevantAttributesFor(none, _configAPs), equalTo(ImmutableList.of()));
    assertThat(
        relevantAttributesFor(localPref, _configAPs), equalTo(ImmutableList.of(LOCAL_PREFERENCE)));
    assertThat(
        relevantAttributesFor(commOrMed, _configAPs), containsInAnyOrder(COMMUNITIES, METRIC));
    assertThat(
        relevantAttributesFor(notASAndAD, _configAPs),
        containsInAnyOrder(AS_PATH, ADMINISTRATIVE_DISTANCE));
    assertThat(
        relevantAttributesFor(prefixLenAndWeight, _configAPs), containsInAnyOrder(NETWORK, WEIGHT));
    assertThat(
        relevantAttributesFor(several, _configAPs),
        containsInAnyOrder(NETWORK, NEXT_HOP, CLUSTER_LIST, TAG));
  }

  @Test
  public void testRelevantInputAttributesForDifferenceNone() {
    TransferReturn path = new TransferReturn(new BDDRoute(_bddRoute), _factory.one(), true);
    TransferReturn otherPath = new TransferReturn(new BDDRoute(_bddRoute), _factory.one(), false);
    Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>> res =
        findConcreteDifference(
            path,
            otherPath,
            _bddRoute.bgpWellFormednessConstraints(),
            _configAPs,
            _policyBuilderRef
                .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
                .build(),
            _policyBuilderOther
                .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
                .build(),
            Environment.Direction.IN);
    assertThat(res.getFirst().getRelevantInputAttributes(), equalTo(ImmutableList.of()));
    assertThat(res.getSecond().getRelevantInputAttributes(), equalTo(ImmutableList.of()));
  }

  @Test
  public void testRelevantInputAttributesForDifferenceTwo() {
    TransferReturn path =
        new TransferReturn(
            new BDDRoute(_bddRoute),
            _bddRoute.getLocalPref().value(300).or(_bddRoute.getMed().value(20)),
            true);
    TransferReturn otherPath = new TransferReturn(new BDDRoute(_bddRoute), _factory.one(), false);
    Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>> res =
        findConcreteDifference(
            path,
            otherPath,
            _bddRoute.bgpWellFormednessConstraints(),
            _configAPs,
            _policyBuilderRef
                .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
                .build(),
            _policyBuilderOther
                .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
                .build(),
            Environment.Direction.IN);
    assertThat(
        res.getFirst().getRelevantInputAttributes(), containsInAnyOrder(LOCAL_PREFERENCE, METRIC));
    assertThat(
        res.getSecond().getRelevantInputAttributes(), containsInAnyOrder(LOCAL_PREFERENCE, METRIC));
  }

  @Test
  public void testRelevantInputAttributesForDifferenceIntersect() {
    TransferReturn path =
        new TransferReturn(
            new BDDRoute(_bddRoute),
            _bddRoute.getLocalPref().value(300).or(_bddRoute.getMed().value(20)),
            true);
    TransferReturn otherPath =
        new TransferReturn(new BDDRoute(_bddRoute), _bddRoute.getLocalPref().value(300), false);
    Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>> res =
        findConcreteDifference(
            path,
            otherPath,
            _bddRoute.bgpWellFormednessConstraints(),
            _configAPs,
            _policyBuilderRef
                .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
                .build(),
            _policyBuilderOther
                .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
                .build(),
            Environment.Direction.IN);
    assertThat(res.getFirst().getRelevantInputAttributes(), containsInAnyOrder(LOCAL_PREFERENCE));
    assertThat(res.getSecond().getRelevantInputAttributes(), containsInAnyOrder(LOCAL_PREFERENCE));
  }

  @Test
  public void testRelevantInputAttributesForDifferenceOutput() {
    BDDRoute pathRoute = new BDDRoute(_bddRoute);
    pathRoute.getLocalPref().setValue(300);
    TransferReturn path = new TransferReturn(pathRoute, _factory.one(), true);
    TransferReturn otherPath = new TransferReturn(new BDDRoute(_bddRoute), _factory.one(), true);
    Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>> res =
        findConcreteDifference(
            path,
            otherPath,
            _bddRoute.bgpWellFormednessConstraints(),
            _configAPs,
            _policyBuilderRef
                .setStatements(
                    ImmutableList.of(
                        new SetLocalPreference(new LiteralLong(300)),
                        Statements.ExitAccept.toStaticStatement()))
                .build(),
            _policyBuilderOther
                .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
                .build(),
            Environment.Direction.IN);
    assertThat(res.getFirst().getRelevantInputAttributes(), containsInAnyOrder(LOCAL_PREFERENCE));
    assertThat(res.getSecond().getRelevantInputAttributes(), containsInAnyOrder(LOCAL_PREFERENCE));
  }
}
