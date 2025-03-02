package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static org.batfish.common.util.isp.IspModelingUtils.installRoutingPolicyAdvertiseStatic;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.IpProtocol.UDP;
import static org.batfish.datamodel.IpProtocol.ICMP;

import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NatGateway extends Resource implements Serializable {

    static final List<IpProtocol> NAT_PROTOCOLS = ImmutableList.of(TCP, UDP, ICMP);
    private final Properties _properties;

    public NatGateway(
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) Properties properties
    )
    {
        super(name, id, type);
        _properties = properties;
    }

    public String getNodeName() {
        return getCleanId();
    }

    public Configuration toConfigurationNode(Region region, ConvertedConfiguration convertedConfiguration) {

        Configuration cfgNode = Configuration.builder()
                .setHostname(getCleanId())
                .setConfigurationFormat(ConfigurationFormat.AZURE)
                .setDefaultCrossZoneAction(LineAction.PERMIT)
                .setDefaultInboundAction(LineAction.PERMIT)
                .setDomainName("azure")
                .build();

        Vrf.builder()
                .setName(Configuration.DEFAULT_VRF_NAME)
                .setOwner(cfgNode)
                .build();

        {   // Internet
            Interface toInternet = Interface.builder()
                    .setName(AzureConfiguration.BACKBONE_FACING_INTERFACE_NAME)
                    .setVrf(cfgNode.getDefaultVrf())
                    .setOwner(cfgNode)
                    .setType(InterfaceType.PHYSICAL)
                    .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
                    .build();

            BgpProcess process = BgpProcess.builder()
                    .setRouterId(AzureConfiguration.LINK_LOCAL_IP)
                    .setVrf(cfgNode.getDefaultVrf())
                    .setEbgpAdminCost(20)
                    .setIbgpAdminCost(200)
                    .setLocalAdminCost(200)
                    // arbitrary values below since does not export from BGP RIB
                    .setLocalOriginationTypeTieBreaker(LocalOriginationTypeTieBreaker.NO_PREFERENCE)
                    .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
                    .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
                    .build();

            BgpUnnumberedPeerConfig.builder()
                    .setPeerInterface(AzureConfiguration.BACKBONE_FACING_INTERFACE_NAME)
                    .setRemoteAs(AzureConfiguration.AZURE_BACKBONE_ASN)
                    .setLocalIp(AzureConfiguration.LINK_LOCAL_IP)
                    .setLocalAs(AzureConfiguration.AZURE_LOCAL_ASN)
                    .setBgpProcess(process)
                    .setIpv4UnicastAddressFamily(
                        Ipv4UnicastAddressFamily.builder().setExportPolicy(
                                AzureConfiguration.AWS_SERVICES_GATEWAY_EXPORT_POLICY_NAME).build())
                    .build();

            for(PublicIpAddressId id : getProperties().getPublicIpAddresses()){
                PublicIpAddress publicIpAddress = region.getPublicIpAddresses().get(id.getId());

                // todo : handle multiple public ips not in the same range
                toInternet.setOutgoingTransformation(
                        Transformation.when(
                                new MatchHeaderSpace(HeaderSpace.builder().setIpProtocols(NAT_PROTOCOLS).build())
                        ).apply(
                                TransformationStep.assignSourceIp(publicIpAddress.getProperties().getIpAddress()),
                                TransformationStep.assignSourcePort(1024,65525)
                        ).build()
                );

                // need to create a null route so we can advertise the prefix before sending traffic to the right host
                StaticRoute st = StaticRoute.builder()
                        .setNextHopInterface(NULL_INTERFACE_NAME)
                        .setNetwork(publicIpAddress.getProperties().getIpAddress().toPrefix())
                        .setAdministrativeCost(0)
                        .setMetric(0)
                        .setNonForwarding(true)
                        .build();

                cfgNode.getDefaultVrf().getStaticRoutes().add(st);

                PrefixSpace pf = new PrefixSpace();
                pf.addPrefix(publicIpAddress.getProperties().getIpAddress().toPrefix());
                installRoutingPolicyAdvertiseStatic(AzureConfiguration.AWS_SERVICES_GATEWAY_EXPORT_POLICY_NAME, cfgNode, pf);
            }


        }
        {   // Subnet
            Interface.builder()
                    .setName("subnet")
                    .setVrf(cfgNode.getDefaultVrf())
                    .setOwner(cfgNode)
                    .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
                    .build();
        }


        return cfgNode;
    }

    public Properties getProperties() {
        return _properties;
    }

    public static class Properties implements Serializable {
        private final Set<PublicIpAddressId> _publicIpAddresses;
        private final Set<PublicIpPrefixId>  _publicIpPrefixes;

        @JsonCreator
        public Properties(
                @JsonProperty("publicIpAddresses") Set<PublicIpAddressId> publicIpAddresses,
                @JsonProperty("publicIpPrefixes") Set<PublicIpPrefixId> publicIpPrefixes
        ) {
            _publicIpAddresses = publicIpAddresses;
            _publicIpPrefixes = publicIpPrefixes;
        }

        public Set<PublicIpAddressId> getPublicIpAddresses() {
            return _publicIpAddresses;
        }

        public Set<PublicIpPrefixId> getPublicIpPrefixes() {
            return _publicIpPrefixes;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublicIpAddressId implements Serializable {
        private final String _id;

        @JsonCreator
        public PublicIpAddressId(
                @JsonProperty(AzureEntities.JSON_KEY_ID) String id) {
            _id = id;
        }

        public String getId() {
            return _id;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublicIpPrefixId implements Serializable {
        private final String _id;

        @JsonCreator
        public PublicIpPrefixId(
                @JsonProperty(AzureEntities.JSON_KEY_ID) String id
        ) {
            _id = id;
        }

        public String getId() {
            return _id;
        }
    }
}
