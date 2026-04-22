package org.batfish.grammar.flatjuniper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResourceBytes;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@code irb} interfaces. */
public final class JuniperIrbTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/juniper/testconfigs/";
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      assertThat(configs, hasKey(hostname.toLowerCase()));
      return configs.get(hostname.toLowerCase());
    } catch (IOException e) {
      throw new AssertionError("Failed to parse " + hostname, e);
    }
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  @Test
  public void testIrbDeactivate() {
    Configuration c = parseConfig("irb-deactivate");
    assertThat(c, hasInterface("irb.2", isActive()));
    assertThat(c, hasInterface("irb.5", isActive(false)));
  }

  /**
   * VNI VLANs without trunk port members should have their IRB interfaces deactivated by autostate,
   * regardless of whether the VLAN has a VXLAN VNI. When runtime data reports the trunk port as
   * line-down, the IRB that depended on it should also be deactivated.
   */
  @Test
  public void testIrbVxlanAutostate() throws IOException {
    String hostname = "irb-vxlan-autostate";

    // Without runtime data: VLAN100 has a trunk member (xe-0/0/0), VLAN200 does not.
    // irb.100 should be active, irb.200 should be deactivated by autostate.
    Configuration c = parseConfig(hostname);
    assertThat(c, hasInterface("irb.100", isActive()));
    assertThat(c, hasInterface("irb.200", isActive(false)));

    // With runtime data reporting xe-0/0/0 (physical) as line-down: the bind dependency
    // deactivates xe-0/0/0.0, leaving VLAN100 with zero active members. Both IRBs should be
    // deactivated by autostate.
    SnapshotRuntimeData lineDown =
        SnapshotRuntimeData.builder()
            .setInterfacesLineDown(ImmutableList.of(NodeInterfacePair.of(hostname, "xe-0/0/0")))
            .build();
    Configuration cLineDown = parseConfigWithRuntimeData(hostname, lineDown);
    assertThat(cLineDown, hasInterface("irb.100", isActive(false)));
    assertThat(cLineDown, hasInterface("irb.200", isActive(false)));

    // With runtime data reporting xe-0/0/0 as line-up: irb.100 stays active, irb.200 still
    // deactivated (no trunk member regardless of runtime data).
    SnapshotRuntimeData lineUp =
        SnapshotRuntimeData.builder()
            .setInterfacesLineUp(ImmutableList.of(NodeInterfacePair.of(hostname, "xe-0/0/0")))
            .build();
    Configuration cLineUp = parseConfigWithRuntimeData(hostname, lineUp);
    assertThat(cLineUp, hasInterface("irb.100", isActive()));
    assertThat(cLineUp, hasInterface("irb.200", isActive(false)));
  }

  private Configuration parseConfigWithRuntimeData(String hostname, SnapshotRuntimeData runtimeData)
      throws IOException {
    String resourcePath = TESTCONFIGS_PREFIX + hostname;
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationBytes(
                    ImmutableSortedMap.of(
                        new File(resourcePath).getName(), readResourceBytes(resourcePath)))
                .setRuntimeDataBytes(BatfishObjectMapper.writeString(runtimeData).getBytes(UTF_8))
                .build(),
            _folder);
    Map<String, Configuration> configs = batfish.loadConfigurations(batfish.getSnapshot());
    assertThat(configs, hasKey(hostname.toLowerCase()));
    return configs.get(hostname.toLowerCase());
  }
}
