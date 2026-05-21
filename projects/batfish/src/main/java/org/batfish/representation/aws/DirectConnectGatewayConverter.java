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

  public static List<Configuration> convertDirectConnectGateways(
      AwsConfiguration awsConfiguration) {
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
                v ->
                    vifsByDxgw
                        .computeIfAbsent(v.getDirectConnectGatewayId(), k -> new ArrayList<>())
                        .add(v));
      }
    }

    return uniqueGateways.values().stream()
        .map(
            dxgw ->
                dxgw.toConfigurationNode(
                    associationsByDxgw.getOrDefault(dxgw.getId(), ImmutableList.of()),
                    vifsByDxgw.getOrDefault(dxgw.getId(), ImmutableList.of())))
        .collect(ImmutableList.toImmutableList());
  }

  private DirectConnectGatewayConverter() {}
}
