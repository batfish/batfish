package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;

/**
 * Represents PostgreSQL flexibleServers azure service. <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.dbforpostgresql/flexibleservers?pivots=deployment-language-arm-template">Resource
 * link</a>
 *
 * <p>Partially implemented:
 * <li>only private access (Virtual Network integration)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Postgres extends Instance implements Serializable {

  private final @Nonnull Properties _properties;

  @JsonCreator
  public Postgres(
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nonnull String id,
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nonnull String type,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nonnull Properties properties) {
    super(name, id, type);
    checkArgument(properties != null, "properties must be provided");
    _properties = properties;
  }

  /** Generates Postgres {@link Configuration} node */
  @Override
  public Configuration toConfigurationNode(
      Region rgp, ConvertedConfiguration convertedConfiguration) {
    Configuration cfgNode =
        Configuration.builder()
            .setHostname(getCleanId())
            .setHumanName(getName())
            .setDomainName("azure")
            .setDeviceModel(DeviceModel.AZURE_POSTGRE)
            .setDefaultInboundAction(LineAction.PERMIT)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setConfigurationFormat(ConfigurationFormat.AZURE)
            .build();

    cfgNode.setDeviceType(DeviceType.HOST);

    Vrf.builder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(cfgNode).build();

    // assume db is in delegated mode
    String subnetId = getProperties().getNetwork().getDelegatedSubnetResourceId();
    if (subnetId == null) {
      throw new BatfishException(
          "Postgres are only supported with a Delegated Subnet (Vnet integration).");
    }
    Subnet subnet = rgp.findResource(subnetId, Subnet.class);

    Interface toSubnet =
        Interface.builder()
            .setName("subnet")
            .setHumanName(getName())
            .setAddress(
                ConcreteInterfaceAddress.create(
                    // postgres is alone in the subnet (delegated ) so we can use a random ip.
                    Ip.create(
                        subnet.getProperties().getAddressPrefix().getFirstHostIp().asLong() + 1L),
                    subnet.getProperties().getAddressPrefix().getPrefixLength()))
            .setOwner(cfgNode)
            .setVrf(cfgNode.getDefaultVrf())
            .setType(InterfaceType.PHYSICAL)
            .build();

    subnet.connectToHost(rgp, convertedConfiguration, cfgNode, toSubnet);

    cfgNode
        .getDefaultVrf()
        .getStaticRoutes()
        .add(
            StaticRoute.builder()
                .setNextHopIp(subnet.computeInstancesIfaceIp())
                .setAdministrativeCost(0)
                .setMetric(0)
                .setNetwork(Prefix.ZERO)
                .build());

    return cfgNode;
  }

  public @Nonnull Properties getProperties() {
    return _properties;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {
    private final @Nonnull Network _network;

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_POSTGRES_NETWORK) @Nonnull Network network) {
      checkArgument(network != null, "network must be provided");
      _network = network;
    }

    public @Nonnull Network getNetwork() {
      return _network;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Network implements Serializable {
    // when db is in delegated subnet, we need to provide one exclusive subnet for this particular
    // db
    // (network isolation and firewall)

    private final @Nonnull String _delegatedSubnetResourceId;

    @JsonCreator
    public Network(
        @JsonProperty(AzureEntities.JSON_KEY_POSTGRES_NETWORK_DELEGATED_SUBNET_ID) @Nonnull
            String delegatedSubnetResourceId) {
      checkArgument(
          delegatedSubnetResourceId != null, "delegatedSubnetResourceId must be provided");
      _delegatedSubnetResourceId = delegatedSubnetResourceId;
    }

    public @Nonnull String getDelegatedSubnetResourceId() {
      return _delegatedSubnetResourceId;
    }
  }
}
