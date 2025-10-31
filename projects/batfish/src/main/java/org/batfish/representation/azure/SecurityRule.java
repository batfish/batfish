package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/**
 * Represents a Security Rule : part of NetworkSecurityGroups (NSG) <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/networksecuritygroups/securityrules?pivots=deployment-language-arm-template">Resource
 * link</a> Partially implemented :
 * <li>Do not support service tags
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityRule extends Resource implements Serializable {

  private static final IpSpace VirtualNetworkIpSpace =
      AclIpSpace.union(
          Prefix.parse("10.0.0.0/8").toIpSpace(),
          Prefix.parse("172.16.0.0/20").toIpSpace(),
          Prefix.parse("192.168.0.0/16").toIpSpace());
  private static final IpSpace InternetIpSpace =
      AclIpSpace.difference(UniverseIpSpace.INSTANCE, VirtualNetworkIpSpace);

  private final @Nonnull Properties _properties;

  /**
   * Generates an AclLine from this {@link SecurityRule}
   *
   * @return ExprAclLine
   */
  public @Nonnull ExprAclLine getAclLine() {
    HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();

    headerSpaceBuilder.setDstIps(_properties.getDestinationIpSpace());
    headerSpaceBuilder.setSrcIps(_properties.getSourceIpSpace());

    if (_properties.getProtocol() != null) {
        headerSpaceBuilder.setIpProtocols(_properties.getProtocol());

        // handle port range(s) only if layer 4's protocol is TCP or UDP
        if ((_properties.getProtocol().equals(IpProtocol.TCP))
                || _properties.getProtocol().equals(IpProtocol.UDP)) {

            // if there are multiple port range, then we ignore single port range field

            if (_properties.getSourcePortRanges().isEmpty()) {
                headerSpaceBuilder.setSrcPorts(_properties.getSourcePortRange());
            } else {
                headerSpaceBuilder.setSrcPorts(_properties.getSourcePortRanges());
            }

            if (_properties.getDestinationPortRanges().isEmpty()) {
                headerSpaceBuilder.setDstPorts(_properties.getDestinationPortRange());
            } else {
                headerSpaceBuilder.setDstPorts(_properties.getDestinationPortRanges());
            }

        } else if (_properties.getProtocol().equals(IpProtocol.ICMP)) {
            headerSpaceBuilder.setIpProtocols(_properties.getProtocol());
        }
    }

    LineAction action =
        _properties.getAccess().equals("Allow") ? LineAction.PERMIT : LineAction.DENY;

    HeaderSpace headerSpace = headerSpaceBuilder.build();
    return ExprAclLine.builder()
        .setName(getName())
        .setMatchCondition(new MatchHeaderSpace(headerSpace))
        .setAction(action)
        .build();
  }

  @JsonCreator
  public SecurityRule(
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nonnull String id,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nonnull String type,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nonnull Properties properties) {
    super(name, id, type);
    checkArgument(properties != null, "properties must be provided");
    _properties = properties;
  }

  public @Nonnull Properties getProperties() {
    return _properties;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {

    private final @Nonnull IpProtocol _protocol;
    private final @Nonnull SubRange _sourcePortRange;
    private final @Nonnull SubRange _destinationPortRange;
    private final @Nullable String _sourceAddressPrefix;
    private final @Nullable String _destinationAddressPrefix;
    private final @Nonnull Set<String> _sourceAddressPrefixes;
    private final @Nonnull Set<String> _destinationAddressPrefixes;
    private final @Nonnull Set<SubRange> _sourcePortRanges;
    private final @Nonnull Set<SubRange> _destinationPortRanges;
    private final @Nonnull String _access;
    private final int _priority;
    private final @Nonnull String _direction;

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PORT) @Nullable String sourcePortRange,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PORT) @Nullable String destinationPortRange,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PORTS) @Nullable Set<String> sourcePortRanges,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PORTS) @Nullable
            Set<String> destinationPortRanges,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PREFIX) @Nullable String sourceAddressPrefix,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PREFIX) @Nullable
            String destinationAddressPrefix,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PREFIXES) @Nullable
            Set<String> sourceAddressPrefixes,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PREFIXES) @Nullable
            Set<String> destinationAddressPrefixes,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_PROTOCOL) @Nonnull String protocol,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_ACCESS) @Nonnull String access,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_PRIORITY) @Nonnull Integer priority,
        @JsonProperty(AzureEntities.JSON_KEY_NSG_DIRECTION) @Nonnull String direction) {
      checkArgument(protocol != null, "protocol must be provided");
      checkArgument(access != null, "access must be provided");
      checkArgument(priority != null, "priority must be provided");
      checkArgument(direction != null, "direction must be provided");

      _sourcePortRange = getSubRange(sourcePortRange);
      _destinationPortRange = getSubRange(destinationPortRange);
      _sourceAddressPrefix = sourceAddressPrefix;
      _destinationAddressPrefix = destinationAddressPrefix;
      _sourceAddressPrefixes = Optional.ofNullable(sourceAddressPrefixes).orElseGet(HashSet::new);
      _destinationAddressPrefixes =
          Optional.ofNullable(destinationAddressPrefixes).orElseGet(HashSet::new);

      _protocol = getProtocol(protocol);
      _access = access;
      _priority = priority;
      _direction = direction;

      _sourcePortRanges =
          Optional.ofNullable(sourcePortRanges)
              .map(
                  ranges ->
                      ranges.stream().map(Properties::getSubRange).collect(Collectors.toSet()))
              .orElseGet(HashSet::new);

      _destinationPortRanges =
          Optional.ofNullable(destinationPortRanges)
              .map(
                  ranges ->
                      ranges.stream().map(Properties::getSubRange).collect(Collectors.toSet()))
              .orElseGet(HashSet::new);
    }

    /** convert {@link String} to {@link IpProtocol} */
    private static @Nullable IpProtocol getProtocol(String protocol) {
      if (protocol.equals("*")) {
        return null;
      }
      return IpProtocol.fromString(protocol);
    }

    /** convert {@link String} to {@link SubRange} */
    private static @Nonnull SubRange getSubRange(String subRange) {
      if (subRange == null || subRange.equals("*")) {
        return new SubRange(0, 65535);
      }

      try {
        return new SubRange(Integer.parseInt(subRange));
      } catch (NumberFormatException e) {
        return new SubRange(subRange);
      }
    }

    /**
     * Parse String prefix, and return appropriate IpSpace if found service tag. (Supported service
     * tag : Internet, VirtualNetwork)
     */
    private static @Nonnull IpSpace parseIpSpace(String prefix) {
      if (prefix == null || prefix.equals("*")) {
        return UniverseIpSpace.INSTANCE;
      } else if (prefix.equals("Internet")) {
        return InternetIpSpace;
      } else if (prefix.equals("VirtualNetwork")) {
        return VirtualNetworkIpSpace;
      }

      try {
        return Prefix.parse(prefix).toIpSpace();
      } catch (IllegalArgumentException e) {
        // no known service tag has been found
        // nor regular prefix
        return EmptyIpSpace.INSTANCE;
      }
    }

    public @Nullable IpProtocol getProtocol() {
      return _protocol;
    }

    public @Nonnull SubRange getSourcePortRange() {
      return _sourcePortRange;
    }

    public @Nonnull SubRange getDestinationPortRange() {
      return _destinationPortRange;
    }

    public @Nullable String getSourceAddressPrefix() {
      return _sourceAddressPrefix;
    }

    public @Nonnull Set<String> getSourceAddressPrefixes() {
      return _sourceAddressPrefixes;
    }

    public @Nullable String getDestinationAddressPrefix() {
      return _destinationAddressPrefix;
    }

    public @Nonnull Set<String> getDestinationAddressPrefixes() {
      return _destinationAddressPrefixes;
    }

    /**
     * Returns ipSpace of prefix and prefixes combined (handles null prefixes)
     *
     * @return {@link IpSpace}
     */
    private @Nonnull IpSpace ipSpaceFromPrefixes(String prefix, Set<String> prefixes) {
      if (prefixes == null || prefixes.isEmpty()) {
        return AclIpSpace.union(EmptyIpSpace.INSTANCE, parseIpSpace(prefix));
      }

      IpSpace collectedIpSpace =
          AclIpSpace.union(
              prefixes.stream()
                  .filter(Objects::nonNull)
                  .map(Properties::parseIpSpace)
                  .collect(Collectors.toSet()));

      return AclIpSpace.union(collectedIpSpace, parseIpSpace(prefix));
    }

    public @Nonnull IpSpace getDestinationIpSpace() {
      return ipSpaceFromPrefixes(_destinationAddressPrefix, _destinationAddressPrefixes);
    }

    public @Nonnull IpSpace getSourceIpSpace() {
      return ipSpaceFromPrefixes(_sourceAddressPrefix, _sourceAddressPrefixes);
    }

    public @Nonnull String getAccess() {
      return _access;
    }

    public int getPriority() {
      return _priority;
    }

    public @Nonnull String getDirection() {
      return _direction;
    }

    public @Nonnull Set<SubRange> getSourcePortRanges() {
      return _sourcePortRanges;
    }

    public @Nonnull Set<SubRange> getDestinationPortRanges() {
      return _destinationPortRanges;
    }
  }
}
