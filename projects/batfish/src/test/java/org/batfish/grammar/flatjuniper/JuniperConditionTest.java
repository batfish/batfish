package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test of Juniper conditional BGP export */
public final class JuniperConditionTest {
  @Test
  public void testConditionalExport() throws IOException {
    // Topology:
    // s1 -- c -- s21 -- s22
    //       |
    //       r
    //
    // Each device above peers with its neighbor(s) via an eBGP session.
    // All devices have a unique autonomous system.
    // Device c conditionally exports 4 routes to r, only 3 of which will be actually be sent:
    // - 1.0.0.1/32 (condition on route that travels 1 hop)
    //   - SHOULD be exported to r
    //   - will be received over eBGP from s1
    //   - exported on condition that 1.0.0.0/32 is present in inet.0
    //     - 1.0.0.0/32 will also be received from s1
    // - 2.0.0.1/32 (condition on route that travels 2 hops)
    //   - SHOULD be exported to r
    //   - is a static discard route in inet.0
    //   - exported on condition that 2.0.0.0/32 is present in inet.0
    //     - will be received from s21 via s22
    // - 3.0.0.1/32 (condition on route in non-default VRF)
    //   - SHOULD be exported to r
    //   - is a static discard route in inet.0
    //   - exported on condition that 3.0.0.0/32 is present in ri2.inet.0
    //     - 3.0.0.0/32 is a static discard route in ri2.inet.0
    // - 4.0.0.1/32
    //   - SHOULD NOT be exported to r
    //   - is a static discard route in inet.0
    //   - exported on condition that 4.0.0.0/32 is present in inet.0
    //     - 4.0.0.0/32 is absent from the snapshot, so condition SHOULD NOT succeed
    String snapshotName = "conditional-export";
    List<String> configurationNames = ImmutableList.of("c", "r", "s1", "s21", "s22");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, configurationNames)
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    FinalMainRib rMainRib =
        batfish.loadDataPlane(batfish.getSnapshot()).getRibs().get("r", DEFAULT_VRF_NAME);
    assert rMainRib != null;
    Set<AbstractRoute> rBgpRoutes =
        rMainRib.getRoutes().stream()
            .filter(r -> r.getProtocol() == RoutingProtocol.BGP)
            .collect(ImmutableSet.toImmutableSet());
    assertThat(
        rBgpRoutes,
        containsInAnyOrder(
            hasPrefix(Prefix.strict("1.0.0.1/32")),
            hasPrefix(Prefix.strict("2.0.0.1/32")),
            hasPrefix(Prefix.strict("3.0.0.1/32"))));
  }

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  private static final String SNAPSHOTS_PREFIX = "org/batfish/grammar/juniper/testrigs/";
}
