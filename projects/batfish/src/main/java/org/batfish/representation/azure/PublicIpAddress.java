package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/**
 * Represents a Public Ip Address which can be used on several Azure services (Virtual Machines, Nat
 * gateways, Load balancers, Azure firewall etc..) <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/publicipaddresses?pivots=deployment-language-arm-template">...</a>
 *
 * <p>Partially implemented : - only ipv4 ip s supported
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicIpAddress extends Resource implements Serializable {

  private final @Nonnull Properties _properties;

  @JsonCreator
  public PublicIpAddress(
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nonnull String id,
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
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

    private final @Nonnull Ip _ipAddress;

    // todo type : can be static or dynamic

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_PUBLIC_IP_ADDRESS) @Nonnull Ip ipAddress) {
      checkArgument(ipAddress != null, "ipAddress must be provided");
      _ipAddress = ipAddress;
    }

    public @Nonnull Ip getIpAddress() {
      return _ipAddress;
    }
  }
}
