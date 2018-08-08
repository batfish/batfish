package org.batfish.specifier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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

    Interface iface1_1 = Interface.builder().setName("good").setOwner(node1).build();
    Interface iface1_2 = Interface.builder().setName("bad").setOwner(node1).build();
    Interface iface2 = Interface.builder().setName("irrelevant").setOwner(node2).build();

    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of("node1", node1, "node2", node2))
            .build();

    assertThat(
        new ShorthandInterfaceSpecifier(new InterfacesSpecifier("good"))
            .resolve(ImmutableSet.of("node1"), ctxt),
        equalTo(ImmutableSet.of(iface1_1)));
  }
}
