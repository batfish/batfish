package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.Utils.ACCEPT_ALL_BGP;
import static org.batfish.representation.aws.Utils.ACCEPT_ALL_BGP_AND_STATIC;
import static org.batfish.representation.aws.Utils.makeBgpProcess;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Collections;
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
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

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

  static final String DXGW_EXPORT_POLICY_NAME = "~dxgw~export-policy~";
  static final String DXGW_IMPORT_POLICY_NAME = "~dxgw~import-policy~";

  /**
   * Creates a Configuration node for this Direct Connect Gateway. Uses a single default VRF for
   * both TGW-facing and customer-facing (VIF) interfaces so that routes learned from the customer
   * via BGP are automatically available for forwarding toward the TGW, and vice versa.
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

    // Initialize BGP in the default VRF
    initBgp(cfgNode);

    // Configure BGP sessions toward customer routers via VIFs
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
        .setName(DXGW_EXPORT_POLICY_NAME)
        .setOwner(cfgNode)
        .setStatements(Collections.singletonList(ACCEPT_ALL_BGP_AND_STATIC))
        .build();

    RoutingPolicy.builder()
        .setName(DXGW_IMPORT_POLICY_NAME)
        .setOwner(cfgNode)
        .setStatements(Collections.singletonList(ACCEPT_ALL_BGP))
        .build();
  }

  private void configureVifBgpSession(
      Configuration cfgNode, DirectConnectVirtualInterface vif, Warnings warnings) {
    Ip amazonIp = vif.getAmazonIp();
    Ip customerIp = vif.getCustomerIp();

    Utils.newInterface(
        vif.getId(),
        cfgNode,
        vif.getAmazonAddress(),
        "Direct Connect VIF " + vif.getVirtualInterfaceName());

    // Add BGP peer for the customer router
    BgpActivePeerConfig.builder()
        .setPeerAddress(customerIp)
        .setRemoteAsns(LongSpace.of(vif.getAsn()))
        .setLocalIp(amazonIp)
        .setLocalAs(_amazonSideAsn)
        .setBgpProcess(cfgNode.getDefaultVrf().getBgpProcess())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(DXGW_EXPORT_POLICY_NAME)
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
