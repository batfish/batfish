package org.batfish.specifier;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.junit.Test;

/** Tests for {@link NodeSpecifierFilterSpecifier} */
public class NodeSpecifierFilterSpecifierTest {

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

    // common node
    assertThat(
        new NodeSpecifierFilterSpecifier(new NameNodeSpecifier("node1"))
            .resolve(ImmutableSet.of("node1", "node2"), ctxt),
        containsInAnyOrder(filter1node1, filter2node1));

    // no common nodes
    assertThat(
        new NodeSpecifierFilterSpecifier(new NameNodeSpecifier("node1"))
            .resolve(ImmutableSet.of("node2"), ctxt),
        empty());
  }
}
