package org.batfish.dataplane.ibdp;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.junit.Test;

public class NodeTest {
  @Test
  public void testGetRib() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.FLAT_JUNIPER)
            .build();
    Vrf v = Vrf.builder().setOwner(c).setName("v").build();
    Node n = new Node(c);
    // Right Vrf, right RIB -> present
    assertThat(
        n.getRib(new RibId(c.getHostname(), v.getName(), RibId.DEFAULT_RIB_NAME)).isPresent(),
        equalTo(true));
    // Right Vrf, wrong RIB -> absent
    assertThat(
        n.getRib(new RibId(c.getHostname(), v.getName(), "no-such-rib")).isPresent(),
        equalTo(false));
    // Wrong Vrf -> absent
    assertThat(
        n.getRib(new RibId(c.getHostname(), "no-such-vrf", RibId.DEFAULT_RIB_NAME)).isPresent(),
        equalTo(false));
    // Wrong Config -> absent
    assertThat(
        n.getRib(new RibId("no-such-host", v.getName(), RibId.DEFAULT_RIB_NAME)).isPresent(),
        equalTo(false));
  }
}
