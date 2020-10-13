package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.junit.Test;

public class NameFilterSpecifierTest {

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
        new NameFilterSpecifier("filter1").resolve("node1", ctxt),
        equalTo(ImmutableSet.of(filter1node1)));

    // bad node name
    assertThat(
        new NameFilterSpecifier("filter1").resolve("node", ctxt), equalTo(ImmutableSet.of()));

    // bad filter name
    assertThat(
        new NameFilterSpecifier("filter").resolve("node1", ctxt), equalTo(ImmutableSet.of()));

    // case insensitive
    assertThat(
        new NameFilterSpecifier("FILTER1").resolve("NODE1", ctxt),
        equalTo(ImmutableSet.of(filter1node1)));
  }
}
