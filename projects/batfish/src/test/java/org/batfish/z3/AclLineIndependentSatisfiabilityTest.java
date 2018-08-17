package org.batfish.z3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AclLineIndependentSatisfiabilityTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  Configuration _c;
  IpAccessList.Builder _aclb;
  static final IpAccessListLine _unmatchable =
      IpAccessListLine.accepting().setMatchCondition(FalseExpr.INSTANCE).build();
  static final IpAccessListLine _alwaysMatches =
      IpAccessListLine.accepting().setMatchCondition(TrueExpr.INSTANCE).build();

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    _c = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    _aclb = nf.aclBuilder().setOwner(_c);
  }

  @Test
  public void testUnmatchableAclLinesUnmatchableFirst() throws IOException {
    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(_c.getHostname(), _c);
    _aclb.setName("acl").setLines(ImmutableList.of(_unmatchable, _alwaysMatches)).build();
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, _folder);

    Set<Integer> unmatchable =
        computeIndependentlyUnmatchableAclLines(batfish, _c, "acl", ImmutableSet.of(0, 1));

    assertThat(unmatchable, equalTo(ImmutableSet.of(0)));
  }

  @Test
  public void testUnmatchableAclLinesUnmatchableCovered() throws IOException {
    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(_c.getHostname(), _c);
    _aclb.setName("acl").setLines(ImmutableList.of(_alwaysMatches, _unmatchable)).build();
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, _folder);

    Set<Integer> unmatchable =
        computeIndependentlyUnmatchableAclLines(batfish, _c, "acl", ImmutableSet.of(0, 1));

    assertThat(unmatchable, equalTo(ImmutableSet.of(1)));
  }

  @Test
  public void testUnmatchableAclLinesUnmatchableTwice() throws IOException {
    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(_c.getHostname(), _c);
    _aclb.setName("acl").setLines(ImmutableList.of(_unmatchable, _unmatchable)).build();
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, _folder);

    Set<Integer> unmatchable =
        computeIndependentlyUnmatchableAclLines(batfish, _c, "acl", ImmutableSet.of(0, 1));

    assertThat(unmatchable, equalTo(ImmutableSet.of(0, 1)));
  }

  @Test
  public void testUnmatchableAclLinesMatchableWithCovered() throws IOException {
    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(_c.getHostname(), _c);
    _aclb.setName("acl").setLines(ImmutableList.of(_alwaysMatches, _alwaysMatches)).build();
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, _folder);

    Set<Integer> unmatchable =
        computeIndependentlyUnmatchableAclLines(batfish, _c, "acl", ImmutableSet.of(0, 1));

    assertThat(unmatchable, equalTo(ImmutableSet.of()));
  }

  private Set<Integer> computeIndependentlyUnmatchableAclLines(
      Batfish batfish, Configuration c, String aclName, Set<Integer> linesToCheck) {
    List<NodSatJob<AclLine>> jobs =
        linesToCheck
            .stream()
            .map(l -> batfish.generateUnmatchableAclLineJob(c, aclName, l))
            .collect(Collectors.toList());
    Map<AclLine, Boolean> satisfiabilityByLine = new TreeMap<>();
    batfish.computeNodSatOutput(jobs, satisfiabilityByLine);

    return satisfiabilityByLine
        .entrySet()
        .stream()
        .filter(e -> !e.getValue())
        .map(e -> e.getKey().getLine())
        .collect(Collectors.toSet());
  }
}
