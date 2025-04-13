package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.Utils.ACCEPT_ALL_BGP;
import static org.batfish.representation.aws.Utils.ACCEPT_ALL_BGP_AND_STATIC;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.connect;
import static org.batfish.representation.aws.Utils.interfaceNameToRemote;
import static org.batfish.representation.aws.Utils.makeBgpProcess;
import static org.batfish.representation.aws.Utils.toStaticRoute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.TransitGatewayAttachment.ResourceType;
import org.batfish.representation.aws.TransitGatewayPropagations.Propagation;

/**
 * Represents an AWS Transit Gateway
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-transit-gateways.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class TransitGateway implements AwsVpcEntity, Serializable {

  public @Nonnull TransitGatewayOptions getOptions() {
    return _options;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class TransitGatewayOptions implements Serializable {

    private final long _amazonSideAsn;

    private final boolean _defaultRouteTableAssociation;

    private final @Nonnull String _associationDefaultRouteTableId;

    private final boolean _defaultRouteTablePropagation;

    private final @Nonnull String _propagationDefaultRouteTableId;

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
        @JsonProperty(JSON_KEY_AMAZON_SIDE_ASN) @Nullable Long amazonSideAsn,
        @JsonProperty(JSON_KEY_DEFAULT_ROUTE_TABLE_ASSOCIATION) @Nullable
            String defaultRouteTableAssociation,
        @JsonProperty(JSON_KEY_ASSOCIATION_DEFAULT_ROUTE_TABLE_ID) @Nullable
            String associationDefaultRouteTableId,
        @JsonProperty(JSON_KEY_DEFAULT_ROUTE_TABLE_PROPAGATION) @Nullable
            String defaultRouteTablePropagation,
        @JsonProperty(JSON_KEY_PROPAGATION_DEFAULT_ROUTE_TABLE_ID) @Nullable
            String propagationDefaultRouteTableId,
        @JsonProperty(JSON_KEY_VPN_ECMP_SUPPORT) @Nullable String vpcEcmpSupport) {
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

    public @Nonnull String getAssociationDefaultRouteTableId() {
      return _associationDefaultRouteTableId;
    }

    public boolean isDefaultRouteTablePropagation() {
      return _defaultRouteTablePropagation;
    }

    public @Nonnull String getPropagationDefaultRouteTableId() {
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

  private final @Nonnull String _gatewayId;
  private final @Nonnull TransitGatewayOptions _options;
  private final @Nonnull String _ownerId;
  private final @Nonnull Map<String, String> _tags;

  @JsonCreator
  private static TransitGateway create(
      @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) @Nullable String gatewayId,
      @JsonProperty(JSON_KEY_OPTIONS) @Nullable TransitGatewayOptions options,
      @JsonProperty(JSON_KEY_OWNER_ID) @Nullable String ownerId,
      @JsonProperty(JSON_KEY_TAGS) @Nullable List<Tag> tags) {
    checkArgument(gatewayId != null, "Transit Gateway Id cannot be null");
    checkArgument(options != null, "Transit Gateway Options cannot be null");
    checkArgument(ownerId != null, "Transit Gateway owner ID cannot be null");

    return new TransitGateway(
        gatewayId,
        options,
        ownerId,
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)));
  }

  public TransitGateway(
      String gatewayId, TransitGatewayOptions options, String ownerId, Map<String, String> tags) {
    _gatewayId = gatewayId;
    _options = options;
    _ownerId = ownerId;
    _tags = tags;
  }

  /** Creates a node for the transit gateway. */
  Configuration toConfigurationNode(
      AwsConfiguration vsConfig,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(
            nodeName(_gatewayId), "aws", _tags, DeviceModel.AWS_TRANSIT_GATEWAY);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    Set<TransitGatewayRouteTable> routeTables =
        region.getTransitGatewayRouteTables().values().stream()
            .filter(routeTable -> routeTable.getGatewayId().equals(_gatewayId))
            .collect(ImmutableSet.toImmutableSet());

    // create a VRF for each route table
    routeTables.forEach(
        table ->
            Vrf.builder().setOwner(cfgNode).setName(vrfNameForRouteTable(table.getId())).build());

    // initialize VPN infrastructure if this TGW has any VPN attachments
    if (region.getTransitGatewayAttachments().values().stream()
        .anyMatch(
            a -> a.getGatewayId().equals(_gatewayId) && a.getResourceType() == ResourceType.VPN)) {
      VpnConnection.initVpnConnectionsInfrastructure(cfgNode);
    }

    // make connections to the attachments
    region.getTransitGatewayAttachments().values().stream()
        .filter(a -> a.getGatewayId().equals(_gatewayId))
        .forEach(
            a ->
                connectAttachment(
                    cfgNode, a, routeTables, vsConfig, awsConfiguration, region, warnings));

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
                            cfgNode,
                            table,
                            propagation,
                            vsConfig,
                            awsConfiguration,
                            region,
                            warnings)));

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
      Set<TransitGatewayRouteTable> routeTables,
      AwsConfiguration vsConfiguration,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    switch (attachment.getResourceType()) {
      case PEERING:
        {
          // for peerings, we create interfaces here, so we point static routes to it
          // the interfaces are connected when we process peerings after creating all TGWs
          routeTables.forEach(
              rt ->
                  Utils.newInterface(
                      sendSidePeeringInterfaceName(rt.getId(), attachment.getId()),
                      tgwCfg,
                      vrfNameForRouteTable(rt.getId()),
                      LinkLocalAddress.of(LINK_LOCAL_IP),
                      "To " + attachment.getResourceId()));
          return;
        }
      case VPC:
        {
          Optional<TransitGatewayVpcAttachment> vpcAttachment =
              region.findTransitGatewayVpcAttachment(
                  attachment.getResourceId(), tgwCfg.getHostname());
          if (!vpcAttachment.isPresent()) {
            warnings.redFlagf(
                "VPC attachment %s not found for transit gateway %s",
                attachment.getResourceId(), tgwCfg.getHostname());
            return;
          }
          connectVpc(tgwCfg, attachment, vsConfiguration, awsConfiguration, region, warnings);
          return;
        }
      case VPN:
        {
          Optional<VpnConnection> vpnConnection =
              region.findTransitGatewayVpnConnection(
                  attachment.getResourceId(), tgwCfg.getHostname());
          if (!vpnConnection.isPresent()) {
            warnings.redFlagf(
                "VPN connection %s for transit gateway %s not found",
                attachment.getResourceId(), tgwCfg.getHostname());
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

  /**
   * Makes possibly multiple links between TGW and VPC. There is one link per associated or
   * propagated route table. The same VRF is used on the VPC-side for all links. A table-specific
   * VRF is used on the TGW side. This function assumes that those VRFs have already been created.
   *
   * <p>For the associated route table, it also installs a static route on the VPC that sends all
   * traffic to the new associated link.
   */
  @VisibleForTesting
  static void connectVpc(
      Configuration tgwCfg,
      TransitGatewayAttachment attachment,
      AwsConfiguration vsConfiguration,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {

    // we need to look beyond the region; all VPCs attached to the transit gateway can be in
    // different accounts
    Vpc vpc = vsConfiguration.getVpc(attachment.getResourceId());
    if (vpc == null) {
      warnings.redFlagf(
          "VPC %s for attachment %s not found in region %s",
          attachment.getResourceId(), attachment.getId(), region.getName());
      return;
    }

    Configuration vpcCfg = awsConfiguration.getNode(Vpc.nodeName(vpc.getId()));
    String vrfNameOnVpc = Vpc.vrfNameForLink(attachment.getId());
    if (!vpcCfg.getVrfs().containsKey(vrfNameOnVpc)) {
      warnings.redFlagf("VRF %s not found on VPC %s", vrfNameOnVpc, vpc.getId());
      return;
    }

    String associatedRouteTable =
        attachment.getAssociation() == null
                || !attachment.getAssociation().getState().equals(STATE_ASSOCIATED)
            ? null
            : attachment.getAssociation().getRouteTableId();

    if (associatedRouteTable != null) {
      connectVpcToRouteTable(
          tgwCfg, associatedRouteTable, vpcCfg, vrfNameOnVpc, true, awsConfiguration, warnings);
    }

    // link to additional tables used for propagation
    region.getTransitGatewayRouteTables().values().stream()
        .filter(
            table ->
                !table.getId().equals(associatedRouteTable) // not the assoc. table
                    && table.getGatewayId().equals(attachment.getGatewayId()) // this TGW's table
                    && region.getTransitGatewayPropagations().containsKey(table.getId())
                    // propagations of the table contain the VPC
                    && region
                        .getTransitGatewayPropagations()
                        .get(table.getId())
                        .getPropagations()
                        .stream()
                        .anyMatch(
                            propagation ->
                                propagation.getAttachmentId().equals(attachment.getId())
                                    && propagation.isEnabled()))
        .forEach(
            table ->
                connectVpcToRouteTable(
                    tgwCfg,
                    table.getId(),
                    vpcCfg,
                    vrfNameOnVpc,
                    false,
                    awsConfiguration,
                    warnings));
  }

  private static void connectVpcToRouteTable(
      Configuration tgwCfg,
      String routeTableId,
      Configuration vpcCfg,
      String vrfNameOnVpc,
      boolean associatedTable,
      ConvertedConfiguration awsConfiguration,
      Warnings warnings) {
    String vrfNameOnTgw = vrfNameForRouteTable(routeTableId);
    if (!tgwCfg.getVrfs().containsKey(vrfNameOnTgw)) {
      warnings.redFlagf("VRF %s not found on TGW %s", vrfNameOnTgw, tgwCfg.getHostname());
      return;
    }
    connect(awsConfiguration, tgwCfg, vrfNameOnTgw, vpcCfg, vrfNameOnVpc, routeTableId);
    if (associatedTable) {
      addStaticRoute(
          vpcCfg.getVrfs().get(vrfNameOnVpc),
          toStaticRoute(
              Prefix.ZERO,
              Utils.interfaceNameToRemote(tgwCfg, routeTableId),
              Utils.getInterfaceLinkLocalIp(
                  tgwCfg, Utils.interfaceNameToRemote(vpcCfg, routeTableId))));
    }
  }

  private static void connectVpn(
      Configuration tgwCfg,
      TransitGatewayAttachment attachment,
      VpnConnection vpnConnection,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {

    if (attachment.getAssociation() == null
        || !attachment.getAssociation().getState().equals(STATE_ASSOCIATED)) {
      warnings.redFlagf(
          "Skipped VPN %s as attachment because it is not associated", attachment.getResourceId());
      return;
    }

    String vrfName = vrfNameForRouteTable(attachment.getAssociation().getRouteTableId());
    if (!tgwCfg.getVrfs().containsKey(vrfName)) {
      warnings.redFlagf("VRF %s not found on TGW %s", vrfName, tgwCfg.getHostname());
      return;
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
        tgwCfg, vrf, bgpExportPolicyName(vrfName), bgpImportPolicyName(vrfName), warnings);
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
    if (attachment.getAssociation() == null) {
      return Optional.empty();
    }
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
              "Unsupported VPN attachment configuration for %s. Association route table = %s."
                  + " Propagation route tables = %s",
              vpnConnection.getId(), associatedRoutingTableId, propagatedRoutingTableIds));
    }
  }

  @VisibleForTesting
  static void createBgpProcess(
      Configuration tgwCfg, Vrf vrf, ConvertedConfiguration awsConfiguration) {
    LinkLocalAddress loopbackBgpAddress = LinkLocalAddress.of(LINK_LOCAL_IP);
    Utils.newInterface(
        "bgp-loopback-" + vrf.getName(),
        tgwCfg,
        vrf.getName(),
        loopbackBgpAddress,
        "BGP loopback for " + vrf.getName());

    BgpProcess proc =
        makeBgpProcess(
            loopbackBgpAddress.getIp(),
            vrf); // TODO: check if vpn ecmp support setting in transit gateway has an impact here
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
      AwsConfiguration vsConfig,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    if (!propagation.isEnabled()) {
      return;
    }
    switch (propagation.getResourceType()) {
      case VPC:
        // need to look across all VPCs (accross accounts)
        Vpc vpc = vsConfig.getVpc(propagation.getResourceId());
        if (vpc == null) {
          warnings.redFlagf(
              "VPC %s for propagating attachment %s not found in region %s",
              propagation.getResourceId(), propagation.getAttachmentId(), region.getName());
          return;
        }
        propagateRoutesVpc(tgwCfg, table, vpc, awsConfiguration, warnings);
        return;
      case VPN:
        /*
        For currently supported configurations, we don't need to do anything here.
        (We warn about lack of support in connectVpn.)
        */
        return;
      default:
        warnings.redFlagf(
            "Resource type %s for transit gateway route propagation",
            propagation.getResourceType());
    }
  }

  private static void propagateRoutesVpc(
      Configuration tgwCfg,
      TransitGatewayRouteTable table,
      Vpc vpc,
      ConvertedConfiguration awsConfiguration,
      Warnings warnings) {

    Configuration vpcCfg = awsConfiguration.getNode(Vpc.nodeName(vpc.getId()));
    if (vpcCfg == null) {
      warnings.redFlagf("VPC configuration node for VPC %s not found", vpc.getId());
      return;
    }

    Interface localIface =
        tgwCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(vpcCfg, table.getId()));
    if (localIface == null) {
      warnings.redFlagf("Interface facing VPC %s not found on TGW", vpc.getId());
      return;
    }

    Interface remoteIface =
        vpcCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(tgwCfg, table.getId()));
    if (remoteIface == null) {
      warnings.redFlagf("Interface facing TGW not found on VPC %s", vpc.getId());
      return;
    }

    String vrfName = vrfNameForRouteTable(table.getId());
    Vrf tgwVrf = tgwCfg.getVrfs().get(vrfName);
    if (tgwVrf == null) {
      warnings.redFlagf("VRF %s not found on TGW %s", vrfName, tgwCfg.getHostname());
      return;
    }

    if (!localIface.getVrfName().equals(vrfName)) {
      warnings.redFlagf(
          "Unexpected interface VRF %s. Expected %s", localIface.getVrfName(), vrfName);
      return;
    }

    vpc.getCidrBlockAssociations()
        .forEach(
            pfx ->
                addStaticRoute(
                    tgwVrf,
                    toStaticRoute(
                        pfx, localIface.getName(), remoteIface.getLinkLocalAddress().getIp())));
  }

  private void addTransitGatewayStaticRoute(
      Configuration tgwCfg,
      TransitGatewayRouteTable routeTable,
      TransitGatewayRouteV4 route,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    String vrfName = vrfNameForRouteTable(routeTable.getId());
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
                    tgwCfg,
                    vrf,
                    route,
                    attachmentId,
                    routeTable.getId(),
                    awsConfiguration,
                    region,
                    warnings));
  }

  private void addTransitGatewayStaticRouteAttachment(
      Configuration tgwCfg,
      Vrf vrf,
      TransitGatewayRouteV4 route,
      String attachmentId,
      String routeTableId,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    TransitGatewayAttachment tgwAttachment =
        region.findTransitGatewayAttachment(attachmentId, _gatewayId).orElse(null);
    if (tgwAttachment == null) {
      warnings.redFlagf(
          "Transit gateway attachment %s not found for route %s", attachmentId, route);
      return;
    }
    switch (tgwAttachment.getResourceType()) {
      case PEERING:
        {
          addStaticRoute(
              vrf,
              toStaticRoute(
                  route.getDestinationCidrBlock(),
                  sendSidePeeringInterfaceName(routeTableId, attachmentId),
                  LINK_LOCAL_IP));
          return;
        }
      case VPC:
        {
          Configuration vpcCfg =
              awsConfiguration.getNode(Vpc.nodeName(tgwAttachment.getResourceId()));
          if (vpcCfg == null) {
            warnings.redFlagf(
                "Static route to %s in route table %s on TGW %s points to VPC %s, but the VPC"
                    + " configuration was not found",
                route.getDestinationCidrBlock(),
                routeTableId,
                tgwCfg.getHostname(),
                Vpc.nodeName(tgwAttachment.getResourceId()));
            return;
          }
          String ifaceNameOnVpc = interfaceNameToRemote(tgwCfg, routeTableId);
          if (!vpcCfg.getAllInterfaces().containsKey(ifaceNameOnVpc)) {
            warnings.redFlagf(
                "Static route to %s in route table %s on TGW %s points to VPC %s, but the VPC"
                    + " is not propagating to that table",
                route.getDestinationCidrBlock(),
                routeTableId,
                tgwCfg.getHostname(),
                vpcCfg.getHostname());
            return;
          }
          addStaticRoute(
              vrf,
              toStaticRoute(
                  route.getDestinationCidrBlock(),
                  Utils.interfaceNameToRemote(vpcCfg, routeTableId),
                  Utils.getInterfaceLinkLocalIp(vpcCfg, ifaceNameOnVpc)));
          return;
        }
      case VPN:
        {
          Optional<VpnConnection> vpnConnection =
              region.findTransitGatewayVpnConnection(tgwAttachment.getResourceId(), _gatewayId);
          if (!vpnConnection.isPresent()) {
            warnings.redFlagf(
                "VPN connection %s for transit gateway %s",
                tgwAttachment.getResourceId(), _gatewayId);
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
        warnings.redFlagf(
            "Transit gateway attachment type %s not handled in addRoute",
            tgwAttachment.getResourceType());
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

  /** Return the interface name used for TGW peering interfaces on the receiver side */
  static String receiveSidePeeringInterfaceName(
      String sendSideRouteTableId, String attachmentId, String associatedRouteTableId) {
    return sendSideRouteTableId + "-" + attachmentId + "-" + associatedRouteTableId;
  }

  /** Return the interface name used for TGW peering interfaces on the sender side */
  static String sendSidePeeringInterfaceName(String routeTableId, String attachmentId) {
    return routeTableId + "-" + attachmentId;
  }

  /** Return the VRF name used for a route table */
  static String vrfNameForRouteTable(String routeTableId) {
    return "vrf-" + routeTableId;
  }

  @Override
  public String getId() {
    return _gatewayId;
  }

  public @Nonnull String getOwnerId() {
    return _ownerId;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransitGateway)) {
      return false;
    }
    TransitGateway that = (TransitGateway) o;
    return _gatewayId.equals(that._gatewayId)
        && _options.equals(that._options)
        && _ownerId.equals(that._ownerId)
        && _tags.equals(that._tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_gatewayId, _options, _tags);
  }
}
