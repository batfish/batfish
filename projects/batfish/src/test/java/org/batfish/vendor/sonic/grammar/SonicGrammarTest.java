package org.batfish.vendor.sonic.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.vendor.sonic.representation.ConfigDb;
import org.batfish.vendor.sonic.representation.DeviceMetadata;
import org.batfish.vendor.sonic.representation.L3Interface;
import org.batfish.vendor.sonic.representation.SonicConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SonicGrammarTest {

  private static final String SNAPSHOTS_PREFIX = "org/batfish/vendor/sonic/grammar/snapshots/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  /** A basic test that configdb files are read and properly linked to the FRR files. */
  @Test
  public void testBasic() throws IOException {
    String snapshotName = "basic";

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setSonicConfigFiles(
                    SNAPSHOTS_PREFIX + snapshotName,
                    ImmutableList.of("device/frr.conf", "device/config_db.json"))
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("basic");
    assertThat(
        vc.getConfigDb(),
        equalTo(
            ConfigDb.builder()
                .setDeviceMetadata(ImmutableMap.of("localhost", new DeviceMetadata("basic")))
                .setInterfaces(
                    ImmutableMap.of(
                        "Ethernet0", new L3Interface(ConcreteInterfaceAddress.parse("1.1.1.1/24"))))
                .build()));
    assertThat(vc.getFrrConfiguration().getRouteMaps().keySet(), equalTo(ImmutableSet.of("TEST")));

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertEquals("basic", c.getHostname());
  }
}
