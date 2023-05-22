package org.batfish.minesweeper;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Before;
import org.junit.Test;

/** Tests for the {@link ConfigAtomicPredicates} class. */
public class ConfigAtomicPredicatesTest {
  private static final String HOSTNAME = "hostname";
  private IBatfish _batfish;
  private Configuration _baseConfig;

  private NetworkFactory _nf;

  static final class MockBatfish extends IBatfishTestAdapter {
    private final SortedMap<String, Configuration> _baseConfigs;

    MockBatfish(SortedMap<String, Configuration> baseConfigs) {
      _baseConfigs = ImmutableSortedMap.copyOf(baseConfigs);
    }

    @Override
    public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
      if (getSnapshot().equals(snapshot)) {
        return _baseConfigs;
      }
      throw new IllegalArgumentException("Unknown snapshot: " + snapshot);
    }

    @Override
    public TopologyProvider getTopologyProvider() {
      return new TopologyProviderTestAdapter(this) {
        @Override
        public Topology getInitialLayer3Topology(NetworkSnapshot networkSnapshot) {
          return Topology.EMPTY;
        }
      };
    }

    @Override
    public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot networkSnapshot) {
      return ImmutableMap.of();
    }
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    Configuration.Builder cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    _nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _batfish = new MockBatfish(ImmutableSortedMap.of(HOSTNAME, _baseConfig));
  }

  @Test
  public void testConstructor1() {
    ConfigAtomicPredicates cap =
        new ConfigAtomicPredicates(_batfish, _batfish.getSnapshot(), HOSTNAME);

    RegexAtomicPredicates<CommunityVar> commAPs = cap.getStandardCommunityAtomicPredicates();
    RegexAtomicPredicates<SymbolicAsPathRegex> asAPs = cap.getAsPathRegexAtomicPredicates();

    assertEquals(commAPs.getAtomicPredicateAutomata().size(), 1);
    assertThat(
        commAPs.getAtomicPredicateAutomata().values(),
        hasItem(CommunityVar.ALL_STANDARD_COMMUNITIES.toAutomaton()));

    assertEquals(asAPs.getAtomicPredicateAutomata().size(), 1);
    assertThat(
        asAPs.getAtomicPredicateAutomata().values(),
        hasItem(SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton()));

    assertEquals(cap.getTracks(), ImmutableList.of());
  }

  @Test
  public void testConstructor2Null() {
    ConfigAtomicPredicates cap =
        new ConfigAtomicPredicates(_batfish, _batfish.getSnapshot(), HOSTNAME, null, null);

    RegexAtomicPredicates<CommunityVar> commAPs = cap.getStandardCommunityAtomicPredicates();
    RegexAtomicPredicates<SymbolicAsPathRegex> asAPs = cap.getAsPathRegexAtomicPredicates();

    assertEquals(commAPs.getAtomicPredicateAutomata().size(), 1);
    assertThat(
        commAPs.getAtomicPredicateAutomata().values(),
        hasItem(CommunityVar.ALL_STANDARD_COMMUNITIES.toAutomaton()));

    assertEquals(cap.getNonStandardCommunityLiterals().size(), 0);

    assertEquals(asAPs.getAtomicPredicateAutomata().size(), 1);
    assertThat(
        asAPs.getAtomicPredicateAutomata().values(),
        hasItem(SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton()));
  }

  @Test
  public void testConstructor2() {
    ConfigAtomicPredicates cap =
        new ConfigAtomicPredicates(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            ImmutableSet.of(CommunityVar.from(StandardCommunity.parse("30:40"))),
            ImmutableSet.of("^$"));

    RegexAtomicPredicates<CommunityVar> commAPs = cap.getStandardCommunityAtomicPredicates();
    RegexAtomicPredicates<SymbolicAsPathRegex> asAPs = cap.getAsPathRegexAtomicPredicates();

    assertEquals(commAPs.getAtomicPredicateAutomata().size(), 2);
    assertThat(
        commAPs.getAtomicPredicateAutomata().values(),
        hasItem(CommunityVar.from(StandardCommunity.parse("30:40")).toAutomaton()));

    assertEquals(cap.getNonStandardCommunityLiterals().size(), 0);

    assertEquals(asAPs.getAtomicPredicateAutomata().size(), 2);
    assertThat(
        asAPs.getAtomicPredicateAutomata().values(),
        hasItem(new SymbolicAsPathRegex("^$").toAutomaton()));
  }

  @Test
  public void testConstructor3() {
    ConfigAtomicPredicates cap =
        new ConfigAtomicPredicates(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            ImmutableSet.of(CommunityVar.from(ExtendedCommunity.parse("0:30:40"))),
            ImmutableSet.of("^$"));

    RegexAtomicPredicates<CommunityVar> commAPs = cap.getStandardCommunityAtomicPredicates();
    RegexAtomicPredicates<SymbolicAsPathRegex> asAPs = cap.getAsPathRegexAtomicPredicates();

    assertEquals(commAPs.getAtomicPredicateAutomata().size(), 1);
    assertThat(
        commAPs.getAtomicPredicateAutomata().values(),
        hasItem(CommunityVar.ALL_STANDARD_COMMUNITIES.toAutomaton()));

    assertEquals(cap.getNonStandardCommunityLiterals().size(), 1);
    assertThat(
        cap.getNonStandardCommunityLiterals().values(),
        hasItem(CommunityVar.from(ExtendedCommunity.parse("0:30:40"))));

    assertEquals(asAPs.getAtomicPredicateAutomata().size(), 2);
    assertThat(
        asAPs.getAtomicPredicateAutomata().values(),
        hasItem(new SymbolicAsPathRegex("^$").toAutomaton()));
  }

  @Test
  public void testCopyConstructor() {
    ConfigAtomicPredicates cap =
        new ConfigAtomicPredicates(
            _batfish,
            _batfish.getSnapshot(),
            HOSTNAME,
            ImmutableSet.of(CommunityVar.from(ExtendedCommunity.parse("0:30:40"))),
            ImmutableSet.of("^$"));

    ConfigAtomicPredicates copy = new ConfigAtomicPredicates(cap);

    assertNotSame(
        cap.getStandardCommunityAtomicPredicates(), copy.getStandardCommunityAtomicPredicates());
    assertNotSame(cap.getNonStandardCommunityLiterals(), copy.getNonStandardCommunityLiterals());
    assertNotSame(cap.getAsPathRegexAtomicPredicates(), copy.getAsPathRegexAtomicPredicates());
    // the lists of tracks and source VRFs are immutable
    assertEquals(cap.getTracks(), copy.getTracks());
    assertEquals(cap.getSourceVrfs(), copy.getSourceVrfs());
  }

  @Test
  public void testCallStatement() {

    RoutingPolicy firstPolicy =
        _nf.routingPolicyBuilder()
            .setName("firstPolicy")
            .setOwner(_baseConfig)
            .addStatement(new CallStatement("secondPolicy"))
            .addStatement(
                new SetCommunities(
                    CommunitySetUnion.of(
                        InputCommunities.instance(),
                        new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("1:1"))))))
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();

    RoutingPolicy secondPolicy =
        _nf.routingPolicyBuilder()
            .setName("secondPolicy")
            .setOwner(_baseConfig)
            .addStatement(new CallStatement("firstPolicy"))
            .addStatement(
                new SetCommunities(
                    CommunitySetUnion.of(
                        InputCommunities.instance(),
                        new LiteralCommunitySet(CommunitySet.of(StandardCommunity.parse("2:2"))))))
            .addStatement(new Statements.StaticStatement(Statements.ExitAccept))
            .build();

    _baseConfig.setRoutingPolicies(
        ImmutableMap.of("firstPolicy", firstPolicy, "secondPolicy", secondPolicy));
    ConfigAtomicPredicates cap =
        new ConfigAtomicPredicates(_batfish, _batfish.getSnapshot(), HOSTNAME);

    assertEquals(3, cap.getStandardCommunityAtomicPredicates().getNumAtomicPredicates());
  }
}
