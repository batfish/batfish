package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.testSetup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Multi-region snapshot test for the global Direct Connect Gateway. Reproduces the bug where the
 * DXGW Configuration node was rebuilt per-region and the {@code
 * TransitGateway.connectDirectConnect} lookup was scoped to a single region. The snapshot has:
 *
 * <ul>
 *   <li>One global DXGW (entry duplicated in both regions, as AWS returns it).
 *   <li>{@code tgw-east} in us-east-1, {@code tgw-west} in us-west-1, each with a DX attachment.
 *   <li>Associations file ONLY in us-east-1, listing both {@code (DXGW↔tgw-east)} and {@code
 *       (DXGW↔tgw-west)}.
 * </ul>
 *
 * Before the fix, the per-region DXGW build in us-west-1 (where no associations exist) overwrote
 * the wired version, and the TGW conversion of {@code tgw-west} couldn't find the (DXGW↔tgw-west)
 * association in its own region. Now both TGW-facing interfaces appear on the single global DXGW.
 */
public class AwsConfigurationDirectConnectMultiRegionTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-direct-connect-gateway-multi-region";

  private static final List<String> fileNames =
      ImmutableList.of(
          "accounts/123456789012/us-east-1/DirectConnectGateways.json",
          "accounts/123456789012/us-east-1/DirectConnectGatewayAssociations.json",
          "accounts/123456789012/us-east-1/TransitGateways.json",
          "accounts/123456789012/us-east-1/TransitGatewayAttachments.json",
          "accounts/123456789012/us-east-1/TransitGatewayRouteTables.json",
          "accounts/123456789012/us-west-1/DirectConnectGateways.json",
          "accounts/123456789012/us-west-1/TransitGateways.json",
          "accounts/123456789012/us-west-1/TransitGatewayAttachments.json",
          "accounts/123456789012/us-west-1/TransitGatewayRouteTables.json");

  private static final String _dxgw =
      DirectConnectGateway.nodeName("7b2c4f8a-1234-4abc-9def-fedcba987654");
  private static final String _tgwEast = "tgw-east";
  private static final String _tgwWest = "tgw-west";

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  /**
   * Verify the DXGW node has interfaces toward both TGWs even though the associations are only
   * declared in one region. This is the headline regression test: prior to the global converter,
   * the {@code tgw-west} side wouldn't be wired because its region carries no associations.
   */
  @Test
  public void testDxgwHasInterfacesTowardBothTgws() {
    Configuration dxgwCfg = _batfish.loadConfigurations(_batfish.getSnapshot()).get(_dxgw);
    assertThat(
        "DXGW Configuration node should be present", dxgwCfg, org.hamcrest.Matchers.notNullValue());
    // Interface name format from Utils.connect: <remoteHostname>-<suffix>.
    assertThat(
        "DXGW should have an interface to tgw-east",
        dxgwCfg.getAllInterfaces().keySet(),
        hasItem(_tgwEast + "-tgw-rtb-east"));
    assertThat(
        "DXGW should have an interface to tgw-west (association is in us-east-1)",
        dxgwCfg.getAllInterfaces().keySet(),
        hasItem(_tgwWest + "-tgw-rtb-west"));
  }

  /** Both TGWs should also have their DXGW-facing interfaces. */
  @Test
  public void testBothTgwsHaveInterfacesTowardDxgw() {
    Configuration tgwEast = _batfish.loadConfigurations(_batfish.getSnapshot()).get(_tgwEast);
    Configuration tgwWest = _batfish.loadConfigurations(_batfish.getSnapshot()).get(_tgwWest);
    assertThat(
        "tgw-east should have an interface to DXGW",
        tgwEast.getAllInterfaces().keySet(),
        hasItem(_dxgw + "-tgw-rtb-east"));
    assertThat(
        "tgw-west should have an interface to DXGW",
        tgwWest.getAllInterfaces().keySet(),
        hasItem(_dxgw + "-tgw-rtb-west"));
  }
}
