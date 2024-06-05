package org.batfish.grammar.cumulus_concatenated;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.RoutingProtocol.BGP;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test that 10.2.10.0/24 is present as a BGP route on s0.
 *
 * <p>See <a href="https://github.com/batfish/batfish/issues/8588">batfish/batfish#8588</a>
 */
public final class Gh8588AdvertiseInactiveTest {

  @Test
  public void testGh8588AdvertiseInactive() throws IOException {
    String snapshotName = "gh_8588_advertise_inactive";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    SNAPSHOTS_PREFIX + snapshotName, ImmutableList.of("s0.cfg", "s1.cfg", "s2.cfg"))
                .setHostsFiles(SNAPSHOTS_PREFIX + snapshotName, ImmutableList.of("d1.json"))
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    FinalMainRib s0MainRib = dp.getRibs().get("s0", DEFAULT_VRF_NAME);
    assert s0MainRib != null;
    assertThat(s0MainRib.getRoutes(Prefix.strict("10.2.10.0/24")), contains(hasProtocol(BGP)));
  }

  private static final String SNAPSHOTS_PREFIX =
      "org/batfish/grammar/cumulus_concatenated/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
}
