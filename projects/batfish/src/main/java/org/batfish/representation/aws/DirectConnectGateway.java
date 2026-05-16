package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.Utils.makeBgpProcess;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Represents an AWS Direct Connect Gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class DirectConnectGateway implements AwsVpcEntity, Serializable {

  static final String JSON_KEY_DIRECT_CONNECT_GATEWAYS = "DirectConnectGateways";
  static final String JSON_KEY_DIRECT_CONNECT_GATEWAY_ID = "DirectConnectGatewayId";
  static final String JSON_KEY_DIRECT_CONNECT_GATEWAY_NAME = "DirectConnectGatewayName";

  private final @Nonnull String _directConnectGatewayId;

  private final @Nonnull String _directConnectGatewayName;

  private final long _amazonSideAsn;

  private final @Nonnull Map<String, String> _tags;

  @JsonCreator
  private static DirectConnectGateway create(
      @JsonProperty(JSON_KEY_DIRECT_CONNECT_GATEWAY_ID) @Nullable String directConnectGatewayId,
      @JsonProperty(JSON_KEY_DIRECT_CONNECT_GATEWAY_NAME) @Nullable String directConnectGatewayName,
      @JsonProperty(JSON_KEY_AMAZON_SIDE_ASN) @Nullable Long amazonSideAsn,
      @JsonProperty(JSON_KEY_TAGS) @Nullable List<Tag> tags) {
    checkArgument(directConnectGatewayId != null, "Direct Connect Gateway id cannot be null");
    checkArgument(directConnectGatewayName != null, "Direct Connect Gateway name cannot be null");
    checkArgument(
        amazonSideAsn != null, "Amazon side ASN cannot be null for Direct Connect Gateway");

    return new DirectConnectGateway(
        directConnectGatewayId,
        directConnectGatewayName,
        amazonSideAsn,
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)));
  }

  DirectConnectGateway(
      String directConnectGatewayId,
      String directConnectGatewayName,
      long amazonSideAsn,
      Map<String, String> tags) {
    _directConnectGatewayId = directConnectGatewayId;
    _directConnectGatewayName = directConnectGatewayName;
    _amazonSideAsn = amazonSideAsn;
    _tags = tags;
  }

  @Override
  public @Nonnull String getId() {
    return _directConnectGatewayId;
  }

  public @Nonnull String getDirectConnectGatewayName() {
    return _directConnectGatewayName;
  }

  public long getAmazonSideAsn() {
    return _amazonSideAsn;
  }

  public @Nonnull Map<String, String> getTags() {
    return _tags;
  }

  static String nodeName(String directConnectGatewayId) {
    return directConnectGatewayId;
  }

  /**
   * Routing policy that exports routes to the customer over the VIF: only the originated allowed
   * prefixes (advertised as static null-routes on the DXGW), matching AWS's behavior of advertising
   * the allowed prefix list itself rather than the underlying routes.
   */
  static final String VIF_EXPORT_POLICY_NAME = "~dxgw~vif-export~";

  /** Per-association policy for routes exported from the DXGW toward an associated TGW. */
  static String tgwExportPolicyName(String associationId) {
    return String.format("~dxgw~tgw-export~%s~", associationId);
  }

  /** Common import policy on the DXGW: accept all BGP. */
  static final String DXGW_IMPORT_POLICY_NAME = "~dxgw~import-policy~";

  /**
   * Creates a Configuration node for this Direct Connect Gateway. Uses a single default VRF for
   * both TGW-facing and customer-facing (VIF) interfaces.
   *
   * <p>Routing on the DXGW is BGP-driven and matches AWS's documented behavior:
   *
   * <ul>
   *   <li>Each association's {@code allowedPrefixesToDirectConnectGateway} list is originated as
   *       static null-routes on the DXGW. These statics are advertised to the on-prem customer BGP
   *       peer (matching AWS's "advertise the allowed prefix list itself" behavior).
   *   <li>Routes received from on-prem (BGP) are exported toward each TGW peer, filtered to those
   *       within or equal to one of that association's allowed prefixes.
   *   <li>Routes received from a TGW peer (BGP) are kept locally on the DXGW for forwarding but not
   *       re-advertised toward on-prem (the on-prem side already has the summary statics).
   * </ul>
   */
  Configuration toConfigurationNode(
      Region region, ConvertedConfiguration awsConfiguration, Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(
            nodeName(_directConnectGatewayId),
            "aws",
            _tags,
            DeviceModel.AWS_DIRECT_CONNECT_GATEWAY);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    initBgp(cfgNode);

    // Per-association: install allowed-prefix statics and a TGW-export filter.
    region.getDirectConnectGatewayAssociations().values().stream()
        .filter(assoc -> _directConnectGatewayId.equals(assoc.getDirectConnectGatewayId()))
        .forEach(
            assoc -> {
              installAllowedPrefixStatics(cfgNode, assoc.getAllowedPrefixes());
              buildTgwExportPolicy(cfgNode, assoc.getId(), assoc.getAllowedPrefixes());
            });

    // VIF export policy: advertise the static null-routes (which are exactly the allowed prefixes).
    buildVifExportPolicy(cfgNode);

    // Configure BGP sessions toward customer routers via VIFs.
    region.getDirectConnectVirtualInterfaces().values().stream()
        .filter(vif -> _directConnectGatewayId.equals(vif.getDirectConnectGatewayId()))
        .forEach(vif -> configureVifBgpSession(cfgNode, vif, warnings));

    return cfgNode;
  }

  private void initBgp(Configuration cfgNode) {
    LinkLocalAddress loopbackAddress = LinkLocalAddress.of(LINK_LOCAL_IP);
    Utils.newInterface("bgp-loopback", cfgNode, loopbackAddress, "BGP loopback");

    Vrf defaultVrf = cfgNode.getDefaultVrf();
    BgpProcess proc = makeBgpProcess(loopbackAddress.getIp(), defaultVrf);
    proc.setMultipathEquivalentAsPathMatchMode(MultipathEquivalentAsPathMatchMode.EXACT_PATH);

    RoutingPolicy.builder()
        .setName(DXGW_IMPORT_POLICY_NAME)
        .setOwner(cfgNode)
        .setStatements(
            ImmutableList.of(
                new If(
                    new MatchProtocol(RoutingProtocol.BGP),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
        .build();
  }

  private static void installAllowedPrefixStatics(Configuration cfgNode, List<Prefix> prefixes) {
    Vrf defaultVrf = cfgNode.getDefaultVrf();
    prefixes.forEach(
        p -> {
          StaticRoute route =
              StaticRoute.builder()
                  .setNetwork(p)
                  .setNextHop(org.batfish.datamodel.route.nh.NextHopDiscard.instance())
                  .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
                  .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
                  .build();
          defaultVrf.getStaticRoutes().add(route);
        });
  }

  /** Build the policy that exports static null-routes (the allowed prefixes) toward on-prem. */
  private static void buildVifExportPolicy(Configuration cfgNode) {
    if (cfgNode.getRoutingPolicies().containsKey(VIF_EXPORT_POLICY_NAME)) {
      return;
    }
    RoutingPolicy.builder()
        .setName(VIF_EXPORT_POLICY_NAME)
        .setOwner(cfgNode)
        .setStatements(
            ImmutableList.of(
                new If(
                    new MatchProtocol(RoutingProtocol.STATIC),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
        .build();
  }

  /**
   * Build a per-association export policy from the DXGW toward a TGW peer. Accept BGP routes whose
   * destination is the same as or more specific than one of the allowed prefixes.
   */
  private static void buildTgwExportPolicy(
      Configuration cfgNode, String associationId, List<Prefix> allowedPrefixes) {
    String policyName = tgwExportPolicyName(associationId);
    if (allowedPrefixes.isEmpty()) {
      RoutingPolicy.builder()
          .setName(policyName)
          .setOwner(cfgNode)
          .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
          .build();
      return;
    }
    PrefixSpace space = new PrefixSpace();
    allowedPrefixes.forEach(p -> space.addPrefixRange(PrefixRange.sameAsOrMoreSpecificThan(p)));
    RoutingPolicy.builder()
        .setName(policyName)
        .setOwner(cfgNode)
        .setStatements(
            ImmutableList.of(
                new If(
                    new Conjunction(
                        ImmutableList.of(
                            new MatchProtocol(RoutingProtocol.BGP),
                            new MatchPrefixSet(
                                DestinationNetwork.instance(), new ExplicitPrefixSet(space)))),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
        .build();
  }

  private void configureVifBgpSession(
      Configuration cfgNode, DirectConnectVirtualInterface vif, Warnings warnings) {
    Ip amazonIp = vif.getAmazonIp();
    Ip customerIp = vif.getCustomerIp();

    Interface vifIface =
        Utils.newInterface(
            vif.getId(),
            cfgNode,
            vif.getAmazonAddress(),
            "Direct Connect VIF " + vif.getVirtualInterfaceName());
    vifIface.updateInterfaceType(org.batfish.datamodel.InterfaceType.PHYSICAL);

    BgpActivePeerConfig.builder()
        .setPeerAddress(customerIp)
        .setRemoteAsns(LongSpace.of(vif.getAsn()))
        .setLocalIp(amazonIp)
        .setLocalAs(_amazonSideAsn)
        .setBgpProcess(cfgNode.getDefaultVrf().getBgpProcess())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(VIF_EXPORT_POLICY_NAME)
                .setImportPolicy(DXGW_IMPORT_POLICY_NAME)
                .build())
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DirectConnectGateway)) {
      return false;
    }
    DirectConnectGateway that = (DirectConnectGateway) o;
    return _amazonSideAsn == that._amazonSideAsn
        && Objects.equals(_directConnectGatewayId, that._directConnectGatewayId)
        && Objects.equals(_directConnectGatewayName, that._directConnectGatewayName)
        && Objects.equals(_tags, that._tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_directConnectGatewayId, _directConnectGatewayName, _amazonSideAsn, _tags);
  }
}
