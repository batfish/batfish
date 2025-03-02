package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Postgres extends Instance implements Serializable {

    private final Properties _properties;

    @JsonCreator
    public Postgres(
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) Properties properties
    ){
        super(name, id, type);
        _properties = properties;
    }

    public Configuration toConfigurationNode(Region rgp, ConvertedConfiguration convertedConfiguration) {
        Configuration cfgNode = Configuration.builder()
                .setHostname(getCleanId())
                .setHumanName(getName())
                .setDomainName("azure")
                .setDeviceModel(DeviceModel.AZURE_POSTGRE)
                .setDefaultInboundAction(LineAction.PERMIT)
                .setDefaultCrossZoneAction(LineAction.PERMIT)
                .setConfigurationFormat(ConfigurationFormat.AZURE)
                .build();

        cfgNode.setDeviceType(DeviceType.HOST);

        Vrf.builder()
                .setName(Configuration.DEFAULT_VRF_NAME)
                .setOwner(cfgNode)
                .build();

        // assume db is in delegated mode
        if(getProperties() == null || getProperties().getNetwork() == null) {
            throw new UnsupportedOperationException("Missing required attribute in Postgre resource. \nid : "
            + getId());
        }

        String subnetId = getProperties().getNetwork().getDelegatedSubnetResourceId();
        if(subnetId == null) {
            throw new BatfishException("Postgres are only supported with a Delegated Subnet (Vnet integration).");
        }
        Subnet subnet = rgp.getSubnets().get(getProperties().getNetwork().getDelegatedSubnetResourceId());
        if(subnet == null) {
            throw new BatfishException("Subnet not found (did you forget to include it ?). id: "
                    + getProperties().getNetwork().getDelegatedSubnetResourceId());
        }

        Interface toSubnet = Interface.builder()
                .setName("to-subnet")
                .setHumanName(getName())
                .setAddress(
                        ConcreteInterfaceAddress.create(
                                // postgres is alone in the subnet (delegated ) so we can use a random ip.
                                Ip.create(subnet.getProperties().getAddressPrefix().getFirstHostIp().asLong() +1L),
                                subnet.getProperties().getAddressPrefix().getPrefixLength()
                        )
                )
                .setOwner(cfgNode)
                .setVrf(cfgNode.getDefaultVrf())
                .build();

        StaticRoute st = StaticRoute.builder()
                .setNextHopIp(subnet.computeInstancesIfaceIp())
                .setAdministrativeCost(0)
                .setMetric(0)
                .setNetwork(Prefix.ZERO)
                .build();

        cfgNode.getDefaultVrf().getStaticRoutes().add(st);

        convertedConfiguration.addLayer1Edge(
                subnet.getNodeName(), subnet.getToLanInterfaceName(),
                cfgNode.getHostname(), toSubnet.getName()
        );

        return cfgNode;
    }

    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable {
        private final Network _network;

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_POSTGRES_NETWORK) Network network)
        {
            _network = network;
        }

        public Network getNetwork() {
            return _network;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Network implements Serializable {
        // when db is in delegated subnet, we need to provide one exclusive subnet for this particular db
        // (network isolation and firewall)

        private final String _delegatedSubnetResourceId;

        @JsonCreator
        public Network(
                @JsonProperty(AzureEntities.JSON_KEY_POSTGRES_NETWORK_DELEGATED_SUBNET_ID) String delegatedSubnetResourceId
        ) {
            _delegatedSubnetResourceId = delegatedSubnetResourceId;
        }

        public String getDelegatedSubnetResourceId() {
            return _delegatedSubnetResourceId;
        }
    }
}
