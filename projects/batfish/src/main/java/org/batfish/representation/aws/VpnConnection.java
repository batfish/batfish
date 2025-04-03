package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsConfiguration.vpnExternalInterfaceName;
import static org.batfish.representation.aws.AwsConfiguration.vpnInterfaceName;
import static org.batfish.representation.aws.AwsConfiguration.vpnTunnelId;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.createBackboneConnection;
import static org.batfish.representation.aws.Utils.toStaticRoute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.Lists;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Represents an AWS VPN connection */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class VpnConnection implements AwsVpcEntity, Serializable {

  // the VRF for interfaces that underlie the IPSec tunnel. they are the ones with public IP.
  static final String VPN_UNDERLAY_VRF_NAME = "vrf-vpn-underlay";

  /** Export policy to backbone */
  static final String VPN_TO_BACKBONE_EXPORT_POLICY_NAME = "~vpn~to~backbone~export~policy~";

  /**
   * Routing policy statement that exports connected routes. It is used to advertize underlay
   * interface addresses (public IPs) to the backbone.
   */
  static Statement EXPORT_CONNECTED_STATEMENT =
      new If(
          new MatchProtocol(RoutingProtocol.CONNECTED),
          ImmutableList.of(
              new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, null)),
              Statements.ExitAccept.toStaticStatement()));

  private static DiffieHellmanGroup toDiffieHellmanGroup(String perfectForwardSecrecy) {
    switch (perfectForwardSecrecy) {
      case "2":
        return DiffieHellmanGroup.GROUP2;
      case "5":
        return DiffieHellmanGroup.GROUP5;
      case "14":
        return DiffieHellmanGroup.GROUP14;
      case "15":
        return DiffieHellmanGroup.GROUP15;
      case "16":
        return DiffieHellmanGroup.GROUP16;
      case "17":
        return DiffieHellmanGroup.GROUP17;
      case "18":
        return DiffieHellmanGroup.GROUP18;
      case "19":
        return DiffieHellmanGroup.GROUP19;
      case "20":
        return DiffieHellmanGroup.GROUP20;
      case "21":
        return DiffieHellmanGroup.GROUP21;
      case "22":
        return DiffieHellmanGroup.GROUP22;
      case "23":
        return DiffieHellmanGroup.GROUP23;
      case "24":
        return DiffieHellmanGroup.GROUP24;
      default:
        throw new BatfishException(
            "No conversion to Diffie-Hellman group for string: \"" + perfectForwardSecrecy + "\"");
    }
  }

  private static EncryptionAlgorithm toEncryptionAlgorithm(String encryptionProtocol) {
    switch (encryptionProtocol) {
      case "AES128":
        return EncryptionAlgorithm.AES_128_CBC;
      case "AES256":
        return EncryptionAlgorithm.AES_256_CBC;
      case "AES128-GCM-16":
        return EncryptionAlgorithm.AES_128_GCM;
      case "AES256-GCM-16":
        return EncryptionAlgorithm.AES_256_GCM;
      default:
        throw new BatfishException(
            "No conversion to encryption algorithm for string: \"" + encryptionProtocol + "\"");
    }
  }

  private static IkeHashingAlgorithm toIkeAuthenticationAlgorithm(String ikeAuthProtocol) {
    switch (ikeAuthProtocol) {
      case "SHA1":
        return IkeHashingAlgorithm.SHA1;
      case "SHA2-256":
        return IkeHashingAlgorithm.SHA_256;
      case "SHA2-384":
        return IkeHashingAlgorithm.SHA_384;
      case "SHA2-512":
        return IkeHashingAlgorithm.SHA_512;
      default:
        throw new BatfishException(
            "No conversion to ike authentication algorithm for string: \""
                + ikeAuthProtocol
                + "\"");
    }
  }

  private static IpsecAuthenticationAlgorithm toIpsecAuthenticationAlgorithm(
      String ipsecAuthProtocol) {
    switch (ipsecAuthProtocol) {
      case "SHA1":
        return IpsecAuthenticationAlgorithm.HMAC_SHA1_96;
      case "SHA2-256":
        return IpsecAuthenticationAlgorithm.HMAC_SHA_256_128;
      case "SHA2-384":
        return IpsecAuthenticationAlgorithm.HMAC_SHA_384;
      case "SHA2-512":
        return IpsecAuthenticationAlgorithm.HMAC_SHA_512;
      default:
        throw new BatfishException(
            "No conversion to ipsec authentication algorithm for string: \""
                + ipsecAuthProtocol
                + "\"");
    }
  }

  private static IpsecProtocol toIpsecProtocol(String ipsecProtocol) {
    switch (ipsecProtocol) {
      case "esp":
        return IpsecProtocol.ESP;
      default:
        throw new BatfishException(
            "No conversion to ipsec protocol for string: \"" + ipsecProtocol + "\"");
    }
  }

  private static @Nullable IpsecEncapsulationMode toIpsecEncapdulationMode(
      String ipsecEncapsulationMode, Warnings warnings) {
    switch (ipsecEncapsulationMode) {
      case "tunnel":
        return IpsecEncapsulationMode.TUNNEL;
      case "transport":
        return IpsecEncapsulationMode.TRANSPORT;
      default:
        warnings.redFlagf("No IPsec encapsulation mode for string '%s'", ipsecEncapsulationMode);
        return null;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class VpnRoute {

    @JsonCreator
    private static VpnRoute create(
        @JsonProperty(JSON_KEY_DESTINATION_CIDR_BLOCK) @Nullable Prefix destinationCidrBlock) {
      checkArgument(
          destinationCidrBlock != null, "Destination CIDR block cannot be null in VpnRoute");
      return new VpnRoute(destinationCidrBlock);
    }

    private final @Nonnull Prefix _destinationCidrBlock;

    private VpnRoute(Prefix destinationCidrBlock) {
      _destinationCidrBlock = destinationCidrBlock;
    }

    @Nonnull
    Prefix getDestinationCidrBlock() {
      return _destinationCidrBlock;
    }
  }

  private final @Nonnull List<VgwTelemetry> _vgwTelemetries;

  enum GatewayType {
    TRANSIT,
    VPN
  }

  private final @Nonnull String _customerGatewayId;

  private final @Nonnull List<IpsecTunnel> _ipsecTunnels;

  private final boolean _isBgpConnection;

  private final @Nonnull List<Prefix> _routes;

  private final boolean _staticRoutesOnly;

  VpnConnection(
      boolean isBgpConnection,
      String vpnConnectionId,
      String customerGatewayId,
      GatewayType awsGatewayType,
      String awsGatewayId,
      List<IpsecTunnel> ipsecTunnels,
      List<Prefix> routes,
      List<VgwTelemetry> vgwTelemetries,
      boolean staticRoutesOnly) {
    _isBgpConnection = isBgpConnection;
    _vpnConnectionId = vpnConnectionId;
    _customerGatewayId = customerGatewayId;
    _awsGatewayType = awsGatewayType;
    _awsGatewayId = awsGatewayId;
    _ipsecTunnels = ipsecTunnels;
    _routes = routes;
    _vgwTelemetries = vgwTelemetries;
    _staticRoutesOnly = staticRoutesOnly;
  }

  private final @Nonnull String _vpnConnectionId;

  private final @Nonnull GatewayType _awsGatewayType;

  private final @Nonnull String _awsGatewayId;

  @JsonCreator
  private static VpnConnection create(
      @JsonProperty(JSON_KEY_VPN_CONNECTION_ID) @Nullable String vpnConnectionId,
      @JsonProperty(JSON_KEY_CUSTOMER_GATEWAY_ID) @Nullable String customerGatewayId,
      @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) @Nullable String transitGatewayId,
      @JsonProperty(JSON_KEY_VPN_GATEWAY_ID) @Nullable String vpnGatewayId,
      @JsonProperty(JSON_KEY_CUSTOMER_GATEWAY_CONFIGURATION) @Nullable String cgwConfiguration,
      @JsonProperty(JSON_KEY_ROUTES) @Nullable List<VpnRoute> routes,
      @JsonProperty(JSON_KEY_VGW_TELEMETRY) @Nullable List<VgwTelemetry> vgwTelemetries,
      @JsonProperty(JSON_KEY_OPTIONS) @Nullable Options options) {
    checkArgument(vpnConnectionId != null, "VPN connection Id cannot be null");
    checkArgument(
        customerGatewayId != null, "Customer gateway Id cannot be null for VPN connection");
    checkArgument(
        transitGatewayId != null || vpnGatewayId != null,
        "At least one of Transit or VPN gateway must be non-null for VPN connection");
    checkArgument(
        transitGatewayId == null || vpnGatewayId == null,
        "At least one of Transit or VPN gateway must be null for VPN connection");
    checkArgument(
        cgwConfiguration != null,
        "Customer gateway configuration cannot be null for VPN connection");
    checkArgument(routes != null, "Route list cannot be null for VPN connection");
    checkArgument(vgwTelemetries != null, "VGW telemetry cannot be null for VPN connection");
    checkArgument(options != null, "Options cannot be null for VPN connection");


    ImmutableList.Builder<IpsecTunnel> ipsecTunnels = new ImmutableList.Builder<>();

    // the field is absent for BGP connections and is "NoBGPVPNConnection" for static connections
    boolean isBgpConnection = !options.getStaticRoutesOnly();

    for (int index = 0; index < options.getTunnelOptions().size(); index++) {
      TunnelOptions ipsecTunnel = options.getTunnelOptionAtIndex(index);
      IpsecTunnel ipt = IpsecTunnel.create(ipsecTunnel);
      ipsecTunnels.add(ipt);
    }

    return new VpnConnection(
        isBgpConnection,
        vpnConnectionId,
        customerGatewayId,
        transitGatewayId != null ? GatewayType.TRANSIT : GatewayType.VPN,
        transitGatewayId != null ? transitGatewayId : vpnGatewayId,
        ipsecTunnels.build(),
        routes.stream()
            .map(VpnRoute::getDestinationCidrBlock)
            .collect(ImmutableList.toImmutableList()),
        vgwTelemetries,
        options.getStaticRoutesOnly());
  }

  /** Converts AWS IKE Phase 1 proposals into Batfish's internal model. */
  private static @Nonnull List<IkePhase1Proposal> toIkePhase1Proposals(IpsecTunnel ipsecTunnel) {
    List<IkePhase1Proposal> proposals = new ArrayList<>();
    for (Value ikePfs : ipsecTunnel.getIkePerfectForwardSecrecy()) {
      for (Value authAlgorithm : ipsecTunnel.getIpsecAuthProtocol()) {
        for (Value encryptionAlgorithm : ipsecTunnel.getIpsecEncryptionProtocol()) {
          IkePhase1Proposal ikePhase1Proposal =
              new IkePhase1Proposal(
                  "ike_proposal_"
                      + ikePfs.getValue()
                      + "_"
                      + authAlgorithm.getValue()
                      + "_"
                      + encryptionAlgorithm.getValue());
          ikePhase1Proposal.setHashingAlgorithm(
              toIkeAuthenticationAlgorithm(authAlgorithm.getValue()));
          ikePhase1Proposal.setEncryptionAlgorithm(
              toEncryptionAlgorithm(encryptionAlgorithm.getValue()));
          ikePhase1Proposal.setDiffieHellmanGroup(toDiffieHellmanGroup(ikePfs.getValue()));
          ikePhase1Proposal.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
          proposals.add(ikePhase1Proposal);
        }
      }
    }
    return proposals;
  }

  private static @Nonnull IkePhase1Policy toIkePhase1Policy(
      String vpnId,
      List<String> ikePhase1Proposals,
      IkePhase1Key ikePhase1Key,
      Ip remoteIdentity,
      String localInterface) {
    IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(vpnId);
    ikePhase1Policy.setIkePhase1Key(ikePhase1Key);
    ikePhase1Policy.setIkePhase1Proposals(ikePhase1Proposals);
    ikePhase1Policy.setRemoteIdentity(remoteIdentity.toIpSpace());
    ikePhase1Policy.setLocalInterface(localInterface);
    return ikePhase1Policy;
  }

  private static @Nonnull IkePhase1Key toIkePhase1PreSharedKey(
      IpsecTunnel ipsecTunnel, Ip remoteIdentity, String localInterface) {
    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setKeyHash(ipsecTunnel.getIkePreSharedKeyHash());
    ikePhase1Key.setRemoteIdentity(remoteIdentity.toIpSpace());
    ikePhase1Key.setLocalInterface(localInterface);
    return ikePhase1Key;
  }

  private static @Nonnull List<IpsecPhase2Proposal> toIpsecPhase2Proposals(
      IpsecTunnel ipsecTunnel, Warnings warnings) {
    List<IpsecPhase2Proposal> proposals = new ArrayList<>();

    for (Value authAlgorithm : ipsecTunnel.getIpsecAuthProtocol()) {
      for (Value encryptionAlgorithm : ipsecTunnel.getIpsecEncryptionProtocol()) {
        IpsecPhase2Proposal ipsecPhase2Proposal = new IpsecPhase2Proposal();
        ipsecPhase2Proposal.setAuthenticationAlgorithm(
            toIpsecAuthenticationAlgorithm(authAlgorithm.getValue()));
        ipsecPhase2Proposal.setEncryptionAlgorithm(
            toEncryptionAlgorithm(encryptionAlgorithm.getValue()));
        ipsecPhase2Proposal.setProtocols(
            ImmutableSortedSet.of(toIpsecProtocol(ipsecTunnel.getIpsecProtocol())));
        ipsecPhase2Proposal.setIpsecEncapsulationMode(
            toIpsecEncapdulationMode(ipsecTunnel.getIpsecMode(), warnings));
        proposals.add(ipsecPhase2Proposal);
      }
    }
    return proposals;
  }

  private static @Nonnull IpsecPhase2Policy toIpsecPhase2Policy(
      List<String> proposals,
      Value perfectForwardSecrecy) {
    IpsecPhase2Policy ipsecPhase2Policy = new IpsecPhase2Policy();
    ipsecPhase2Policy.setPfsKeyGroup(toDiffieHellmanGroup(perfectForwardSecrecy.getValue()));
    ipsecPhase2Policy.setProposals(proposals);
    return ipsecPhase2Policy;
  }

  /**
   * Sets up what is what needed to establish VPN connections to remote nodes: the underlay VRF,
   * routing export policy to backbone, and the connection to backbone.
   */
  static void initVpnConnectionsInfrastructure(Configuration gwCfg) {
    Vrf underlayVrf = Vrf.builder().setOwner(gwCfg).setName(VPN_UNDERLAY_VRF_NAME).build();

    RoutingPolicy.builder()
        .setName(VPN_TO_BACKBONE_EXPORT_POLICY_NAME)
        .setOwner(gwCfg)
        .setStatements(Collections.singletonList(EXPORT_CONNECTED_STATEMENT))
        .build();

    createBackboneConnection(gwCfg, underlayVrf, VPN_TO_BACKBONE_EXPORT_POLICY_NAME);
  }

  /**
   * Creates the infrastructure for this VPN connection on the gateway. This includes created
   * underlay and IPSec tunnel interfaces, configuring IPSec, and running BGP on the tunnel
   * interfaces.
   *
   * <p>The underlay and overlay VRFs and export/import policies must be instantiated before calling
   * this function.
   */
  void applyToGateway(
      Configuration gwCfg,
      Vrf tunnelVrf,
      @Nullable String exportPolicyName,
      @Nullable String importPolicyName,
      Warnings warnings) {
    ImmutableSortedMap.Builder<String, IkePhase1Policy> ikePhase1PolicyMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IkePhase1Key> ikePhase1KeyMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IkePhase1Proposal> ikePhase1ProposalMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IpsecPhase2Proposal> ipsecPhase2ProposalMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IpsecPhase2Policy> ipsecPhase2PolicyMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IpsecPeerConfig> ipsecPeerConfigMapBuilder =
        ImmutableSortedMap.naturalOrder();

    if (gwCfg.getVrfs().get(VPN_UNDERLAY_VRF_NAME) == null) {
      warnings.redFlagf("Underlay VRF does not exist on gateway %s", gwCfg.getHostname());
      return;
    }
    if (gwCfg.getVrfs().get(tunnelVrf.getName()) == null) {
      warnings.redFlagf("Tunnel VRF does not exist on gateway %s", gwCfg.getHostname());
      return;
    }
    List<String> seenIkePhase1Proposals = new ArrayList<>();
    List<String> seenIpsecPhase2Proposals = new ArrayList<>();

    for (int i = 0; i < _ipsecTunnels.size(); i++) {
      String tunnelId = vpnTunnelId(_vpnConnectionId, i + 1);
      IpsecTunnel ipsecTunnel = _ipsecTunnels.get(i);

      // create representation structures and add to configuration node
      String externalInterfaceName = vpnExternalInterfaceName(tunnelId);
      ConcreteInterfaceAddress externalInterfaceAddress =
          ConcreteInterfaceAddress.create(
              ipsecTunnel.getVgwOutsideAddress(), Prefix.MAX_PREFIX_LENGTH);
      Utils.newInterface(
          externalInterfaceName,
          gwCfg,
          VPN_UNDERLAY_VRF_NAME,
          externalInterfaceAddress,
          "IPSec tunnel " + tunnelId);

      String vpnIfaceName = vpnInterfaceName(tunnelId);
      ConcreteInterfaceAddress vpnInterfaceAddress =
          ConcreteInterfaceAddress.create(
              ipsecTunnel.getVgwInsideAddress(), ipsecTunnel.getVgwInsidePrefixLength());
      Utils.newInterface(
          vpnIfaceName, gwCfg, tunnelVrf.getName(), vpnInterfaceAddress, "VPN " + tunnelId);

      // configure Ike
      List<IkePhase1Proposal> ikeProposals = toIkePhase1Proposals(ipsecTunnel);
      for (IkePhase1Proposal ikePhase1Proposal : ikeProposals) {
        if (!seenIkePhase1Proposals.contains(ikePhase1Proposal.getName())) {
          ikePhase1ProposalMapBuilder.put(ikePhase1Proposal.getName(), ikePhase1Proposal);
          seenIkePhase1Proposals.add(ikePhase1Proposal.getName());
        }
      }

      IkePhase1Key ikePhase1Key =
          toIkePhase1PreSharedKey(
              ipsecTunnel, ipsecTunnel.getCgwOutsideAddress(), externalInterfaceName);
      ikePhase1KeyMapBuilder.put(tunnelId, ikePhase1Key);
      ikePhase1PolicyMapBuilder.put(
          tunnelId,
          toIkePhase1Policy(
              tunnelId,
              ikeProposals.stream().map(IkePhase1Proposal::getName).toList(),
              ikePhase1Key,
              ipsecTunnel.getCgwOutsideAddress(),
              externalInterfaceName));

      // configure Ipsec
      List<IpsecPhase2Proposal> ipsecProposals = toIpsecPhase2Proposals(ipsecTunnel, warnings);
      List<String> ipsecProposalNames = Lists.newArrayList();
      for (IpsecPhase2Proposal ipsecPhase2Proposal : ipsecProposals) {
        String name =
            "ipsec_proposal_"
                + ipsecPhase2Proposal.getAuthenticationAlgorithm()
                + "_"
                + ipsecPhase2Proposal.getEncryptionAlgorithm();
        if (!seenIpsecPhase2Proposals.contains(name)) {
          ipsecPhase2ProposalMapBuilder.put(name, ipsecPhase2Proposal);
          seenIpsecPhase2Proposals.add(name);
        }
        ipsecProposalNames.add(name);
      }
      for (Value pfs : ipsecTunnel.getIpsecPerfectForwardSecrecy()) {
        String ipsecPolicyName = tunnelId + "-" + toDiffieHellmanGroup(pfs.getValue());
        ipsecPhase2PolicyMapBuilder.put(
            ipsecPolicyName, toIpsecPhase2Policy(ipsecProposalNames, pfs));
        ipsecPeerConfigMapBuilder.put(
            ipsecPolicyName,
            IpsecStaticPeerConfig.builder()
                .setTunnelInterface(vpnIfaceName)
                .setIkePhase1Policy(tunnelId)
                .setIpsecPolicy(ipsecPolicyName)
                .setSourceInterface(externalInterfaceName)
                .setLocalAddress(ipsecTunnel.getVgwOutsideAddress())
                .setDestinationAddress(ipsecTunnel.getCgwOutsideAddress())
                .build());
      }

      // configure BGP peering
      if (_isBgpConnection) {
        BgpActivePeerConfig.builder()
            .setPeerAddress(ipsecTunnel.getCgwInsideAddress())
            .setRemoteAsns(
                Optional.ofNullable(ipsecTunnel.getCgwBgpAsn())
                    .map(LongSpace::of)
                    .orElse(LongSpace.EMPTY))
            .setBgpProcess(tunnelVrf.getBgpProcess())
            .setLocalAs(ipsecTunnel.getVgwBgpAsn())
            .setLocalIp(ipsecTunnel.getVgwInsideAddress())
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder()
                    .setExportPolicy(exportPolicyName)
                    .setImportPolicy(importPolicyName)
                    .build())
            .build();
      }

      // configure static routes -- this list of routes should be empty in case of transit gateway
      _routes.forEach(
          pfx -> addStaticRoute(gwCfg, toStaticRoute(pfx, ipsecTunnel.getCgwInsideAddress())));
    }
    gwCfg.extendIkePhase1Proposls(ikePhase1ProposalMapBuilder.build());
    gwCfg.extendIkePhase1Keys(ikePhase1KeyMapBuilder.build());
    gwCfg.extendIkePhase1Policies(ikePhase1PolicyMapBuilder.build());
    gwCfg.extendIpsecPhase2Proposals(ipsecPhase2ProposalMapBuilder.build());
    gwCfg.extendIpsecPhase2Policies(ipsecPhase2PolicyMapBuilder.build());
    gwCfg.extendIpsecPeerConfigs(ipsecPeerConfigMapBuilder.build());
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class Options {

    private final List<TunnelOptions> _tunnelOptions;

    private final boolean _staticRoutesOnly;

    private Options(List<TunnelOptions> tunnelOptions, boolean staticRoutesOnly) {
      _tunnelOptions = tunnelOptions;
      _staticRoutesOnly = staticRoutesOnly;
    }

    @Nonnull
    @JsonCreator
    private static Options create(
        @JsonProperty(JSON_KEY_TUNNEL_OPTIONS) @Nullable List<TunnelOptions> tunnelOptions,
        @JsonProperty(JSON_KEY_STATIC_ROUTES_ONLY) @Nullable Boolean staticRoutesOnly) {
      return new Options(
          firstNonNull(tunnelOptions, Collections.emptyList()),
          firstNonNull(staticRoutesOnly, false));
    }

    TunnelOptions getTunnelOptionAtIndex(int index) {
      if (index < 0 || index >= _tunnelOptions.size()) {
        throw new IndexOutOfBoundsException(
            "Index " + index + " is out of bounds for length " + _tunnelOptions.size());
      }
      return _tunnelOptions.get(index);
    }

    @Nonnull
    List<TunnelOptions> getTunnelOptions() {
      return _tunnelOptions;
    }

    boolean getStaticRoutesOnly() {
      return _staticRoutesOnly;
    }
  }

  @Nonnull
  List<VgwTelemetry> getVgwTelemetries() {
    return _vgwTelemetries;
  }

  @Nonnull
  String getCustomerGatewayId() {
    return _customerGatewayId;
  }

  @Override
  public String getId() {
    return _vpnConnectionId;
  }

  @Nonnull
  List<IpsecTunnel> getIpsecTunnels() {
    return _ipsecTunnels;
  }

  @Nonnull
  List<Prefix> getRoutes() {
    return _routes;
  }

  boolean getStaticRoutesOnly() {
    return _staticRoutesOnly;
  }

  boolean isBgpConnection() {
    return _isBgpConnection;
  }

  @Nonnull
  String getVpnConnectionId() {
    return _vpnConnectionId;
  }

  @Nonnull
  GatewayType getAwsGatewayType() {
    return _awsGatewayType;
  }

  @Nonnull
  String getAwsGatewayId() {
    return _awsGatewayId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VpnConnection)) {
      return false;
    }
    VpnConnection that = (VpnConnection) o;
    return _staticRoutesOnly == that._staticRoutesOnly
        && Objects.equals(_customerGatewayId, that._customerGatewayId)
        && Objects.equals(_ipsecTunnels, that._ipsecTunnels)
        && Objects.equals(_isBgpConnection, that._isBgpConnection)
        && Objects.equals(_routes, that._routes)
        && Objects.equals(_vgwTelemetries, that._vgwTelemetries)
        && Objects.equals(_vpnConnectionId, that._vpnConnectionId)
        && Objects.equals(_awsGatewayType, that._awsGatewayType)
        && Objects.equals(_awsGatewayId, that._awsGatewayId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _customerGatewayId,
        _ipsecTunnels,
        _isBgpConnection,
        _routes,
        _staticRoutesOnly,
        _vgwTelemetries,
        _vpnConnectionId,
        _awsGatewayType.ordinal(),
        _awsGatewayId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_customerGatewayId", _customerGatewayId)
        .add("_ipsecTunnels", _ipsecTunnels)
        .add("_isBgpConnection", _isBgpConnection)
        .add("_routes", _routes)
        .add("_staticRoutesOnly", _staticRoutesOnly)
        .add("_vgwTelemetries", _vgwTelemetries)
        .add("_vpnConnectionId", _vpnConnectionId)
        .add("_awsGatewayType", _awsGatewayType)
        .add("_awsGatewayId", _awsGatewayId)
        .toString();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  public static class TunnelOptions implements Serializable {
    @Nullable private final List<Value> _ikeVersions;
    @Nullable private final List<Value> _phase1EncryptionAlgorithm;
    @Nullable private final List<Value> _phase1IntegrityAlgorithm;
    @Nullable private final List<Value> _phase1DHGroupNumbers;
    @Nullable private final List<Value> _phase2EncryptionAlgorithm;
    @Nullable private final List<Value> _phase2IntegrityAlgorithms;
    @Nullable private final List<Value> _phase2DHGroupNumbers;
    @Nullable private final Ip _outsideIpAddress;
    @Nullable private final String _tunnelInsideCidr;
    @Nullable private final Integer _replayWindowSize;
    @Nullable private final String _presharedKey;
    @Nullable private final Integer _phase2LifetimeSeconds;

    private TunnelOptions(
        @Nullable List<Value> ikeVersions,
        @Nullable List<Value> phase1EncryptionAlgorithm,
        @Nullable List<Value> phase1IntegrityAlgorithm,
        @Nullable List<Value> phase1DHGroupNumbers,
        @Nullable List<Value> phase2EncryptionAlgorithm,
        @Nullable List<Value> phase2IntegrityAlgorithms,
        @Nullable List<Value> phase2DHGroupNumbers,
        @Nullable Ip outsideIpAddress,
        @Nullable String tunnelInsideCidr,
        @Nullable Integer replayWindowSize,
        @Nullable String presharedKey,
        @Nullable Integer phase2LifetimeSeconds) {
      _ikeVersions = ikeVersions;
      _phase1EncryptionAlgorithm = phase1EncryptionAlgorithm;
      _phase1IntegrityAlgorithm = phase1IntegrityAlgorithm;
      _phase1DHGroupNumbers = phase1DHGroupNumbers;
      _phase2EncryptionAlgorithm = phase2EncryptionAlgorithm;
      _phase2IntegrityAlgorithms = phase2IntegrityAlgorithms;
      _phase2DHGroupNumbers = phase2DHGroupNumbers;
      _outsideIpAddress = outsideIpAddress;
      _tunnelInsideCidr = tunnelInsideCidr;
      _replayWindowSize = replayWindowSize;
      _presharedKey = presharedKey;
      _phase2LifetimeSeconds = phase2LifetimeSeconds;
    }

    @Nonnull
    @JsonCreator
    private static TunnelOptions create(
        @JsonProperty(JSON_KEY_IKE_VERSIONS) @Nullable List<Value> ikeVersions,
        @JsonProperty(JSON_KEY_PHASE1_ENCRYPTION_ALGORITHMS) @Nullable
            List<Value> phase1EncryptionAlgorithm,
        @JsonProperty(JSON_KEY_PHASE1_INTEGRITY_ALGORITHMS) @Nullable
            List<Value> phase1IntegrityAlgorithm,
        @JsonProperty(JSON_KEY_PHASE1_DH_GROUP_NUMBERS) @Nullable List<Value> phase1DHGroupNumbers,
        @JsonProperty(JSON_KEY_PHASE2_ENCRYPTION_ALGORITHMS) @Nullable
            List<Value> phase2EncryptionAlgorithm,
        @JsonProperty(JSON_KEY_PHASE2_INTEGRITY_ALGORITHMS) @Nullable
            List<Value> phase2IntegrityAlgorithms,
        @JsonProperty(JSON_KEY_PHASE2_DH_GROUP_NUMBERS) @Nullable List<Value> phase2DHGroupNumbers,
        @JsonProperty(JSON_KEY_OUTSIDE_IP_ADDRESS) @Nullable String outsideIpAddress,
        @JsonProperty(JSON_KEY_TUNNEL_INSIDE_CIDR) @Nullable String tunnelInsideCidr,
        @JsonProperty(JSON_KEY_REPLAY_WINDOW_SIZE) @Nullable Integer replayWindowSize,
        @JsonProperty(JSON_KEY_PRESHARED_KEY) @Nullable String presharedKey,
        @JsonProperty(JSON_KEY_PHASE2_LIFETIME_SECONDS) @Nullable Integer phase2LifetimeSeconds) {
      checkArgument(outsideIpAddress != null, "OutsideIpAddress cannot be null");
      checkArgument(presharedKey != null, "PreSharedKey cannot be null");
      checkArgument(tunnelInsideCidr != null, "TunnelInsideCidr cannot be null");
      return new TunnelOptions(
          ikeVersions,
          firstNonNull(
              phase1EncryptionAlgorithm,
              List.of(
                  new Value("AES128"),
                  new Value("AES256"),
                  new Value("AES128-GCM-16"),
                  new Value("AES256-GCM-16"))),
          firstNonNull(
              phase1IntegrityAlgorithm,
              List.of(
                  new Value("SHA1"),
                  new Value("SHA2-256"),
                  new Value("SHA2-384"),
                  new Value("SHA2-512"))),
          firstNonNull(
              phase1DHGroupNumbers,
              List.of(
                  new Value("2"),
                  new Value("14"),
                  new Value("15"),
                  new Value("16"),
                  new Value("17"),
                  new Value("18"),
                  new Value("19"),
                  new Value("20"),
                  new Value("21"),
                  new Value("22"),
                  new Value("23"),
                  new Value("24"))),
          firstNonNull(
              phase2EncryptionAlgorithm,
              List.of(
                  new Value("AES128"),
                  new Value("AES256"),
                  new Value("AES128-GCM-16"),
                  new Value("AES256-GCM-16"))),
          firstNonNull(
              phase2IntegrityAlgorithms,
              List.of(
                  new Value("SHA1"),
                  new Value("SHA2-256"),
                  new Value("SHA2-384"),
                  new Value("SHA2-512"))),
          firstNonNull(
              phase2DHGroupNumbers,
              List.of(
                  new Value("2"),
                  new Value("5"),
                  new Value("14"),
                  new Value("15"),
                  new Value("16"),
                  new Value("17"),
                  new Value("18"),
                  new Value("19"),
                  new Value("20"),
                  new Value("21"),
                  new Value("22"),
                  new Value("23"),
                  new Value("24"))),
          Ip.parse(outsideIpAddress),
          tunnelInsideCidr,
          firstNonNull(replayWindowSize, 28800),
          presharedKey,
          firstNonNull(phase2LifetimeSeconds, 3600));
    }

    @Nullable
    List<Value> getIkeVersion() {
      return _ikeVersions;
    }

    @Nullable
    List<Value> getPhase1EncryptionAlgorithm() {
      return _phase1EncryptionAlgorithm;
    }

    @Nullable
    List<Value> getPhase1IntegrityAlgorithm() {
      return _phase1IntegrityAlgorithm;
    }

    @Nullable
    List<Value> getPhase1DHGroupNumbers() {
      return _phase1DHGroupNumbers;
    }

    @Nullable
    List<Value> getPhase2EncryptionAlgorithm() {
      return _phase2EncryptionAlgorithm;
    }

    @Nullable
    List<Value> getPhase2IntegrityAlgorithm() {
      return _phase2IntegrityAlgorithms;
    }

    @Nullable
    List<Value> getPhase2DHGroupNumbers() {
      return _phase2DHGroupNumbers;
    }

    @Nullable
    Ip getOutsideIpAddress() {
      return _outsideIpAddress;
    }
    ;

    @Nullable
    String getTunnelInsideCidr() {
      return _tunnelInsideCidr;
    }
    ;

    @Nullable
    Integer getReplayWindowSize() {
      return _replayWindowSize;
    }
    ;

    @Nullable
    String getPresharedKey() {
      return _presharedKey;
    }
    ;

    @Nullable
    Integer getPhase2LifetimeSeconds() {
      return _phase2LifetimeSeconds;
    }
    ;
  }
}
