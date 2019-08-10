package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.getTextXml;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.representation.aws.VpnGateway.VGW_EXPORT_POLICY_NAME;
import static org.batfish.representation.aws.VpnGateway.VGW_IMPORT_POLICY_NAME;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Represents an AWS VPN connection */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class VpnConnection implements AwsVpcEntity, Serializable {

  private static final int BGP_NEIGHBOR_DEFAULT_METRIC = 0;

  private static DiffieHellmanGroup toDiffieHellmanGroup(String perfectForwardSecrecy) {
    switch (perfectForwardSecrecy) {
      case "group2":
        return DiffieHellmanGroup.GROUP2;
      default:
        throw new BatfishException(
            "No conversion to Diffie-Hellman group for string: \"" + perfectForwardSecrecy + "\"");
    }
  }

  private static EncryptionAlgorithm toEncryptionAlgorithm(String encryptionProtocol) {
    switch (encryptionProtocol) {
      case "aes-128-cbc":
        return EncryptionAlgorithm.AES_128_CBC;
      default:
        throw new BatfishException(
            "No conversion to encryption algorithm for string: \"" + encryptionProtocol + "\"");
    }
  }

  private static IkeHashingAlgorithm toIkeAuthenticationAlgorithm(String ikeAuthProtocol) {
    switch (ikeAuthProtocol) {
      case "sha1":
        return IkeHashingAlgorithm.SHA1;

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
      case "hmac-sha1-96":
        return IpsecAuthenticationAlgorithm.HMAC_SHA1_96;
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

  @Nullable
  private static IpsecEncapsulationMode toIpsecEncapdulationMode(
      String ipsecEncapsulationMode, Warnings warnings) {
    switch (ipsecEncapsulationMode) {
      case "tunnel":
        return IpsecEncapsulationMode.TUNNEL;
      case "transport":
        return IpsecEncapsulationMode.TRANSPORT;
      default:
        warnings.redFlag(
            String.format("No IPsec encapsulation mode for string '%s'", ipsecEncapsulationMode));
        return null;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class VpnRoute {

    @JsonCreator
    private static VpnRoute create(
        @Nullable @JsonProperty(JSON_KEY_DESTINATION_CIDR_BLOCK) Prefix destinationCidrBlock) {
      checkArgument(
          destinationCidrBlock != null, "Destination CIDR block cannot be null in VpnRoute");
      return new VpnRoute(destinationCidrBlock);
    }

    @Nonnull private final Prefix _destinationCidrBlock;

    private VpnRoute(Prefix destinationCidrBlock) {
      _destinationCidrBlock = destinationCidrBlock;
    }

    @Nonnull
    Prefix getDestinationCidrBlock() {
      return _destinationCidrBlock;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class Options {

    @JsonCreator
    private static Options create(
        @Nullable @JsonProperty(JSON_KEY_STATIC_ROUTES_ONLY) Boolean staticRoutesOnly) {
      return new Options(firstNonNull(staticRoutesOnly, false));
    }

    private final boolean _staticRoutesOnly;

    private Options(boolean staticRoutesOnly) {
      _staticRoutesOnly = staticRoutesOnly;
    }

    boolean getStaticRoutesOnly() {
      return _staticRoutesOnly;
    }
  }

  @Nonnull private final String _customerGatewayId;

  @Nonnull private final List<IpsecTunnel> _ipsecTunnels;

  private final boolean _isBgpConnection;

  @Nonnull private final List<Prefix> _routes;

  @Nonnull private final boolean _staticRoutesOnly;

  @Nonnull private final List<VgwTelemetry> _vgwTelemetrys;

  @Nonnull private final String _vpnConnectionId;

  @Nonnull private final String _vpnGatewayId;

  @JsonCreator
  private static VpnConnection create(
      @Nullable @JsonProperty(JSON_KEY_VPN_CONNECTION_ID) String vpnConnectionId,
      @Nullable @JsonProperty(JSON_KEY_CUSTOMER_GATEWAY_ID) String customerGatewayId,
      @Nullable @JsonProperty(JSON_KEY_VPN_GATEWAY_ID) String vpnGatewayId,
      @Nullable @JsonProperty(JSON_KEY_CUSTOMER_GATEWAY_CONFIGURATION) String cgwConfiguration,
      @Nullable @JsonProperty(JSON_KEY_ROUTES) List<VpnRoute> routes,
      @Nullable @JsonProperty(JSON_KEY_VGW_TELEMETRY) List<VgwTelemetry> vgwTelemetrys,
      @Nullable @JsonProperty(JSON_KEY_OPTIONS) Options options) {
    checkArgument(vpnConnectionId != null, "VPN connection Id cannot be null");
    checkArgument(
        customerGatewayId != null, "Customer gateway Id cannot be null for VPN connection");
    checkArgument(vpnGatewayId != null, "VPN gateway Id cannot be null for VPN connection");
    checkArgument(
        cgwConfiguration != null,
        "Customer gateway configuration cannot be null for VPN connection");
    checkArgument(routes != null, "Route list cannot be null for VPN connection");
    checkArgument(vgwTelemetrys != null, "VGW telemetry cannot be null for VPN connection");
    checkArgument(options != null, "Options cannot be null for VPN connection");

    Document document;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(cgwConfiguration));
      document = builder.parse(is);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new IllegalArgumentException(
          "Could not parse XML for CustomerGatewayConfiguration for vpn connection "
              + vpnConnectionId
              + " "
              + e);
    }

    ImmutableList.Builder<IpsecTunnel> ipsecTunnels = new ImmutableList.Builder<>();

    Element vpnConnection = (Element) document.getElementsByTagName(XML_KEY_VPN_CONNECTION).item(0);

    // the field is absent for BGP connections and is "NoBGPVPNConnection" for static connections
    boolean isBgpConnection =
        vpnConnection
                    .getElementsByTagName(AwsVpcEntity.XML_KEY_VPN_CONNECTION_ATTRIBUTES)
                    .getLength()
                == 0
            || !getTextXml(vpnConnection, AwsVpcEntity.XML_KEY_VPN_CONNECTION_ATTRIBUTES)
                .contains("NoBGP");

    NodeList nodeList = document.getElementsByTagName(XML_KEY_IPSEC_TUNNEL);

    for (int index = 0; index < nodeList.getLength(); index++) {
      Element ipsecTunnel = (Element) nodeList.item(index);
      ipsecTunnels.add(IpsecTunnel.create(ipsecTunnel, isBgpConnection));
    }

    return new VpnConnection(
        isBgpConnection,
        vpnConnectionId,
        customerGatewayId,
        vpnGatewayId,
        ipsecTunnels.build(),
        routes.stream()
            .map(VpnRoute::getDestinationCidrBlock)
            .collect(ImmutableList.toImmutableList()),
        vgwTelemetrys,
        options.getStaticRoutesOnly());
  }

  VpnConnection(
      boolean isBgpConnection,
      String vpnConnectionId,
      String customerGatewayId,
      String vpnGatewayId,
      List<IpsecTunnel> ipsecTunnels,
      List<Prefix> routes,
      List<VgwTelemetry> vgwTelemetrys,
      boolean staticRoutesOnly) {
    _isBgpConnection = isBgpConnection;
    _vpnConnectionId = vpnConnectionId;
    _customerGatewayId = customerGatewayId;
    _vpnGatewayId = vpnGatewayId;
    _ipsecTunnels = ipsecTunnels;
    _routes = routes;
    _vgwTelemetrys = vgwTelemetrys;
    _staticRoutesOnly = staticRoutesOnly;
  }

  @Nonnull
  private static IkePhase1Proposal toIkePhase1Proposal(
      String proposalName, IpsecTunnel ipsecTunnel) {
    IkePhase1Proposal ikePhase1Proposal = new IkePhase1Proposal(proposalName);
    if (ipsecTunnel.getIkePreSharedKeyHash() != null) {
      ikePhase1Proposal.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    }
    ikePhase1Proposal.setHashingAlgorithm(
        toIkeAuthenticationAlgorithm(ipsecTunnel.getIkeAuthProtocol()));
    ikePhase1Proposal.setDiffieHellmanGroup(
        toDiffieHellmanGroup(ipsecTunnel.getIkePerfectForwardSecrecy()));
    ikePhase1Proposal.setEncryptionAlgorithm(
        toEncryptionAlgorithm(ipsecTunnel.getIkeEncryptionProtocol()));
    return ikePhase1Proposal;
  }

  @Nonnull
  private static IkePhase1Key toIkePhase1PreSharedKey(
      IpsecTunnel ipsecTunnel, Ip remoteIdentity, String localInterface) {
    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setKeyHash(ipsecTunnel.getIkePreSharedKeyHash());
    ikePhase1Key.setRemoteIdentity(remoteIdentity.toIpSpace());
    ikePhase1Key.setLocalInterface(localInterface);
    return ikePhase1Key;
  }

  @Nonnull
  private static IkePhase1Policy toIkePhase1Policy(
      String vpnId,
      String ikePhase1ProposalName,
      IkePhase1Key ikePhase1Key,
      Ip remoteIdentity,
      String localInterface) {
    IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(vpnId);
    ikePhase1Policy.setIkePhase1Key(ikePhase1Key);
    ikePhase1Policy.setIkePhase1Proposals(ImmutableList.of(ikePhase1ProposalName));
    ikePhase1Policy.setRemoteIdentity(remoteIdentity.toIpSpace());
    ikePhase1Policy.setLocalInterface(localInterface);
    return ikePhase1Policy;
  }

  @Nonnull
  private static IpsecPhase2Proposal toIpsecPhase2Proposal(
      IpsecTunnel ipsecTunnel, Warnings warnings) {
    IpsecPhase2Proposal ipsecPhase2Proposal = new IpsecPhase2Proposal();
    ipsecPhase2Proposal.setAuthenticationAlgorithm(
        toIpsecAuthenticationAlgorithm(ipsecTunnel.getIpsecAuthProtocol()));
    ipsecPhase2Proposal.setEncryptionAlgorithm(
        toEncryptionAlgorithm(ipsecTunnel.getIpsecEncryptionProtocol()));
    ipsecPhase2Proposal.setProtocols(
        ImmutableSortedSet.of(toIpsecProtocol(ipsecTunnel.getIpsecProtocol())));
    ipsecPhase2Proposal.setIpsecEncapsulationMode(
        toIpsecEncapdulationMode(ipsecTunnel.getIpsecMode(), warnings));
    return ipsecPhase2Proposal;
  }

  @Nonnull
  private static IpsecPhase2Policy toIpsecPhase2Policy(
      IpsecTunnel ipsecTunnel, String ipsecPhase2Proposal) {
    IpsecPhase2Policy ipsecPhase2Policy = new IpsecPhase2Policy();
    ipsecPhase2Policy.setPfsKeyGroup(
        toDiffieHellmanGroup(ipsecTunnel.getIpsecPerfectForwardSecrecy()));
    ipsecPhase2Policy.setProposals(ImmutableList.of(ipsecPhase2Proposal));
    return ipsecPhase2Policy;
  }

  void applyToVpnGateway(AwsConfiguration awsConfiguration, Region region, Warnings warnings) {
    if (!awsConfiguration.getConfigurationNodes().containsKey(_vpnGatewayId)) {
      warnings.redFlag(
          String.format(
              "VPN Gateway \"%s\" referred by VPN connection \"%s\" not found",
              _vpnGatewayId, _vpnConnectionId));
      return;
    }
    Configuration vgwCfgNode = awsConfiguration.getConfigurationNodes().get(_vpnGatewayId);

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

    for (int i = 0; i < _ipsecTunnels.size(); i++) {
      int idNum = i + 1;
      String vpnId = _vpnConnectionId + "-" + idNum;
      IpsecTunnel ipsecTunnel = _ipsecTunnels.get(i);

      // create representation structures and add to configuration node
      String externalInterfaceName = getExternalInterfaceName(idNum);
      ConcreteInterfaceAddress externalInterfaceAddress =
          ConcreteInterfaceAddress.create(
              ipsecTunnel.getVgwOutsideAddress(), Prefix.MAX_PREFIX_LENGTH);
      Utils.newInterface(
          externalInterfaceName, vgwCfgNode, externalInterfaceAddress, "IPSec tunnel " + idNum);

      String vpnInterfaceName = getVpnInterfaceName(idNum);
      ConcreteInterfaceAddress vpnInterfaceAddress =
          ConcreteInterfaceAddress.create(
              ipsecTunnel.getVgwInsideAddress(), ipsecTunnel.getVgwInsidePrefixLength());
      Utils.newInterface(vpnInterfaceName, vgwCfgNode, vpnInterfaceAddress, "VPN " + idNum);

      // configure Ipsec
      ikePhase1ProposalMapBuilder.put(vpnId, toIkePhase1Proposal(vpnId, ipsecTunnel));
      IkePhase1Key ikePhase1Key =
          toIkePhase1PreSharedKey(
              ipsecTunnel, ipsecTunnel.getCgwOutsideAddress(), externalInterfaceName);
      ikePhase1KeyMapBuilder.put(vpnId, ikePhase1Key);
      ikePhase1PolicyMapBuilder.put(
          vpnId,
          toIkePhase1Policy(
              vpnId,
              vpnId,
              ikePhase1Key,
              ipsecTunnel.getCgwOutsideAddress(),
              externalInterfaceName));
      ipsecPhase2ProposalMapBuilder.put(vpnId, toIpsecPhase2Proposal(ipsecTunnel, warnings));
      ipsecPhase2PolicyMapBuilder.put(vpnId, toIpsecPhase2Policy(ipsecTunnel, vpnId));
      ipsecPeerConfigMapBuilder.put(
          vpnId,
          IpsecStaticPeerConfig.builder()
              .setTunnelInterface(vpnInterfaceName)
              .setIkePhase1Policy(vpnId)
              .setIpsecPolicy(vpnId)
              .setSourceInterface(externalInterfaceName)
              .setLocalAddress(ipsecTunnel.getVgwOutsideAddress())
              .setDestinationAddress(ipsecTunnel.getCgwOutsideAddress())
              .build());

      // configure BGP peering
      if (_isBgpConnection) {
        BgpActivePeerConfig.builder()
            .setPeerAddress(ipsecTunnel.getCgwInsideAddress())
            .setRemoteAs(ipsecTunnel.getCgwBgpAsn())
            .setBgpProcess(vgwCfgNode.getDefaultVrf().getBgpProcess())
            .setLocalAs(ipsecTunnel.getVgwBgpAsn())
            .setLocalIp(ipsecTunnel.getVgwInsideAddress())
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder()
                    .setExportPolicy(VGW_EXPORT_POLICY_NAME)
                    .setImportPolicy(VGW_IMPORT_POLICY_NAME)
                    .build())
            .build();
      }

      // configure IPsec
      _routes.forEach(
          pfx -> addStaticRoute(vgwCfgNode, toStaticRoute(pfx, ipsecTunnel.getCgwInsideAddress())));
    }

    vgwCfgNode.setIkePhase1Proposals(ikePhase1ProposalMapBuilder.build());
    vgwCfgNode.setIkePhase1Keys(ikePhase1KeyMapBuilder.build());
    vgwCfgNode.setIkePhase1Policies(ikePhase1PolicyMapBuilder.build());
    vgwCfgNode.setIpsecPhase2Proposals(ipsecPhase2ProposalMapBuilder.build());
    vgwCfgNode.setIpsecPhase2Policies(ipsecPhase2PolicyMapBuilder.build());
    vgwCfgNode.setIpsecPeerConfigs(ipsecPeerConfigMapBuilder.build());
  }

  @VisibleForTesting
  static String getExternalInterfaceName(int idNum) {
    return "external" + idNum;
  }

  @VisibleForTesting
  static String getVpnInterfaceName(int idNum) {
    return "vpn" + idNum;
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

  @Nonnull
  List<VgwTelemetry> getVgwTelemetrys() {
    return _vgwTelemetrys;
  }

  boolean isBgpConnection() {
    return _isBgpConnection;
  }

  @Nonnull
  String getVpnConnectionId() {
    return _vpnConnectionId;
  }

  @Nonnull
  String getVpnGatewayId() {
    return _vpnGatewayId;
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
        && Objects.equals(_vgwTelemetrys, that._vgwTelemetrys)
        && Objects.equals(_vpnConnectionId, that._vpnConnectionId)
        && Objects.equals(_vpnGatewayId, that._vpnGatewayId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _customerGatewayId,
        _ipsecTunnels,
        _isBgpConnection,
        _routes,
        _staticRoutesOnly,
        _vgwTelemetrys,
        _vpnConnectionId,
        _vpnGatewayId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_customerGatewayId", _customerGatewayId)
        .add("_ipsecTunnels", _ipsecTunnels)
        .add("_isBgpConnection", _isBgpConnection)
        .add("_routes", _routes)
        .add("_staticRoutesOnly", _staticRoutesOnly)
        .add("_vgwTelemetrys", _vgwTelemetrys)
        .add("_vpnConnectionId", _vpnConnectionId)
        .add("_vpnGatewayId", _vpnGatewayId)
        .toString();
  }
}
