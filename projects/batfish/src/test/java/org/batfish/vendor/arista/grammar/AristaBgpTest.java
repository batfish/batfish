package org.batfish.vendor.arista.grammar;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasLocalPreference;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class AristaBgpTest {
  private static final String DEFAULT_VRF_NAME = "default";
  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/arista/grammar/testconfigs/";
  private static final String SNAPSHOTS_PREFIX = "org/batfish/vendor/arista/grammar/snapshots/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  /** Undefined redistribute map is treated as permitting everything. */
  @Test
  public void testBgpRedistributeUndefined() throws IOException {
    String config = "bgp-redistribute-undefined";
    Batfish batfish = getBatfishForConfigurationNames(config);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    assertThat(
        dp.getBgpRoutes().get(config, DEFAULT_VRF_NAME),
        contains(hasPrefix(Prefix.parse("1.2.3.4/24"))));
  }

  /** BGP local-pref and export-localpref. */
  @Test
  public void testBgpLocalPreference() throws IOException {
    String originator = "originator";
    String listener = "listener";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    SNAPSHOTS_PREFIX + "bgp-local-preference",
                    ImmutableList.of(originator, listener))
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    assertThat(
        dp.getBgpRoutes().get(listener, DEFAULT_VRF_NAME),
        hasItems(
            allOf(hasPrefix(Prefix.parse("172.16.1.1/32")), hasLocalPreference(100L)),
            allOf(hasPrefix(Prefix.parse("172.16.1.2/32")), hasLocalPreference(5L))));
    assertThat(
        dp.getBgpRoutes().get(originator, DEFAULT_VRF_NAME),
        hasItem(allOf(hasPrefix(Prefix.parse("172.16.2.1/32")), hasLocalPreference(250L))));
  }
}
