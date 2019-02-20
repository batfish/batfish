package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.regex.Pattern;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.junit.Test;

public class NameRegexFilterSpecifierTest {

  @Test
  public void testResolve() {

    Configuration node1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration node2 = new Configuration("node2", ConfigurationFormat.CISCO_IOS);

    IpAccessList filter1node1 = IpAccessList.builder().setName("filter1").setOwner(node1).build();
    IpAccessList filter2node1 = IpAccessList.builder().setName("filter2").setOwner(node1).build();
    IpAccessList filter1node2 = IpAccessList.builder().setName("filter1").setOwner(node2).build();

    node1.setIpAccessLists(
        ImmutableSortedMap.of(
            filter1node1.getName(), filter1node1, filter2node1.getName(), filter2node1));
    node2.setIpAccessLists(ImmutableSortedMap.of(filter1node2.getName(), filter1node2));

    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of(node1.getHostname(), node1, node2.getHostname(), node2))
            .build();

    assertThat(
        new NameRegexFilterSpecifier(Pattern.compile("filter1")).resolve("node1", ctxt),
        equalTo(ImmutableSet.of(filter1node1)));

    // bad node name
    assertThat(
        new NameRegexFilterSpecifier(Pattern.compile("filter1")).resolve("node", ctxt),
        equalTo(ImmutableSet.of()));

    // non-matching pattern
    assertThat(
        new NameRegexFilterSpecifier(Pattern.compile("filter")).resolve("node1", ctxt),
        equalTo(ImmutableSet.of()));
  }
}
