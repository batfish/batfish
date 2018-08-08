package org.batfish.specifier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.junit.Test;

public class ShorthandInterfaceSpecifierTest {

  @Test
  public void resolve() {

    Configuration node1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration node2 = new Configuration("node2", ConfigurationFormat.CISCO_IOS);

    Interface iface11 = Interface.builder().setName("iface11").setOwner(node1).build();
    Interface iface12 = Interface.builder().setName("iface12").setOwner(node1).build();
    Interface iface2 = Interface.builder().setName("iface2").setOwner(node2).build();

    node1.setInterfaces(ImmutableSortedMap.of("iface11", iface11, "iface12", iface12));
    node2.setInterfaces(ImmutableSortedMap.of("iface2", iface2));

    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of("node1", node1, "node2", node2))
            .build();

    assertThat(
        new ShorthandInterfaceSpecifier(new InterfacesSpecifier("iface11"))
            .resolve(ImmutableSet.of("node1"), ctxt),
        equalTo(ImmutableSet.of(iface11)));
  }
}
