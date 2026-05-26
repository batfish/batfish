package org.batfish.representation.aws;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;

/**
 * Builds Configuration nodes for Direct Connect Gateways. DXGWs are global resources: the same
 * gateway appears in every region's {@code DirectConnectGateways.json}, while its associations and
 * VIFs are regional and may live in different regions. This converter walks every account/region,
 * dedups DXGWs by id, aggregates associations and VIFs across regions, and builds one node per
 * unique DXGW.
 */
@ParametersAreNonnullByDefault
public final class DirectConnectGatewayConverter {

  /**
   * Converts all Direct Connect Gateways in {@code awsConfiguration}, adds the resulting nodes to
   * {@code convertedConfiguration}, and wires DXGW→VGW links for any VGW-typed associations (the
   * VGW nodes must already exist in {@code convertedConfiguration}).
   */
  public static void convertDirectConnectGateways(
      AwsConfiguration awsConfiguration, ConvertedConfiguration convertedConfiguration) {
    Map<String, DirectConnectGateway> uniqueGateways = new HashMap<>();
    Map<String, List<DirectConnectGatewayAssociation>> associationsByDxgw = new HashMap<>();
    Map<String, List<DirectConnectVirtualInterface>> vifsByDxgw = new HashMap<>();

    for (Account account : awsConfiguration.getAccounts()) {
      for (Region region : account.getRegions()) {
        region.getDirectConnectGateways().forEach(uniqueGateways::putIfAbsent);
        region
            .getDirectConnectGatewayAssociations()
            .values()
            .forEach(
                a ->
                    associationsByDxgw
                        .computeIfAbsent(a.getDirectConnectGatewayId(), k -> new ArrayList<>())
                        .add(a));
        region
            .getDirectConnectVirtualInterfaces()
            .values()
            .forEach(
                v -> {
                  if (v.getDirectConnectGatewayId() == null) {
                    return;
                  }
                  vifsByDxgw
                      .computeIfAbsent(v.getDirectConnectGatewayId(), k -> new ArrayList<>())
                      .add(v);
                });
      }
    }

    for (DirectConnectGateway dxgw : uniqueGateways.values()) {
      List<DirectConnectGatewayAssociation> associations =
          associationsByDxgw.getOrDefault(dxgw.getId(), ImmutableList.of());
      List<DirectConnectVirtualInterface> vifs =
          vifsByDxgw.getOrDefault(dxgw.getId(), ImmutableList.of());
      Configuration node = dxgw.toConfigurationNode(associations, vifs);
      convertedConfiguration.addNode(node);

      // Wire DXGW→VGW links for VGW-typed associations. The VGW node was created during the
      // per-region pass, so it must already be present in convertedConfiguration.
      associations.stream()
          .filter(
              a ->
                  a.getAssociatedGateway().getType()
                      == DirectConnectGatewayAssociation.AssociatedGateway.GatewayType
                          .VIRTUAL_PRIVATE_GATEWAY)
          .forEach(
              a ->
                  DirectConnectGateway.connectVgwAssociation(
                      node, dxgw, a, awsConfiguration, convertedConfiguration));
    }
  }

  private DirectConnectGatewayConverter() {}
}
