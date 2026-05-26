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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
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
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
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

  /**
   * Direct Connect Gateway IDs are bare lowercase-hex UUIDs (e.g. {@code
   * 085f3c96-59d9-4e6a-a37a-982e8f5653fc}). Unlike most AWS resource IDs, they have no service
   * prefix and so begin with a digit, which the pybatfish node specifier grammar rejects in
   * unquoted names (it disallows leading digits to avoid ambiguity with IP addresses). Prefix with
   * {@code dxgw-} so the node name starts with a letter.
   */
  static String nodeName(String directConnectGatewayId) {
    return "dxgw-" + directConnectGatewayId;
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

  /** Per-association policy for routes exported from the DXGW toward an associated VGW. */
  static String vgwExportPolicyName(String associationId) {
    return String.format("~dxgw~vgw-export~%s~", associationId);
  }

  /** Common import policy on the DXGW: accept all BGP. */
  static final String DXGW_IMPORT_POLICY_NAME = "~dxgw~import-policy~";

  /** AWS Direct Connect traffic-engineering community: high preference. */
  static final StandardCommunity DX_HIGH_PREF_COMMUNITY = StandardCommunity.of(7224, 7300);

  /** AWS Direct Connect traffic-engineering community: medium preference (default). */
  static final StandardCommunity DX_MEDIUM_PREF_COMMUNITY = StandardCommunity.of(7224, 7200);

  /** AWS Direct Connect traffic-engineering community: low preference. */
  static final StandardCommunity DX_LOW_PREF_COMMUNITY = StandardCommunity.of(7224, 7100);

  /**
   * Builds (if not already present) a routing policy on {@code owner} that accepts BGP routes and
   * sets local-preference based on AWS DX traffic-engineering communities (7224:7300/7200/7100).
   * Returns the policy name. Used on the AWS-internal side of any DXGW-adjacent BGP peer (TGW
   * import, VGW import on a VGW-attached Private VIF, VGW import on a DXGW→VGW peer) so that
   * customer-attached communities propagate to AWS-side path preference.
   */
  static String installDxImportPolicy(Configuration owner, String policyName) {
    if (owner.getRoutingPolicies().containsKey(policyName)) {
      return policyName;
    }
    RoutingPolicy.builder()
        .setName(policyName)
        .setOwner(owner)
        .setStatements(
            ImmutableList.of(
                new If(
                    new MatchProtocol(RoutingProtocol.BGP),
                    ImmutableList.of(
                        new If(
                            dxCommunityMatch(DX_HIGH_PREF_COMMUNITY),
                            ImmutableList.of(
                                new SetLocalPreference(
                                    new LiteralLong(Route.DIRECT_CONNECT_HIGH_LOCAL_PREFERENCE))),
                            ImmutableList.of(
                                new If(
                                    dxCommunityMatch(DX_LOW_PREF_COMMUNITY),
                                    ImmutableList.of(
                                        new SetLocalPreference(
                                            new LiteralLong(
                                                Route.DIRECT_CONNECT_LOW_LOCAL_PREFERENCE))),
                                    ImmutableList.of(
                                        new SetLocalPreference(
                                            new LiteralLong(
                                                Route.DIRECT_CONNECT_MEDIUM_LOCAL_PREFERENCE)))))),
                        Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
        .build();
    return policyName;
  }

  /** Returns a boolean expression that matches if the route carries the given community. */
  private static BooleanExpr dxCommunityMatch(StandardCommunity community) {
    return new MatchCommunities(
        InputCommunities.instance(), new HasCommunity(new CommunityIs(community)));
  }

  /**
   * Creates a Configuration node for this Direct Connect Gateway. Uses a single default VRF for
   * both TGW-facing and customer-facing (VIF) interfaces.
   *
   * <p>DXGWs are global resources: the same gateway appears in every region's collection, while its
   * associations and VIFs are regional and may live in different regions. Callers must aggregate
   * associations and VIFs from every region in which they were observed before invoking this
   * method.
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
      Collection<DirectConnectGatewayAssociation> associations,
      Collection<DirectConnectVirtualInterface> virtualInterfaces) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(
            nodeName(_directConnectGatewayId),
            "aws",
            _tags,
            DeviceModel.AWS_DIRECT_CONNECT_GATEWAY);
    if (cfgNode.getHumanName() == null) {
      cfgNode.setHumanName(_directConnectGatewayName);
    }

    initBgp(cfgNode);

    // Per-association: install allowed-prefix statics and a downstream-export filter.
    associations.forEach(
        assoc -> {
          installAllowedPrefixStatics(cfgNode, assoc.getAllowedPrefixes());
          String policyName =
              assoc.getAssociatedGateway().getType()
                      == DirectConnectGatewayAssociation.AssociatedGateway.GatewayType
                          .VIRTUAL_PRIVATE_GATEWAY
                  ? vgwExportPolicyName(assoc.getId())
                  : tgwExportPolicyName(assoc.getId());
          buildAllowedPrefixExportPolicy(cfgNode, policyName, assoc.getAllowedPrefixes());
        });

    // VIF export policy: advertise the originated allowed-prefix statics to on-prem.
    buildVifExportPolicy(cfgNode);

    // Configure BGP sessions toward customer routers via VIFs.
    virtualInterfaces.forEach(vif -> configureVifBgpSession(cfgNode, vif));

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
          // The static null-route exists so BGP can advertise the allowed-prefix list to on-prem
          // (matching AWS's behavior of advertising the allowed-prefix list itself). The static
          // is the "summary" route; the customer router uses it for outbound destinations within
          // the allowed prefix and the DXGW relies on the more-specific BGP-learned route from
          // the TGW peer for forwarding.
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

  /** Build the policy that exports allowed-prefix static null-routes toward on-prem. */
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
   * Build a per-association export policy from the DXGW toward a downstream peer (TGW or VGW).
   * Accept BGP routes whose destination is the same as or more specific than one of the allowed
   * prefixes.
   */
  private static void buildAllowedPrefixExportPolicy(
      Configuration cfgNode, String policyName, List<Prefix> allowedPrefixes) {
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

  /**
   * Wire a BGP-unnumbered link from this DXGW node to the associated VGW node for a VGW-typed
   * association. The DXGW exports allowed prefixes (same filter as TGW exports). The VGW imports
   * BGP and exports its VPC CIDRs (existing VGW BGP setup, see {@link
   * VpnGateway#toConfigurationNode}).
   */
  static void connectVgwAssociation(
      Configuration dxgwCfg,
      DirectConnectGateway dxgw,
      DirectConnectGatewayAssociation association,
      AwsConfiguration vsConfiguration,
      ConvertedConfiguration awsConfiguration) {
    String vgwId = association.getAssociatedGateway().getId();
    Configuration vgwCfg = awsConfiguration.getNode(vgwId);
    if (vgwCfg == null || vgwCfg.getDefaultVrf().getBgpProcess() == null) {
      // VGW node missing or has no BGP process; nothing to wire. (BGP is enabled on the VGW when
      // any DXGW association points at it; absence implies the VGW JSON wasn't loaded.)
      return;
    }

    long vgwAmazonSideAsn =
        vsConfiguration.getAccounts().stream()
            .flatMap(a -> a.getRegions().stream())
            .map(r -> r.getVpnGateways().get(vgwId))
            .filter(Objects::nonNull)
            .findFirst()
            .map(VpnGateway::getAmazonSideAsn)
            .orElse(0L);

    Utils.connect(
        awsConfiguration,
        dxgwCfg,
        Configuration.DEFAULT_VRF_NAME,
        vgwCfg,
        Configuration.DEFAULT_VRF_NAME,
        association.getId());

    String dxgwIfaceToVgw = Utils.interfaceNameToRemote(vgwCfg, association.getId());
    BgpUnnumberedPeerConfig.builder()
        .setPeerInterface(dxgwIfaceToVgw)
        .setRemoteAs(vgwAmazonSideAsn)
        .setLocalIp(LINK_LOCAL_IP)
        .setLocalAs(dxgw._amazonSideAsn)
        .setBgpProcess(dxgwCfg.getDefaultVrf().getBgpProcess())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(vgwExportPolicyName(association.getId()))
                .setImportPolicy(DXGW_IMPORT_POLICY_NAME)
                .build())
        .build();

    String vgwIfaceToDxgw = Utils.interfaceNameToRemote(dxgwCfg, association.getId());
    String vgwImportPolicy =
        installDxImportPolicy(vgwCfg, VpnGateway.vgwDxImportPolicyName(association.getId()));
    BgpUnnumberedPeerConfig.builder()
        .setPeerInterface(vgwIfaceToDxgw)
        .setRemoteAs(dxgw._amazonSideAsn)
        .setLocalIp(LINK_LOCAL_IP)
        .setLocalAs(vgwAmazonSideAsn)
        .setBgpProcess(vgwCfg.getDefaultVrf().getBgpProcess())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(VpnGateway.VGW_EXPORT_POLICY_NAME)
                .setImportPolicy(vgwImportPolicy)
                .build())
        .build();
  }

  private void configureVifBgpSession(Configuration cfgNode, DirectConnectVirtualInterface vif) {
    Ip amazonIp = vif.getAmazonIp();
    Ip customerIp = vif.getCustomerIp();

    Interface vifIface =
        Utils.newInterface(
            vif.getId(),
            cfgNode,
            vif.getAmazonAddress(),
            "Direct Connect VIF " + vif.getVirtualInterfaceName());
    vifIface.updateInterfaceType(org.batfish.datamodel.InterfaceType.PHYSICAL);
    vifIface.setEncapsulationVlan(vif.getVlan());

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
