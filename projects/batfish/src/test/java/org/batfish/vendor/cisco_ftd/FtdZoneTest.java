package org.batfish.vendor.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Zone;
import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.junit.Test;

public class FtdZoneTest extends FtdGrammarTest {

  @Test
  public void testZoneCreation() throws IOException {
    String config =
        join(
            "interface Ethernet0/0",
            " nameif inside",
            "interface Ethernet0/1",
            " nameif outside",
            "interface Ethernet0/2",
            " nameif dmz");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Map<String, Zone> zones = c.getZones();

    // Verify zones exist
    assertThat(zones.get("inside"), notNullValue());
    assertThat(zones.get("outside"), notNullValue());
    assertThat(zones.get("dmz"), notNullValue());

    // Verify interface assignment
    assertThat(zones.get("inside").getInterfaces(), contains("Ethernet0/0"));
    assertThat(zones.get("outside").getInterfaces(), contains("Ethernet0/1"));
    assertThat(zones.get("dmz").getInterfaces(), contains("Ethernet0/2"));
  }
}
