package org.batfish.z3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AclLineIndependentSatisfiabilityTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testComputeIndependentlyUnmatchableAclLines() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    IpAccessList.Builder aclb = nf.aclBuilder().setOwner(c);
    IpAccessListLine unmatchable =
        IpAccessListLine.accepting().setMatchCondition(FalseExpr.INSTANCE).build();
    IpAccessListLine alwaysMatches =
        IpAccessListLine.accepting().setMatchCondition(TrueExpr.INSTANCE).build();
    IpAccessList unmatchableFirst =
        aclb.setLines(ImmutableList.of(unmatchable, alwaysMatches)).build();
    IpAccessList unmatchableCovered =
        aclb.setLines(ImmutableList.of(alwaysMatches, unmatchable)).build();
    IpAccessList unmatchableTwice =
        aclb.setLines(ImmutableList.of(unmatchable, unmatchable)).build();
    IpAccessList matchableWithCovered =
        aclb.setLines(ImmutableList.of(alwaysMatches, alwaysMatches)).build();
    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(c.getHostname(), c);
    Map<String, Map<String, Set<Integer>>> representatives =
        ImmutableMap.of(
            c.getHostname(),
            ImmutableMap.of(
                unmatchableFirst.getName(),
                rangeSet(unmatchableFirst.getLines().size()),
                unmatchableCovered.getName(),
                rangeSet(unmatchableCovered.getLines().size()),
                unmatchableTwice.getName(),
                rangeSet(unmatchableTwice.getLines().size()),
                matchableWithCovered.getName(),
                rangeSet(matchableWithCovered.getLines().size())));
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, _folder);
    SortedMap<String, SortedMap<String, SortedSet<Integer>>> unmatchableLinesByHostnameAndAclName =
        batfish.computeIndependentlyUnmatchableAclLines(configurations, representatives);

    assertThat(
        unmatchableLinesByHostnameAndAclName,
        hasEntry(equalTo(c.getName()), hasEntry(equalTo(unmatchableFirst.getName()), hasItem(0))));
    assertThat(
        unmatchableLinesByHostnameAndAclName,
        hasEntry(
            equalTo(c.getName()), hasEntry(equalTo(unmatchableFirst.getName()), not(hasItem(1)))));
    assertThat(
        unmatchableLinesByHostnameAndAclName,
        hasEntry(
            equalTo(c.getName()),
            hasEntry(equalTo(unmatchableCovered.getName()), not(hasItem(0)))));
    assertThat(
        unmatchableLinesByHostnameAndAclName,
        hasEntry(
            equalTo(c.getName()), hasEntry(equalTo(unmatchableCovered.getName()), hasItem(1))));
    assertThat(
        unmatchableLinesByHostnameAndAclName,
        hasEntry(equalTo(c.getName()), hasEntry(equalTo(unmatchableTwice.getName()), hasItem(0))));
    assertThat(
        unmatchableLinesByHostnameAndAclName,
        hasEntry(equalTo(c.getName()), hasEntry(equalTo(unmatchableTwice.getName()), hasItem(1))));
    assertThat(
        unmatchableLinesByHostnameAndAclName,
        hasEntry(equalTo(c.getName()), not(hasKey(matchableWithCovered.getName()))));
  }

  private static Set<Integer> rangeSet(int max) {
    return IntStream.range(0, max).mapToObj(i -> new Integer(i)).collect(Collectors.toSet());
  }
}
