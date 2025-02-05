package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;

@ParametersAreNonnullByDefault
public final class TransitGatewayConverter {

  /* Transit Gateway with metadata about which account and region it came from. Required for conversion */
  @VisibleForTesting
  static final class TransitGatewayWithMetadata {
    private @Nonnull TransitGateway _gateway;
    private @Nonnull Region _region;
    private @Nonnull String _accountId;

    @VisibleForTesting
    TransitGatewayWithMetadata(TransitGateway gateway, Region region, String accountId) {
      _gateway = gateway;
      _region = region;
      _accountId = accountId;
    }

    boolean isOriginal() {
      return _accountId.equals(_gateway.getOwnerId());
    }

    String getTransitGatewayId() {
      return _gateway.getId();
    }

    public @Nonnull TransitGateway getGateway() {
      return _gateway;
    }

    public @Nonnull Region getRegion() {
      return _region;
    }
  }

  /**
   * Convert transit gateways in a way that keeps them unique, with one hostname per gateway, even
   * if they appear in multiple accounts. For each tgw, determine the account that owns it, use that
   * VS representation.
   */
  public static List<Configuration> convertTransitGateways(
      AwsConfiguration awsConfiguration, ConvertedConfiguration configs) {
    return getUniqueTransitGateways(
            collectGateways(awsConfiguration), awsConfiguration.getWarnings())
        .stream()
        .map(
            tgw ->
                tgw.getGateway()
                    .toConfigurationNode(
                        awsConfiguration, configs, tgw.getRegion(), awsConfiguration.getWarnings()))
        .collect(ImmutableList.toImmutableList());
  }

  @VisibleForTesting
  static Collection<TransitGatewayWithMetadata> getUniqueTransitGateways(
      Collection<TransitGatewayWithMetadata> gateways, Warnings warnings) {
    // Break into groups (by ID). Attempt to find an original gateway for each ID.
    // If we didn't get correct account from snapshot packaging, bail converting (for that one TGW).
    Map<String, List<TransitGatewayWithMetadata>> byId =
        gateways.stream()
            .collect(
                Collectors.groupingBy(
                    TransitGatewayWithMetadata::getTransitGatewayId, Collectors.toList()));
    List<TransitGatewayWithMetadata> result =
        byId.values().stream()
            .map(TransitGatewayConverter::findOriginalGateway)
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList());
    // If we couldn't find some authoritative TGWs, log a warning
    if (result.size() < byId.size()) {
      Set<String> missing =
          Sets.difference(
              byId.keySet(),
              result.stream()
                  .map(TransitGatewayWithMetadata::getTransitGatewayId)
                  .collect(Collectors.toSet()));
      warnings.redFlagf(
          "Could not find authoritative representation for transit gateways: %s",
          String.join(" ", missing));
    }
    return result;
  }

  /** Collect all transit gateways across regions and accounts */
  private static ImmutableList<TransitGatewayWithMetadata> collectGateways(
      AwsConfiguration awsConfiguration) {
    Builder<TransitGatewayWithMetadata> builder = ImmutableList.builder();
    for (Account account : awsConfiguration.getAccounts()) {
      for (Region region : account.getRegions()) {
        for (TransitGateway tgw : region.getTransitGateways().values()) {
          builder.add(new TransitGatewayWithMetadata(tgw, region, account.getId()));
        }
      }
    }
    return builder.build();
  }

  @VisibleForTesting
  static @Nullable TransitGatewayWithMetadata findOriginalGateway(
      Collection<TransitGatewayWithMetadata> tgws) {
    checkArgument(!tgws.isEmpty());
    checkArgument(
        tgws.stream().map(TransitGatewayWithMetadata::getTransitGatewayId).distinct().count() == 1);
    if (tgws.size() == 1) {
      // Short circuit if there is no de-duplication to do.
      return tgws.iterator().next();
    }
    return tgws.stream().filter(TransitGatewayWithMetadata::isOriginal).findFirst().orElse(null);
  }

  // prevent initialization
  private TransitGatewayConverter() {}
}
