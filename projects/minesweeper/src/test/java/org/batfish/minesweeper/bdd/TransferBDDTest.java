package org.batfish.minesweeper.bdd;

import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantFor;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import net.sf.javabdd.BDD;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.TransferResult;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link TransferBDD}. */
public class TransferBDDTest {

  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private RoutingPolicy.Builder _policyBuilder;
  private IBatfish _batfish;
  private Configuration _baseConfig;
  private Graph _g;
  private PolicyQuotient _pq;
  private BDDRoute _anyRoute;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
    _policyBuilder = nf.routingPolicyBuilder().setOwner(_baseConfig).setName(POLICY_NAME);

    _batfish =
        new IBatfishTestAdapter() {
          @Override
          public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
            return ImmutableSortedMap.of(HOSTNAME, _baseConfig);
          }

          public TopologyProvider getTopologyProvider() {
            return new TopologyProviderTestAdapter(_batfish) {
              @Override
              public Topology getInitialLayer3Topology(NetworkSnapshot networkSnapshot) {
                return Topology.EMPTY;
              }
            };
          }
        };

    _g = new Graph(_batfish, null);
    _pq = new PolicyQuotient(_g);
    _anyRoute = new BDDRoute(ImmutableSet.of());
  }

  @Test
  public void testCaptureAll() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitAccept)).build();

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferResult<TransferReturn, BDD> result = tbdd.compute(ImmutableSet.of());
    BDD policyBDD = result.getReturnValue().getSecond();
    BDD fallThrough = result.getFallthroughValue();
    BDD returnAssigned = result.getReturnAssignedValue();

    // the policy is applicable to all announcements
    assertTrue(policyBDD.isOne());
  }

  @Test
  public void testCaptureNone() {
    RoutingPolicy policy =
        _policyBuilder.addStatement(new StaticStatement(Statements.ExitReject)).build();

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferResult<TransferReturn, BDD> result = tbdd.compute(ImmutableSet.of());
    BDD policyBDD = result.getReturnValue().getSecond();
    BDD fallThrough = result.getFallthroughValue();
    BDD returnAssigned = result.getReturnAssignedValue();

    // the policy is applicable to no announcements
    assertTrue(policyBDD.isZero());
  }

  @Test
  public void testPrefixRange() {
    _policyBuilder.addStatement(
        new If(
            new MatchPrefixSet(
                DestinationNetwork.instance(),
                new ExplicitPrefixSet(
                    new PrefixSpace(
                        ImmutableList.of(
                            new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)))))),
            ImmutableList.of(new StaticStatement(Statements.ExitAccept))));
    RoutingPolicy policy = _policyBuilder.build();

    TransferBDD tbdd = new TransferBDD(_g, _baseConfig, policy.getStatements(), _pq);
    TransferResult<TransferReturn, BDD> result = tbdd.compute(ImmutableSet.of());
    BDD policyBDD = result.getReturnValue().getSecond();
    BDD fallThrough = result.getFallthroughValue();
    BDD returnAssigned = result.getReturnAssignedValue();

    BDD expectedBDD =
        isRelevantFor(_anyRoute, new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24)));
    assertTrue(policyBDD.equals(expectedBDD));
  }

  @Test
  public void testIsRelevantFor() {
    BDDRoute bddRoute = new BDDRoute(ImmutableSet.of());
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(bddRoute.getPrefix());

    PrefixRange range = new PrefixRange(Prefix.parse("1.0.0.0/8"), new SubRange(16, 24));
    BDD rangeBdd = isRelevantFor(bddRoute, range);
    BDD notRangeBdd = rangeBdd.not();

    BDD matchingPrefix = ipSpaceToBDD.toBDD(Ip.parse("1.1.1.1"));
    BDD nonMatchingPrefix = ipSpaceToBDD.toBDD(Ip.parse("2.2.2.2"));
    BDD len24 = bddRoute.getPrefixLength().value(24);
    BDD len8 = bddRoute.getPrefixLength().value(8);
    BDD len32 = bddRoute.getPrefixLength().value(32);

    assertTrue(matchingPrefix.and(len24).imp(rangeBdd).isOne());
    assertTrue(matchingPrefix.and(len32).imp(notRangeBdd).isOne()); // prefix too long
    assertTrue(matchingPrefix.and(len8).imp(notRangeBdd).isOne()); // prefix too short
    assertTrue(nonMatchingPrefix.and(len24).imp(notRangeBdd).isOne()); // prefix doesn't match
  }

  @Test
  public void testIsRelevantFor_range32() {
    BDDRoute bddRoute = new BDDRoute(ImmutableSet.of());

    PrefixRange range = new PrefixRange(Prefix.parse("0.0.0.0/0"), SubRange.singleton(32));
    BDD rangeBdd = isRelevantFor(bddRoute, range);
    BDD len0 = bddRoute.getPrefixLength().value(0);
    BDD len32 = bddRoute.getPrefixLength().value(32);

    assertTrue(len0.imp(rangeBdd.not()).isOne());
    assertTrue(len32.imp(rangeBdd).isOne());
  }
}
