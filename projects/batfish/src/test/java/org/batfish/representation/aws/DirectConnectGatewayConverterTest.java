package org.batfish.representation.aws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.representation.aws.DirectConnectGatewayAssociation.AssociatedGateway;
import org.batfish.representation.aws.DirectConnectGatewayAssociation.AssociatedGateway.GatewayType;
import org.junit.Test;

/** Tests for {@link DirectConnectGatewayConverter}. */
public class DirectConnectGatewayConverterTest {

  private static final String DXGW_ID = "12345678-90ab-4cde-9f01-23456789abcd";

  private static DirectConnectGateway dxgw() {
    return new DirectConnectGateway(DXGW_ID, "my-dx-gateway", 64512L, ImmutableMap.of());
  }

  /**
   * Reproduces the multi-region bug: the same DXGW JSON entry appears in three regions, but VIFs
   * and associations are only present in one. Before the fix, the per-Region build ran three times
   * and the last invocation (no VIFs/associations) overwrote the wired version. Now there is a
   * single converter pass that aggregates VIFs and associations across all regions.
   */
  @Test
  public void testAggregatesAcrossRegions() {
    AwsConfiguration aws = new AwsConfiguration();
    Account account = aws.addOrGetAccount("123456789012");

    // DXGW is global: same entry in three regions.
    Region us1 = account.addOrGetRegion("us-west-1");
    Region us2 = account.addOrGetRegion("us-west-2");
    Region eu = account.addOrGetRegion("eu-west-2");
    DirectConnectGateway global = dxgw();
    setDxgws(us1, ImmutableMap.of(DXGW_ID, global));
    setDxgws(us2, ImmutableMap.of(DXGW_ID, global));
    setDxgws(eu, ImmutableMap.of(DXGW_ID, global));

    // VIFs and associations are regional and live only in us-west-1.
    DirectConnectGatewayAssociation assoc =
        new DirectConnectGatewayAssociation(
            "dxgw-assoc-1",
            DXGW_ID,
            new AssociatedGateway(
                "tgw-abc", GatewayType.TRANSIT_GATEWAY, "123456789012", "us-west-1"),
            ImmutableList.of(Prefix.parse("10.0.0.0/8")));
    DirectConnectVirtualInterface vif =
        new DirectConnectVirtualInterface(
            "dxvif-1",
            "vif1",
            "transit",
            "dxcon-1",
            DXGW_ID,
            100,
            65001L,
            org.batfish.datamodel.ConcreteInterfaceAddress.parse("169.254.10.1/30"),
            org.batfish.datamodel.ConcreteInterfaceAddress.parse("169.254.10.2/30"),
            ImmutableList.of(),
            ImmutableMap.of());
    us1.getDirectConnectGatewayAssociations().put(assoc.getId(), assoc);
    us1.getDirectConnectVirtualInterfaces().put(vif.getId(), vif);

    List<Configuration> nodes = DirectConnectGatewayConverter.convertDirectConnectGateways(aws);
    assertThat(nodes, hasSize(1));
    Configuration cfg = nodes.get(0);
    assertThat(cfg.getHostname(), is(DirectConnectGateway.nodeName(DXGW_ID)));

    // VIF interface from us-west-1 is wired up despite the DXGW also appearing in two other
    // regions where no VIFs exist.
    assertThat(cfg.getAllInterfaces().keySet(), hasItem(vif.getId()));

    // Allowed-prefix static null route from the us-west-1 association is installed.
    List<Prefix> staticNetworks =
        cfg.getDefaultVrf().getStaticRoutes().stream()
            .map(StaticRoute::getNetwork)
            .collect(Collectors.toList());
    assertThat(staticNetworks, contains(Prefix.parse("10.0.0.0/8")));
  }

  /** When the DXGW is referenced but not declared anywhere, no node is produced. */
  @Test
  public void testNoGatewayNoNode() {
    AwsConfiguration aws = new AwsConfiguration();
    Account account = aws.addOrGetAccount("123456789012");
    account.addOrGetRegion("us-east-1");
    assertThat(DirectConnectGatewayConverter.convertDirectConnectGateways(aws), empty());
  }

  /** Two distinct DXGWs across regions each get their own node. */
  @Test
  public void testMultipleDistinctGateways() {
    AwsConfiguration aws = new AwsConfiguration();
    Account account = aws.addOrGetAccount("123456789012");
    Region region = account.addOrGetRegion("us-east-1");

    DirectConnectGateway a =
        new DirectConnectGateway(
            "aaaaaaaa-1111-4222-9333-444444444444", "a", 64512L, ImmutableMap.of());
    DirectConnectGateway b =
        new DirectConnectGateway(
            "bbbbbbbb-1111-4222-9333-444444444444", "b", 64513L, ImmutableMap.of());
    setDxgws(region, ImmutableMap.of(a.getId(), a, b.getId(), b));

    List<String> hostnames =
        DirectConnectGatewayConverter.convertDirectConnectGateways(aws).stream()
            .map(Configuration::getHostname)
            .collect(Collectors.toList());
    assertThat(
        hostnames,
        containsInAnyOrder(
            DirectConnectGateway.nodeName(a.getId()), DirectConnectGateway.nodeName(b.getId())));
    // Verify the nodeName prefix worked: every hostname should start with "dxgw-" (and not be a
    // bare UUID), so that pybatfish node specifiers can reference these unquoted.
    hostnames.forEach(h -> assertThat(h, startsWith("dxgw-")));
    hostnames.forEach(h -> assertThat(h, not(is(a.getId()))));
  }

  private static void setDxgws(Region region, java.util.Map<String, DirectConnectGateway> dxgws) {
    region.getDirectConnectGateways().clear();
    region.getDirectConnectGateways().putAll(dxgws);
  }
}
