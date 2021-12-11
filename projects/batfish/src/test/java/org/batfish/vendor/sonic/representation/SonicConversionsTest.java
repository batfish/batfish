package org.batfish.vendor.sonic.representation;

import static org.batfish.datamodel.ConfigurationFormat.SONIC;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSpeed;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertPorts;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

public class SonicConversionsTest {

  @Test
  public void testConvertPorts() {
    Port port =
        Port.builder()
            .setAdminStatusUp(false)
            .setMtu(56)
            .setDescription("desc")
            .setSpeed(23)
            .build();
    String ifaceAddress = "1.1.1.1/24";
    {
      Configuration c =
          Configuration.builder().setHostname("name").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setName("vrf").setOwner(c).build();
      convertPorts(
          c,
          ImmutableMap.of("iface", port),
          ImmutableMap.of("iface", new L3Interface(ConcreteInterfaceAddress.parse(ifaceAddress))),
          vrf);
      assertThat(
          Iterables.getOnlyElement(c.getAllInterfaces().values()),
          allOf(
              hasName("iface"),
              hasAddress(ifaceAddress),
              hasMtu(56),
              hasDescription("desc"),
              hasSpeed(23.0)));
    }
    {
      // interface does not exist
      Configuration c =
          Configuration.builder().setHostname("name").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setName("vrf").setOwner(c).build();
      convertPorts(c, ImmutableMap.of("iface", port), ImmutableMap.of(), vrf);
      assertThat(Iterables.getOnlyElement(c.getAllInterfaces().values()).getAddress(), nullValue());
    }
  }
}
