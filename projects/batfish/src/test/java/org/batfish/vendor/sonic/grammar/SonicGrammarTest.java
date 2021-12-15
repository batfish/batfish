package org.batfish.vendor.sonic.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAccessVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.Arrays;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.vendor.sonic.representation.DeviceMetadata;
import org.batfish.vendor.sonic.representation.L3Interface;
import org.batfish.vendor.sonic.representation.MgmtVrf;
import org.batfish.vendor.sonic.representation.Port;
import org.batfish.vendor.sonic.representation.SonicConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SonicGrammarTest {

  private static final String SNAPSHOTS_PREFIX = "org/batfish/vendor/sonic/grammar/snapshots/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfish(String snapshotName, String... files) throws IOException {
    return BatfishTestUtils.getBatfishFromTestrigText(
        TestrigText.builder()
            .setSonicConfigFiles(SNAPSHOTS_PREFIX + snapshotName, Arrays.asList(files))
            .build(),
        _folder);
  }

  /** A basic test that configdb files are read and properly linked to the FRR files. */
  @Test
  public void testBasic() throws IOException {
    String snapshotName = "basic";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("basic");
    vc.setWarnings(new Warnings());
    assertThat(
        vc.getConfigDb().getDeviceMetadata(),
        equalTo(ImmutableMap.of("localhost", new DeviceMetadata("basic"))));
    assertThat(
        vc.getConfigDb().getPorts(),
        equalTo(ImmutableMap.of("Ethernet0", Port.builder().setDescription("basic-port").build())));
    assertThat(
        vc.getConfigDb().getInterfaces(),
        equalTo(
            ImmutableMap.of(
                "Ethernet0", new L3Interface(ConcreteInterfaceAddress.parse("1.1.1.1/24")))));
    assertThat(vc.getFrrConfiguration().getRouteMaps().keySet(), equalTo(ImmutableSet.of("TEST")));

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertEquals("basic", c.getHostname());
    assertThat(
        Iterables.getOnlyElement(c.getAllInterfaces().values()),
        allOf(
            hasName("Ethernet0"),
            hasVrfName(DEFAULT_VRF_NAME),
            hasAddress("1.1.1.1/24"),
            hasDescription("basic-port")));
  }

  /** Test that management interfaces are created and put in the right VRF. */
  @Test
  public void testMgmt() throws IOException {
    String snapshotName = "mgmt";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("mgmt");
    vc.setWarnings(new Warnings());

    assertEquals(
        ImmutableMap.of("localhost", new DeviceMetadata("mgmt")),
        vc.getConfigDb().getDeviceMetadata());
    assertEquals(
        ImmutableMap.of("eth0", new L3Interface(ConcreteInterfaceAddress.parse("1.1.1.1/24"))),
        vc.getConfigDb().getMgmtInterfaces());
    assertEquals(
        ImmutableMap.of("eth0", Port.builder().setAdminStatusUp(true).build()),
        vc.getConfigDb().getMgmtPorts());
    assertEquals(
        ImmutableMap.of(
            "vrf_global_not_default", MgmtVrf.builder().setMgmtVrfEnabled(true).build()),
        vc.getConfigDb().getMgmtVrfs());

    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());
    assertThat(
        Iterables.getOnlyElement(c.getAllInterfaces().values()),
        allOf(hasName("eth0"), hasVrfName("vrf_global_not_default"), hasAddress("1.1.1.1/24")));
  }

  /** Test that VLAN related tables are processed. */
  @Test
  public void testVlan() throws IOException {
    String snapshotName = "vlan";
    Batfish batfish = getBatfish(snapshotName, "device/frr.conf", "device/config_db.json");

    NetworkSnapshot snapshot = batfish.getSnapshot();
    SonicConfiguration vc =
        (SonicConfiguration) batfish.loadVendorConfigurations(snapshot).get("vlan");
    vc.setWarnings(new Warnings());
    Configuration c = getOnlyElement(vc.toVendorIndependentConfigurations());

    assertThat(
        c.getAllInterfaces().get("Vlan1"),
        allOf(
            hasName("Vlan1"),
            hasVrfName("default"),
            hasAddress("172.19.0.1/24"),
            hasInterfaceType(InterfaceType.VLAN)));

    assertThat(
        c.getAllInterfaces().get("Ethernet0"),
        allOf(hasName("Ethernet0"), hasAccessVlan(1), hasSwitchPortMode(SwitchportMode.ACCESS)));
  }
}
