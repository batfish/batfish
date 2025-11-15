package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Represents an ipConfiguration (part of {@link NetworkInterface} object). <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/networkinterfaces/ipconfigurations?pivots=deployment-language-arm-template">Link</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IPConfiguration extends Resource {

  private final @Nonnull Properties _properties;

  @JsonCreator
  IPConfiguration(
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nonnull String type,
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nonnull String id,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nonnull Properties properties) {
    super(name, id, type);
    checkArgument(properties != null, "properties must be provided");
    _properties = properties;
  }

  public void advertisePublicIpIfAny(
      Region region, ConvertedConfiguration convertedConfiguration, NatGateway natGateway) {
    if (getProperties().getPublicIpAddressId() == null) {
      return;
    }
    PublicIpAddress publicIpAddress =
        region.findResource(getProperties().getPublicIpAddressId(), PublicIpAddress.class);
    natGateway.handleHostPublicIp(
        convertedConfiguration, publicIpAddress, getProperties().getPrivateIpAddress());
  }

  public @Nonnull Properties getProperties() {
    return _properties;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {

    private final @Nonnull Ip _privateIpAddress;
    private final @Nonnull IdReference _subnet;
    private final @Nullable IdReference _publicIpAddress;
    private final boolean _primary;

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_PRIVATE_IP_ADDRESS) @Nonnull
            Ip privateIpAddress,
        @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_SUBNET) @Nonnull IdReference subnet,
        @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_PUBLIC_IP_ADDRESS) @Nullable
            IdReference publicIpAddress,
        @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_PRIMARY) @Nonnull Boolean primary) {
      checkArgument(privateIpAddress != null, "privateIpAddress must be provided");
      checkArgument(subnet != null, "subnet must be provided");
      checkArgument(primary != null, "primary must be provided");
      _privateIpAddress = privateIpAddress;
      _subnet = subnet;
      _publicIpAddress = publicIpAddress;
      _primary = primary;
    }

    public @Nullable Ip getPrivateIpAddress() {
      return _privateIpAddress;
    }

    public @Nullable String getPublicIpAddressId() {
      return _publicIpAddress == null ? null : _publicIpAddress.getId();
    }

    public @Nonnull String getSubnetId() {
      return _subnet.getId();
    }

    public boolean isPrimary() {
      return _primary;
    }
  }
}
