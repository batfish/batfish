package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.ACCEPT_ALL_BGP;
import static org.batfish.representation.aws.Utils.ACCEPT_ALL_BGP_AND_STATIC;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.connect;
import static org.batfish.representation.aws.Utils.suffixedInterfaceName;
import static org.batfish.representation.aws.Utils.toStaticRoute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.TransitGatewayPropagations.Propagation;

/**
 * Represents an AWS Transit Gateway
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-transit-gateways.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class TransitGateway implements AwsVpcEntity, Serializable {

  @Nonnull
  public TransitGatewayOptions getOptions() {
    return _options;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class TransitGatewayOptions implements Serializable {

    private final long _amazonSideAsn;

    private final boolean _defaultRouteTableAssociation;

    @Nonnull private final String _associationDefaultRouteTableId;

    private final boolean _defaultRouteTablePropagation;

    @Nonnull private final String _propagationDefaultRouteTableId;

    private final boolean _vpnEcmpSupport;

    //    "Options": {
    //          "AmazonSideAsn": 64512,
    //          "AutoAcceptSharedAttachments": "disable",
    //          "DefaultRouteTableAssociation": "enable",
    //          "AssociationDefaultRouteTableId": "tgw-rtb-0fa40c8df355dce6e",
    //          "DefaultRouteTablePropagation": "enable",
    //          "PropagationDefaultRouteTableId": "tgw-rtb-0fa40c8df355dce6e",
    //          "VpnEcmpSupport": "enable",
    //          "DnsSupport": "enable"
    //    },
    @JsonCreator
    private static TransitGatewayOptions create(
        @Nullable @JsonProperty(JSON_KEY_AMAZON_SIDE_ASN) Long amazonSideAsn,
        @Nullable @JsonProperty(JSON_KEY_DEFAULT_ROUTE_TABLE_ASSOCIATION)
            String defaultRouteTableAssociation,
        @Nullable @JsonProperty(JSON_KEY_ASSOCIATION_DEFAULT_ROUTE_TABLE_ID)
            String associationDefaultRouteTableId,
        @Nullable @JsonProperty(JSON_KEY_DEFAULT_ROUTE_TABLE_PROPAGATION)
            String defaultRouteTablePropagation,
        @Nullable @JsonProperty(JSON_KEY_PROPAGATION_DEFAULT_ROUTE_TABLE_ID)
            String propagationDefaultRouteTableId,
        @Nullable @JsonProperty(JSON_KEY_VPN_ECMP_SUPPORT) String vpcEcmpSupport) {
      checkArgument(amazonSideAsn != null, "Amazon side ASN cannot be null for a transit gateway");
      checkArgument(
          defaultRouteTableAssociation != null,
          "Default route table association cannot be null for a transit gateway");
      checkArgument(
          associationDefaultRouteTableId != null,
          "Association default route table id cannot be null for a transit gateway");
      checkArgument(
          defaultRouteTablePropagation != null,
          "Default route table propagation cannot be null for a transit gateway");
      checkArgument(
          propagationDefaultRouteTableId != null,
          "Propagation default route table id cannot be null for a transit gateway");
      checkArgument(
          vpcEcmpSupport != null, "VPC ECMP support cannot be null for a transit gateway");

      return new TransitGatewayOptions(
          amazonSideAsn,
          getBool(defaultRouteTableAssociation),
          associationDefaultRouteTableId,
          getBool(defaultRouteTablePropagation),
          propagationDefaultRouteTableId,
          getBool(vpcEcmpSupport));
    }

    TransitGatewayOptions(
        long amazonSideAsn,
        boolean defaultRouteTableAssociation,
        String associationDefaultRouteTableId,
        boolean defaultRouteTablePropagation,
        String propagationDefaultRouteTableId,
        boolean vpnEcmpSupport) {
      _amazonSideAsn = amazonSideAsn;
      _defaultRouteTableAssociation = defaultRouteTableAssociation;
      _associationDefaultRouteTableId = associationDefaultRouteTableId;
      _defaultRouteTablePropagation = defaultRouteTablePropagation;
      _propagationDefaultRouteTableId = propagationDefaultRouteTableId;
      _vpnEcmpSupport = vpnEcmpSupport;
    }

    private static boolean getBool(String stringValue) {
      if (stringValue.equalsIgnoreCase("enable")) {
        return true;
      }
      if (stringValue.equalsIgnoreCase("disable")) {
        return false;
      }
      throw new IllegalArgumentException(
          String.format("'%s' is not a valid boolean value", stringValue));
    }

    public long getAmazonSideAsn() {
      return _amazonSideAsn;
    }

    public boolean isDefaultRouteTableAssociation() {
      return _defaultRouteTableAssociation;
    }

    @Nonnull
    public String getAssociationDefaultRouteTableId() {
      return _associationDefaultRouteTableId;
    }

    public boolean isDefaultRouteTablePropagation() {
      return _defaultRouteTablePropagation;
    }

    @Nonnull
    public String getPropagationDefaultRouteTableId() {
      return _propagationDefaultRouteTableId;
    }

    public boolean isVpnEcmpSupport() {
      return _vpnEcmpSupport;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TransitGatewayOptions)) {
        return false;
      }
      TransitGatewayOptions that = (TransitGatewayOptions) o;
      return _amazonSideAsn == that._amazonSideAsn
          && _defaultRouteTableAssociation == that._defaultRouteTableAssociation
          && _defaultRouteTablePropagation == that._defaultRouteTablePropagation
          && _vpnEcmpSupport == that._vpnEcmpSupport
          && com.google.common.base.Objects.equal(
              _associationDefaultRouteTableId, that._associationDefaultRouteTableId)
          && com.google.common.base.Objects.equal(
              _propagationDefaultRouteTableId, that._propagationDefaultRouteTableId);
    }

    @Override
    public int hashCode() {
      return com.google.common.base.Objects.hashCode(
          _amazonSideAsn,
          _defaultRouteTableAssociation,
          _associationDefaultRouteTableId,
          _defaultRouteTablePropagation,
          _propagationDefaultRouteTableId,
          _vpnEcmpSupport);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("_amazonSideAsn", _amazonSideAsn)
          .add("_defaultRouteTableAssociation", _defaultRouteTableAssociation)
          .add("_associationDefaultRouteTableId", _associationDefaultRouteTableId)
          .add("_defaultRouteTablePropagation", _defaultRouteTablePropagation)
          .add("_propagationDefaultRouteTableId", _propagationDefaultRouteTableId)
          .add("_vpnEcmpSupport", _vpnEcmpSupport)
          .toString();
    }
  }

  @Nonnull private final String _gatewayId;

  @Nonnull private final TransitGatewayOptions _options;

  @JsonCreator
  private static TransitGateway create(
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) String gatewayId,
      @Nullable @JsonProperty(JSON_KEY_OPTIONS) TransitGatewayOptions options) {
    checkArgument(gatewayId != null, "Transit Gateway Id cannot be null");
    checkArgument(options != null, "Transit Gateway Options cannot be null");

    return new TransitGateway(gatewayId, options);
  }

  public TransitGateway(String gatewayId, TransitGatewayOptions options) {
    _gatewayId = gatewayId;
    _options = options;
  }

  /** Creates a node for the transit gateway. */
  Configuration toConfigurationNode(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(nodeName(_gatewayId), "aws");
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    // make connections to the attachments
    region.getTransitGatewayAttachments().values().stream()
        .filter(a -> a.getGatewayId().equals(_gatewayId))
        .forEach(a -> connectAttachment(cfgNode, a, awsConfiguration, region, warnings));

    Set<TransitGatewayRouteTable> routeTables =
        region.getTransitGatewayRouteTables().values().stream()
            .filter(routeTable -> routeTable.getGatewayId().equals(_gatewayId))
            .collect(ImmutableSet.toImmutableSet());

    // propagate routes
    routeTables.forEach(
        table ->
            region
                .getTransitGatewayPropagations()
                .getOrDefault(
                    table.getId(), new TransitGatewayPropagations("dummy", ImmutableList.of()))
                .getPropagations()
                .forEach(
                    propagation ->
                        propagateRoutes(
                            cfgNode, table, propagation, awsConfiguration, region, warnings)));

    // add static routes that were configured for route tables
    region.getTransitGatewayRouteTables().values().stream()
        .filter(
            table ->
                table.getGatewayId().equals(_gatewayId)
                    && region.getTransitGatewayStaticRoutes().containsKey(table.getId()))
        .forEach(
            table ->
                region.getTransitGatewayStaticRoutes().get(table.getId()).getRoutes().stream()
                    .filter(route -> route instanceof TransitGatewayRouteV4)
                    .forEach(
                        route ->
                            addTransitGatewayStaticRoute(
                                cfgNode,
                                table,
                                (TransitGatewayRouteV4) route,
                                awsConfiguration,
                                region,
                                warnings)));

    return cfgNode;
  }

  private static void connectAttachment(
      Configuration tgwCfg,
      TransitGatewayAttachment attachment,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    switch (attachment.getResourceType()) {
      case VPC:
        {
          Optional<TransitGatewayVpcAttachment> vpcAttachment =
              region.findTransitGatewayVpcAttachment(
                  attachment.getResourceId(), tgwCfg.getHostname());
          if (!vpcAttachment.isPresent()) {
            warnings.redFlag(
                String.format(
                    "VPC attachment %s not found for transit gateway %s",
                    attachment.getResourceId(), tgwCfg.getHostname()));
            return;
          }
          connectVpc(tgwCfg, attachment, awsConfiguration, region, warnings);
          return;
        }
      case VPN:
        {
          Optional<VpnConnection> vpnConnection =
              region.findTransitGatewayVpnConnection(
                  attachment.getResourceId(), tgwCfg.getHostname());
          if (!vpnConnection.isPresent()) {
            warnings.redFlag(
                String.format(
                    "VPN connection %s for transit gateway %s",
                    attachment.getResourceId(), tgwCfg.getHostname()));
            return;
          }
          connectVpn(tgwCfg, attachment, vpnConnection.get(), awsConfiguration, region, warnings);
          return;
        }
      default:
        warnings.redFlag(
            "Unsupported resource type in transit gateway attachment: "
                + attachment.getResourceType());
    }
  }

  private static void connectVpc(
      Configuration tgwCfg,
      TransitGatewayAttachment attachment,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {

    // we don't need to look beyond the region; all VPCs attached to the transit gateway should be
    // in the same region
    Vpc vpc = region.getVpcs().get(attachment.getResourceId());
    if (vpc == null) {
      warnings.redFlag(
          String.format(
              "VPC %s for attachment %s not found in region %s",
              attachment.getResourceId(), attachment.getId(), region.getName()));
      return;
    }
    if (!attachment.getAssociation().getState().equals(STATE_ASSOCIATED)) {
      warnings.redFlag(
          String.format(
              "Skipped VPC %s as attachment because it is in (non-associated) state '%s'",
              attachment.getResourceId(), attachment.getAssociation().getState()));
      return;
    }

    Configuration vpcCfg = awsConfiguration.getConfigurationNodes().get(Vpc.nodeName(vpc.getId()));

    String vrfNameOnVpc = Vpc.vrfNameForLink(attachment.getId());
    String vrfNameOnTgw = vrfNameForRouteTable(attachment.getAssociation().getRouteTableId());

    if (!vpcCfg.getVrfs().containsKey(vrfNameOnVpc)) {
      Vrf vrf = Vrf.builder().setOwner(vpcCfg).setName(vrfNameOnVpc).build();
      vpc.initializeVrf(vrf);
    }

    // the VRF will exist if this routing table has been encountered before
    if (!tgwCfg.getVrfs().containsKey(vrfNameOnTgw)) {
      Vrf.builder().setOwner(tgwCfg).setName(vrfNameOnTgw).build();
    }

    connect(awsConfiguration, tgwCfg, vrfNameOnTgw, vpcCfg, vrfNameOnVpc, attachment.getId());

    addStaticRoute(
        vpcCfg.getVrfs().get(vrfNameOnVpc),
        toStaticRoute(
            Prefix.ZERO,
            Utils.getInterfaceIp(tgwCfg, suffixedInterfaceName(vpcCfg, attachment.getId()))));
  }

  private static void connectVpn(
      Configuration tgwCfg,
      TransitGatewayAttachment attachment,
      VpnConnection vpnConnection,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {

    String vrfName = vrfNameForRouteTable(attachment.getAssociation().getRouteTableId());
    if (!tgwCfg.getVrfs().containsKey(vrfName)) {
      Vrf.builder().setOwner(tgwCfg).setName(vrfName).build();
    }
    Vrf vrf = tgwCfg.getVrfs().get(vrfName);

    if (vpnConnection.isBgpConnection()) {
      Optional<String> unsupported =
          supportedVpnBgpConfiguration(attachment, vpnConnection, region);
      if (unsupported.isPresent()) {
        warnings.redFlag(unsupported.get());
        return;
      }
      if (vrf.getBgpProcess() == null) {
        createBgpProcess(tgwCfg, vrf, awsConfiguration);
      }
    }

    vpnConnection.applyToGateway(
        tgwCfg,
        tgwCfg.getVrfs().get(vrfName),
        bgpExportPolicyName(vrfName),
        bgpImportPolicyName(vrfName),
        warnings);
  }

  /**
   * Currently, we only support VPN attachments that use the same table for association and
   * propagation.
   *
   * @return Optional.empty if supported; explanatory message otherwise
   */
  @VisibleForTesting
  static Optional<String> supportedVpnBgpConfiguration(
      TransitGatewayAttachment attachment, VpnConnection vpnConnection, Region region) {
    String associatedRoutingTableId = attachment.getAssociation().getRouteTableId();
    Set<String> propagatedRoutingTableIds =
        region.getTransitGatewayPropagations().values().stream()
            .filter(
                props ->
                    props.getPropagations().stream()
                        .anyMatch(
                            prop ->
                                prop.isEnabled()
                                    && prop.getResourceId().equals(vpnConnection.getId())))
            .map(TransitGatewayPropagations::getId)
            .collect(ImmutableSet.toImmutableSet());
    if (propagatedRoutingTableIds.equals(ImmutableSet.of(associatedRoutingTableId))) {
      return Optional.empty();
    } else {
      return Optional.of(
          String.format(
              "Unsupported VPN attachment configuration for %s. Association route table = %s. Propagation route tables = %s",
              vpnConnection.getId(), associatedRoutingTableId, propagatedRoutingTableIds));
    }
  }

  @VisibleForTesting
  static void createBgpProcess(
      Configuration tgwCfg, Vrf vrf, ConvertedConfiguration awsConfiguration) {
    ConcreteInterfaceAddress loopbackBgpAddress =
        ConcreteInterfaceAddress.create(
            awsConfiguration.getNextGeneratedLinkSubnet().getStartIp(), Prefix.MAX_PREFIX_LENGTH);
    Utils.newInterface(
        "bgp-loopback-" + vrf.getName(),
        tgwCfg,
        vrf.getName(),
        loopbackBgpAddress,
        "BGP loopback for " + vrf.getName());

    BgpProcess proc =
        BgpProcess.builder()
            .setRouterId(loopbackBgpAddress.getIp())
            .setVrf(vrf)
            .setAdminCostsToVendorDefaults(ConfigurationFormat.AWS)
            .build();
    // TODO: check if vpn ecmp support setting in transit gateway has an impact here
    proc.setMultipathEquivalentAsPathMatchMode(MultipathEquivalentAsPathMatchMode.EXACT_PATH);

    // TODO: confirm if this is the policy we really want
    RoutingPolicy.builder()
        .setName(bgpExportPolicyName(vrf.getName()))
        .setOwner(tgwCfg)
        .setStatements(Collections.singletonList(ACCEPT_ALL_BGP_AND_STATIC))
        .build();

    RoutingPolicy.builder()
        .setName(bgpImportPolicyName(vrf.getName()))
        .setOwner(tgwCfg)
        .setStatements(Collections.singletonList(ACCEPT_ALL_BGP))
        .build();
  }

  private static void propagateRoutes(
      Configuration tgwCfg,
      TransitGatewayRouteTable table,
      Propagation propagation,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    if (!propagation.isEnabled()) {
      return;
    }
    switch (propagation.getResourceType()) {
      case VPC:
        // need to look only in region; all VPCs attached to the gateway should have same region
        Vpc vpc = region.getVpcs().get(propagation.getResourceId());
        if (vpc == null) {
          warnings.redFlag(
              String.format(
                  "VPC %s for propagating attachment %s not found in region %s",
                  propagation.getResourceId(), propagation.getAttachmentId(), region.getName()));
          return;
        }
        propagateRoutesVpc(
            tgwCfg, table, propagation.getAttachmentId(), vpc, awsConfiguration, warnings);
        return;
      case VPN:
        /*
        For currently supported configurations, we don't need to do anything here.
        (We warn about lack of support in connectVpn.)
        */
        return;
      default:
        warnings.redFlag(
            String.format(
                "Resource type %s for transit gateway route propagation",
                propagation.getResourceType()));
    }
  }

  private static void propagateRoutesVpc(
      Configuration tgwCfg,
      TransitGatewayRouteTable table,
      String attachmentId,
      Vpc vpc,
      ConvertedConfiguration awsConfiguration,
      Warnings warnings) {

    Configuration vpcCfg = awsConfiguration.getConfigurationNodes().get(Vpc.nodeName(vpc.getId()));
    if (vpcCfg == null) {
      warnings.redFlag(String.format("VPC configuration node for VPC %s not found", vpc.getId()));
      return;
    }

    Interface localIface =
        tgwCfg.getAllInterfaces().get(suffixedInterfaceName(vpcCfg, attachmentId));
    if (localIface == null) {
      warnings.redFlag(String.format("Interface facing VPC %s not found on TGW", vpc.getId()));
      return;
    }

    Interface remoteIface =
        vpcCfg.getAllInterfaces().get(suffixedInterfaceName(tgwCfg, attachmentId));
    if (remoteIface == null) {
      warnings.redFlag(String.format("Interface facing TGW not found on VPC %s", vpc.getId()));
      return;
    }

    String vrfName = vrfNameForRouteTable(table.getId());
    if (!tgwCfg.getVrfs().containsKey(vrfName)) {
      Vrf.builder().setOwner(tgwCfg).setName(vrfName).build();
    }
    vpc.getCidrBlockAssociations()
        .forEach(
            pfx ->
                addStaticRoute(
                    tgwCfg.getVrfs().get(vrfName),
                    toStaticRoute(
                        pfx, localIface.getName(), remoteIface.getConcreteAddress().getIp())));
  }

  private void addTransitGatewayStaticRoute(
      Configuration tgwCfg,
      TransitGatewayRouteTable routeTable,
      TransitGatewayRouteV4 route,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    String vrfName = vrfNameForRouteTable(routeTable.getId());
    if (!tgwCfg.getVrfs().containsKey(vrfName)) {
      Vrf.builder().setOwner(tgwCfg).setName(vrfName).build();
    }
    Vrf vrf = tgwCfg.getVrfs().get(vrfName);
    if (route.getState() == State.BLACKHOLE) {
      addStaticRoute(
          vrf, toStaticRoute(route.getDestinationCidrBlock(), Interface.NULL_INTERFACE_NAME));
      return;
    }
    route
        .getAttachmentIds()
        .forEach(
            attachmentId ->
                addTransitGatewayStaticRouteAttachment(
                    tgwCfg, vrf, route, attachmentId, awsConfiguration, region, warnings));
  }

  private void addTransitGatewayStaticRouteAttachment(
      Configuration tgwCfg,
      Vrf vrf,
      TransitGatewayRouteV4 route,
      String attachmentId,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    TransitGatewayAttachment tgwAttachment =
        region.findTransitGatewayAttachment(attachmentId, _gatewayId).orElse(null);
    if (tgwAttachment == null) {
      warnings.redFlag(
          String.format(
              "Transit gateway attachment %s not found for route %s", attachmentId, route));
      return;
    }
    switch (tgwAttachment.getResourceType()) {
      case VPC:
        {
          Configuration vpcCfg =
              awsConfiguration
                  .getConfigurationNodes()
                  .get(Vpc.nodeName(tgwAttachment.getResourceId()));
          addStaticRoute(
              vrf,
              toStaticRoute(
                  route.getDestinationCidrBlock(),
                  Utils.getInterfaceIp(vpcCfg, suffixedInterfaceName(tgwCfg, attachmentId))));
          return;
        }
      case VPN:
        {
          Optional<VpnConnection> vpnConnection =
              region.findTransitGatewayVpnConnection(tgwAttachment.getResourceId(), _gatewayId);
          if (!vpnConnection.isPresent()) {
            warnings.redFlag(
                String.format(
                    "VPN connection %s for transit gateway %s",
                    tgwAttachment.getResourceId(), _gatewayId));
            return;
          }
          vpnConnection
              .get()
              .getIpsecTunnels()
              .forEach(
                  tunnel ->
                      addStaticRoute(
                          vrf,
                          toStaticRoute(
                              route.getDestinationCidrBlock(), tunnel.getCgwInsideAddress())));
          return;
        }
      default:
        warnings.redFlag(
            String.format(
                "Transit gateway attachment type %s not handled in addRoute",
                tgwAttachment.getResourceType()));
    }
  }

  static String bgpExportPolicyName(String vrfName) {
    return String.format("~tgw~export-policy~%s~", vrfName);
  }

  static String bgpImportPolicyName(String vrfName) {
    return String.format("~tgw~import-policy~%s~", vrfName);
  }

  /** Returns the {@link Configuration} node name given to transit gateway with the provided id */
  static String nodeName(String gatewayId) {
    return gatewayId;
  }

  /** Return the VRF name used for a route table */
  static String vrfNameForRouteTable(String routeTableId) {
    return "vrf-" + routeTableId;
  }

  @Override
  public String getId() {
    return _gatewayId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransitGateway)) {
      return false;
    }
    TransitGateway that = (TransitGateway) o;
    return Objects.equals(_gatewayId, that._gatewayId) && Objects.equals(_options, that._options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_gatewayId, _options);
  }
}
