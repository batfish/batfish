package org.batfish.common.topology;

import static org.batfish.common.topology.InterfaceUtil.matchingInterface;
import static org.batfish.common.topology.InterfaceUtil.matchingInterfaceName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Test;

public class InterfaceUtilTest {
  @Test
  public void testMatchingInterfaceNameKnown() {
    Set<String> known = ImmutableSet.of("Ethernet1", "Ethernet2");
    assertThat(matchingInterfaceName("Ethernet1", known), equalTo(Optional.of("Ethernet1")));
    assertThat(matchingInterfaceName("ETHERNET1", known), equalTo(Optional.of("Ethernet1")));
    assertThat(matchingInterfaceName("Ethernet3", known), equalTo(Optional.empty()));
  }

  @Test
  public void testMatchingInterfaceNameCanonicalized() {
    Set<String> known = ImmutableSet.of("TenGigE1", "TenGigE1.5");
    assertThat(matchingInterfaceName("Ethernet1", known), equalTo(Optional.empty()));
    assertThat(
        matchingInterfaceName("TenGigabitEthernet1", known), equalTo(Optional.of("TenGigE1")));
    assertThat(
        matchingInterfaceName("tengigabitethernet1", known), equalTo(Optional.of("TenGigE1")));
  }

  @Test
  public void testMatchingInterface() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface i =
        nf.interfaceBuilder()
            .setOwner(c)
            .setName("Ethernet1")
            .setType(InterfaceType.PHYSICAL)
            .build();
    assertThat(matchingInterface(i.getName(), c).get(), sameInstance(i));
    assertThat(matchingInterface(i.getName().toLowerCase(), c).get(), sameInstance(i));
    assertThat(matchingInterface(i.getName().toUpperCase(), c).get(), sameInstance(i));
    assertThat(matchingInterface(i.getName() + 'x', c), equalTo(Optional.empty()));
  }
}
