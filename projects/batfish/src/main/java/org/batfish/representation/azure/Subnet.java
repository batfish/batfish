package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.route.nh.NextHop;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents an Azure subnet (part of VNet object).
 * <a href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/virtualnetworks/subnets?pivots=deployment-language-arm-template">Resource link</a>
 * Do not support UDR.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Subnet extends Resource implements Serializable {

    final private @Nonnull Properties _properties;

    @JsonCreator
    public Subnet(
         @JsonProperty(AzureEntities.JSON_KEY_ID) @Nullable String id,
         @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nullable String name,
         @JsonProperty(AzureEntities.JSON_KEY_TYPE)  @Nullable String type,
         @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nullable Properties properties
    ){
        super(name, id, type);
        checkArgument(properties != null, "properties must be provided");
        _properties = properties;
    }

    public Ip computeInstancesIfaceIp(){
        // first 3 ips are reserved in an azure subnet
        return _properties.getAddressPrefix().getFirstHostIp();
    }

    public String getNodeName(){
        return getCleanId();
    }

    public String getToLanInterfaceName(){
        return "to-lan";
    }
    public String getToVnetInterfaceName(){
        return "to-vnet" ;
    }
    public String getToNatInterfaceName(){
        return "nat-gateway";
    }


    /**
     * Returns the {@link Configuration} node for this subnet.
     * <p> Creates Interfaces (with layer1Edges) to connect to {@link VNet}, {@link NatGateway}
     * and {@link Instance} objects. add StaticRoutes for VNet and Internet access.
     * (If no natGateways exists, creates one for handling VM's public ips).
     * </p>
     */
    public Configuration toConfigurationNode(Region rgp, ConvertedConfiguration convertedConfiguration){

        Configuration cfgNode = Configuration.builder()
                .setHumanName(getName())
                .setHostname(getNodeName())
                .setDomainName("azure")
                .setConfigurationFormat(ConfigurationFormat.AZURE)
                .setDefaultCrossZoneAction(LineAction.PERMIT)
                .setDefaultInboundAction(LineAction.PERMIT)
                .setDeviceModel(DeviceModel.AZURE_SUBNET)
                .build();

        Vrf.builder()
                .setName(Configuration.DEFAULT_VRF_NAME)
                .setOwner(cfgNode)
                .build();

        { // LAN
            Ip instancesIfaceIp = computeInstancesIfaceIp();
            ConcreteInterfaceAddress instancesIfaceAddress =
                    ConcreteInterfaceAddress.create(instancesIfaceIp, _properties.getAddressPrefix().getPrefixLength());

            Interface lanInterface = Interface.builder()
                    .setAddress(instancesIfaceAddress)
                    .setName(getToLanInterfaceName())
                    .setOwner(cfgNode)
                    .setVrf(cfgNode.getDefaultVrf())
                    .build();

            // ACL
            {
                String nsgId = _properties.getNetworkSecurityGroupId();
                if (nsgId != null) {
                    NetworkSecurityGroup nsg =
                            rgp.getNetworkSecurityGroups().get(nsgId);

                    if (nsg == null) {
                        throw new BatfishException(String.format("Unable to apply the NSG %s on subnet %s.\n" +
                                "Missing nsg file !", nsgId, getCleanId()));
                    }

                        nsg.applyToInterface(lanInterface);
                }
            }
        }

        { // VNet

            Interface.builder()
                    .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
                    .setVrf(cfgNode.getDefaultVrf())
                    .setOwner(cfgNode)
                    .setName(getToVnetInterfaceName())
                    .setDescription("to vnet interface")
                    .build();

            // static route set from Vnet object (no knowledge of Vnet addressSpace nor Vnet peering s)
        }

        { // Nat gateway
            Interface toNat = Interface.builder()
                    .setVrf(cfgNode.getDefaultVrf())
                    .setOwner(cfgNode)
                    .setName(getToNatInterfaceName())
                    .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
                    .setDescription("to nat gateway")
                    .build();

            StaticRoute st = StaticRoute.builder()
                    .setNetwork(Prefix.ZERO)
                    .setNonForwarding(false)
                    .setNextHop(NextHop.legacyConverter(toNat.getName(), AzureConfiguration.LINK_LOCAL_IP))
                    .setAdministrativeCost(0)
                    .setMetric(0)
                    .build();

            cfgNode.getDefaultVrf().getStaticRoutes().add(st);

            Configuration natGatewayNode;
            String natGatewayId = getProperties().getNatGatewayId();

            // if subnet doesn't have a nat gateway.
            // generates one for handling vm's public ips.
            if (natGatewayId == null) {
                natGatewayId = getId() + "/internet-gateway";

                NatGateway natGateway = new NatGateway(natGatewayId, getName() + "-internet-gateway",
                        AzureEntities.JSON_TYPE_NAT_GATEWAY, getId());

                rgp.getNatGateways().put(natGatewayId, natGateway);

                getProperties().setNatGatewayId(natGatewayId);
                natGatewayNode = natGateway.toConfigurationNode(rgp, convertedConfiguration);
                convertedConfiguration.addNode(natGatewayNode);

                return cfgNode;
            }
        }

        return cfgNode;
    }

    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable{

        final private @Nonnull Prefix _addressPrefix;
        final private @Nullable IdReference _nsg;
        private String _natGatewayId;
        final private @Nonnull Set<IdReference> _ipConfigurations;
        final private boolean _defaultOutboundAccess;

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_SUBNET_ADDRESS_PREFIX) @Nullable Prefix addressPrefix,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_NGS) @Nullable IdReference nsg,
                @JsonProperty(AzureEntities.JSON_KEY_SUBNET_NAT_GATEWAY) @Nullable String natGatewayId,
                @JsonProperty(AzureEntities.JSON_KEY_SUBNET_IP_CONFIGURATIONS) @Nullable Set<IdReference> ipConfigurations,
                @JsonProperty("defaultOutboundAccess") @Nullable Boolean defaultOutboundAccess
        ){
            checkArgument(addressPrefix != null, "addressPrefix must be provided");
            if (ipConfigurations == null) ipConfigurations = new HashSet<>();
            if (defaultOutboundAccess == null) defaultOutboundAccess = false;
            _addressPrefix = addressPrefix;
            _nsg = nsg;
            _natGatewayId = natGatewayId;
            _ipConfigurations = ipConfigurations;
            _defaultOutboundAccess = defaultOutboundAccess;
        }

        public Prefix getAddressPrefix() {
            return _addressPrefix;
        }

        public String getNetworkSecurityGroupId(){
            if (_nsg == null) return null;
            return _nsg.getId();
        }

        public String getNatGatewayId() {
            return _natGatewayId;
        }
        public void setNatGatewayId(String natGatewayId) {
            _natGatewayId = natGatewayId;
        }

        public Set<IdReference> getIpConfigurations() {
            return _ipConfigurations;
        }

        // todo: handle vm default outbound access if this parameter is true
        // idea : insert a publicIp in every vm ipConfiguration (before node generation and after parsing)
        public boolean getDefaultOutboundAccess() {
            return _defaultOutboundAccess;
        }
    }

}
